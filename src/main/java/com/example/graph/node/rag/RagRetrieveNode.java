package com.example.graph.node.rag;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 检索节点：使用 Spring AI 官方 DocumentRetriever（如 VectorStoreDocumentRetriever）从 state 读 query 做相似度检索，写回 retrievedContext。
 */
public class RagRetrieveNode implements NodeAction {

    private final DocumentRetriever documentRetriever;

    public RagRetrieveNode(DocumentRetriever documentRetriever) {
        this.documentRetriever = documentRetriever;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String queryText = state.value("query", "");
        List<Document> docs = documentRetriever.retrieve(new Query(queryText));
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        return Map.of("retrievedContext", context);
    }
}
