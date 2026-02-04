package com.example.graph.config.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.rag.RagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * RAG Demo 的 Graph 配置（官方方式）：START → rag → END。
 * 使用 Spring AI 官方 RetrievalAugmentationAdvisor + VectorStoreDocumentRetriever，检索与生成由 Advisor 完成。
 */
@Configuration
public class RagGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(RagGraphConfig.class);
    private static final String DOCS_LOCATION = "classpath:docs/**";

    private static final int TOP_K = 3;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    /** 使用官方 TokenTextSplitter 分块后写入向量库。 */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, ResourcePatternResolver resolver) throws IOException {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        Resource[] resources = resolver.getResources(DOCS_LOCATION);
        List<Document> rawDocs = new ArrayList<>();
        for (Resource resource : resources) {
            if (!resource.isReadable() || resource.getFilename() == null) continue;
            try {
                String content = new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (content.isBlank()) continue;
                rawDocs.add(new Document(content, Map.of("source", resource.getFilename())));
            } catch (IOException e) {
                log.warn("跳过无法读取的文件: {}", resource.getFilename(), e);
            }
        }
        if (rawDocs.isEmpty()) return store;
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(rawDocs);
        store.add(chunks);
        log.info("已从 docs/ 加载并用 TokenTextSplitter 分块，共 {} 个片段写入向量库", chunks.size());
        return store;
    }

    /** 官方 RAG Advisor：检索 + 增强 prompt 后交给大模型。 */
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build())
                .build();
    }

    @Bean
    public StateGraph ragGraph(ChatClient chatClient, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("query", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("RagGraph", stateFactory)
                .addNode("rag", node_async(new RagNode(chatClient, retrievalAugmentationAdvisor)))
                .addEdge(StateGraph.START, "rag")
                .addEdge("rag", StateGraph.END);
    }
}
