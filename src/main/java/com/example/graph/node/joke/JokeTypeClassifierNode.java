package com.example.graph.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * 笑话图第一节点：读用户 query，调大模型判断问题类型，写 questionType 为 joke 或 other。
 */
public class JokeTypeClassifierNode implements NodeAction {

    private static final PromptTemplate PROMPT = new PromptTemplate(
            "用户问题：{query}\n请只回答一个词：joke 或 other。joke 表示用户想听笑话，other 表示其他类型。不要解释，只输出这一个词。");

    private final ChatClient chatClient;

    public JokeTypeClassifierNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        String raw = chatClient.prompt()
                .user(u -> u.text(PROMPT.getTemplate()).param("query", query))
                .call()
                .content();
        String questionType = (raw != null && raw.trim().toLowerCase().startsWith("joke")) ? "joke" : "other";
        return Map.of("questionType", questionType);
    }
}
