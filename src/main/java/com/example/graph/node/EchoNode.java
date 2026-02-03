package com.example.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 简单 Echo 节点：从 state 读 query，调大模型一句话回答，把回复写回 result。
 */
public class EchoNode implements NodeAction {

    private static final PromptTemplate PROMPT = new PromptTemplate("请用一句话回答：{query}");

    private final ChatClient chatClient;

    public EchoNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        Flux<String> stream = chatClient.prompt()
                .user(u -> u.text(PROMPT.getTemplate()).param("query", query))
                .stream()
                .content();
        String result = stream.reduce("", (a, b) -> a + b).block();
        return Map.of("result", result != null ? result : "");
    }
}
