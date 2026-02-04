package com.example.graph.config.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.memory.MemoryChatNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 会话记忆 Demo 的 Graph 配置：START → chat → END。
 * 使用 Spring AI 官方的 ChatMemory + MessageChatMemoryAdvisor，按 threadId（CONVERSATION_ID）隔离会话。
 * 状态键：query（当前问题）、threadId（会话 id，供 Advisor 取/存记忆）、result（回复）。
 */
@Configuration
public class MemoryGraphConfig {

    /**
     * 带会话记忆的 ChatClient：默认挂载 MessageChatMemoryAdvisor，调用时通过 advisors 传入 CONVERSATION_ID 即可按会话存取历史。
     */
    @Bean
    public ChatClient memoryChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /** 定义单节点图：chat 节点内用 memoryChatClient + threadId 调用，历史由官方 Advisor 自动注入与保存。 */
    @Bean
    public StateGraph memoryGraph(ChatClient memoryChatClient) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("query", new ReplaceStrategy());
            state.registerKeyAndStrategy("threadId", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("MemoryGraph", stateFactory)
                .addNode("chat", node_async(new MemoryChatNode(memoryChatClient)))
                .addEdge(StateGraph.START, "chat")
                .addEdge("chat", StateGraph.END);
    }
}
