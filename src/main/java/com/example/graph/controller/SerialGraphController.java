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
 * 多 State 串联 Demo：START → step1 → step2 → step3 → END。
 * 接口：GET /serial/run?input=xxx&threadId=xxx，返回 input、step1Out、step2Out、result。
 */
@RestController
@RequestMapping("/serial")
public class SerialGraphController {

    private final CompiledGraph compiledGraph;

    public SerialGraphController(@Qualifier("serialGraph") StateGraph stateGraph) throws GraphStateException {
        this.compiledGraph = stateGraph.compile();
    }

    /** 传入 input，依次经过三个节点，返回各阶段输出。 */
    @GetMapping("/run")
    public Map<String, Object> run(
            @RequestParam(value = "input", defaultValue = "hello") String input,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> payload = new HashMap<>();
        payload.put("input", input);
        Optional<OverAllState> out = compiledGraph.invoke(payload, config);
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }
}
