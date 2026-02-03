package com.example.graph.node.rag;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * RAG 生成节点：从 state 读 query 和 retrievedContext，按「参考 + 问题」调大模型，把回复写回 result。
 */
public class RagGenerateNode implements NodeAction {

    private static final PromptTemplate PROMPT = new PromptTemplate(
            "请仅根据以下「参考」内容回答问题；若参考为空或与问题无关，请明确回答「未检索到相关文档」或「参考中无此信息」。不要编造。\n\n参考：\n{context}\n\n问题：{query}\n\n回答：");

    private final ChatClient chatClient;

    public RagGenerateNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        String context = state.value("retrievedContext", "");
        Flux<String> stream = chatClient.prompt()
                .user(u -> u.text(PROMPT.getTemplate())
                        .param("context", context)
                        .param("query", query))
                .stream()
                .content();
        String result = stream.reduce("", (a, b) -> a + b).block();
        return Map.of("result", result != null ? result : "");
    }
}
