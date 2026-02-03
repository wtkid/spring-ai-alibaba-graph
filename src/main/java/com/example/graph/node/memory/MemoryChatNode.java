package com.example.graph.node.memory;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.Map;

/**
 * 会话记忆图里的聊天节点：使用 Spring AI 官方的带 MessageChatMemoryAdvisor 的 ChatClient。
 * 从 state 读 query、threadId，调用时通过 advisors 传入 CONVERSATION_ID=threadId，历史由 Advisor 自动注入与保存，回复写入 result。
 */
public class MemoryChatNode implements NodeAction {

    private final ChatClient chatClient;

    public MemoryChatNode(ChatClient memoryChatClient) {
        this.chatClient = memoryChatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        String threadId = state.value("threadId", "default");
        String result = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, threadId))
                .user(query)
                .call()
                .content();
        return Map.of("result", result != null ? result : "");
    }
}
