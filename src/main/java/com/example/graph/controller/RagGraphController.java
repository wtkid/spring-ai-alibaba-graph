package com.example.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * RAG Demo：官方 TokenTextSplitter 分块 + VectorStoreDocumentRetriever 检索 → generate。
 * 接口：GET /rag/query?query=xxx&threadId=xxx，返回 query、retrievedContext、result。
 */
@RestController
@RequestMapping("/rag")
public class RagGraphController {

    private final CompiledGraph compiledGraph;

    public RagGraphController(@Qualifier("ragGraph") StateGraph ragGraph) throws GraphStateException {
        this.compiledGraph = ragGraph.compile();
    }

    /** 官方 RAG：检索文档后生成回答。 */
    @GetMapping("/query")
    public Map<String, Object> query(
            @RequestParam(value = "query", defaultValue = "什么是 RAG？") String query,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> input = new HashMap<>();
        input.put("query", query);
        Optional<OverAllState> out = compiledGraph.invoke(input, config);
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }
}
