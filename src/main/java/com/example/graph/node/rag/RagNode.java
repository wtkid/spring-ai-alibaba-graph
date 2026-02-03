package com.example.graph.node.rag;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * RAG 节点（官方方式）：使用 Spring AI 官方 RetrievalAugmentationAdvisor，由 Advisor 完成检索并增强 prompt 后调大模型。
 */
public class RagNode implements NodeAction {

    private final ChatClient chatClient;
    private final Advisor ragAdvisor;

    public RagNode(ChatClient.Builder chatClientBuilder, Advisor ragAdvisor) {
        this.chatClient = chatClientBuilder.build();
        this.ragAdvisor = ragAdvisor;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        String result = chatClient.prompt()
                .advisors(ragAdvisor)
                .user(query)
                .call()
                .content();
        return Map.of("result", result != null ? result : "");
    }
}
