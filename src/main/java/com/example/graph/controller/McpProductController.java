package com.example.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MCP Demo：通过图 + MCP（SSE）从远端服务查询商品信息。
 * 图：START → mcpProduct → END。需在 application.yml 配置 MCP SSE 地址。
 */
@RestController
@RequestMapping("/mcp")
public class McpProductController {

    private static final Logger log = LoggerFactory.getLogger(McpProductController.class);
    private static final long TIMEOUT_SECONDS = 60;

    private final CompiledGraph compiledGraph;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public McpProductController(@Qualifier("mcpGraph") StateGraph mcpGraph) throws GraphStateException {
        this.compiledGraph = mcpGraph.compile();
    }

    /** 通过图调用 MCP 工具查询商品信息，超时时间 60 秒。 */
    @GetMapping("/product")
    public Map<String, Object> product(
            @RequestParam(value = "productId", defaultValue = "1") String productId,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        log.info("[MCP] 请求进入: productId={}, threadId={}", productId, threadId);
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        Map<String, Object> input = new HashMap<>();
        input.put("productId", productId);
        log.info("[MCP] 开始执行图 invoke, 超时 {} 秒", TIMEOUT_SECONDS);
        try {
            Optional<OverAllState> out = executor.submit(() -> compiledGraph.invoke(input, config))
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[MCP] 图 invoke 结束, 有结果={}", out.isPresent());
            return out.map(OverAllState::data).orElse(new HashMap<>());
        } catch (TimeoutException e) {
            log.warn("[MCP] 图执行超时");
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "请求超时");
        } catch (Exception e) {
            log.error("[MCP] 图执行异常", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
