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
 * 条件跳转 Demo：classifier → 条件边 → positiveBranch / negativeBranch。
 * 接口：GET /conditional/branch?input=xxx&threadId=xxx，返回 input、category、branchResult。
 */
@RestController
@RequestMapping("/conditional")
public class ConditionalGraphController {

    private final CompiledGraph compiledGraph;

    public ConditionalGraphController(@Qualifier("conditionalGraph") StateGraph stateGraph) throws GraphStateException {
        this.compiledGraph = stateGraph.compile();
    }

    /** 传入 input，按内容分类后走正向或负向分支，返回分类结果与分支输出。 */
    @GetMapping("/branch")
    public Map<String, Object> branch(
            @RequestParam(value = "input", defaultValue = "正数") String input,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> payload = new HashMap<>();
        payload.put("input", input);
        Optional<OverAllState> out = compiledGraph.invoke(payload, config);
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }
}
