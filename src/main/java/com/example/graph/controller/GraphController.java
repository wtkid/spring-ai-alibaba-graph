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
 * 简单 Echo Demo：单节点图，无条件跳转。
 * 接口：GET /graph/echo?query=xxx&threadId=xxx，返回 state 的 data（含 query、result）。
 */
@RestController
@RequestMapping("/graph")
public class GraphController {

    private final CompiledGraph compiledGraph;

    public GraphController(@Qualifier("simpleGraph") StateGraph stateGraph) throws GraphStateException {
        this.compiledGraph = stateGraph.compile();
    }

    /** 传入 query，执行图后返回整图输出的 state（含大模型 result）。 */
    @GetMapping("/echo")
    public Map<String, Object> echo(
            @RequestParam(value = "query", defaultValue = "你好") String query,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> input = new HashMap<>();
        input.put("query", query);
        Optional<OverAllState> out = compiledGraph.invoke(input, config);
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }
}
