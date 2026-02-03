package com.example.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP Demo：通过图 + MCP（SSE）从远端服务查询商品信息，入参为用户一句话。
 * 图：START → mcpProduct → END。需在 application.yml 配置 MCP SSE 地址。
 */
@RestController
@RequestMapping("/mcp")
public class McpProductController {

    private static final Logger log = LoggerFactory.getLogger(McpProductController.class);

    private final CompiledGraph compiledGraph;

    public McpProductController(@Qualifier("mcpGraph") StateGraph mcpGraph) throws GraphStateException {
        this.compiledGraph = mcpGraph.compile();
    }

    /** 入参为用户一句话，如：帮我查询一下数据库名字为HUAWEI的商品的价格、帮我查询一下数据库id=1的商品的价格 */
    @GetMapping("/product")
    public Map<String, Object> product(
            @RequestParam(value = "message") String message,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        log.info("[MCP] 请求进入: message={}, threadId={}", message, threadId);
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> input = new HashMap<>();
        input.put("message", message);
        log.info("[MCP] 开始执行图 invoke");
        Optional<OverAllState> out = compiledGraph.invoke(input, config);
        log.info("[MCP] 图 invoke 结束, 有结果={}", out.isPresent());
        return out.map(OverAllState::data).orElse(new HashMap<>());
    }
}
