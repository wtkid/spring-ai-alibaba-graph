package com.example.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 会话记忆 Demo：使用 Spring AI 官方的 ChatMemory（MessageChatMemoryAdvisor），同一 threadId 下多轮对话带历史。
 * 图内通过 CONVERSATION_ID=threadId 由 Advisor 自动取/存记忆；清空使用官方 ChatMemoryRepository.deleteByConversationId。
 * 接口：GET /memory/chat、GET /memory/clear。
 */
@RestController
@RequestMapping("/memory")
public class MemoryGraphController {

    private final CompiledGraph compiledGraph;
    private final ChatMemoryRepository chatMemoryRepository;

    public MemoryGraphController(
            @Qualifier("memoryGraph") StateGraph stateGraph,
            ChatMemoryRepository chatMemoryRepository) throws GraphStateException {
        this.compiledGraph = stateGraph.compile();
        this.chatMemoryRepository = chatMemoryRepository;
    }

    /**
     * 带记忆的对话：将 query、threadId 放入 state 执行图，Advisor 按 threadId 自动注入历史并保存本轮。
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam(value = "query", defaultValue = "你好") String query,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> input = new HashMap<>();
        input.put("query", query);
        input.put("threadId", threadId);
        Optional<OverAllState> out = compiledGraph.invoke(input, config);
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }

    /** 清空指定 threadId 的会话历史（使用官方 ChatMemoryRepository）。 */
    @GetMapping("/clear")
    public Map<String, Object> clear(@RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        chatMemoryRepository.deleteByConversationId(threadId);
        return Map.of("ok", true, "message", "已清空会话 " + threadId);
    }
}
