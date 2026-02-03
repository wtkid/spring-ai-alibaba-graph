package com.example.graph.node.mcp;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Map;

/**
 * MCP 商品查询节点：从 state 读 productId，通过 MCP 工具调大模型查商品，写回 result。
 */
public class McpProductNode implements NodeAction {

    private static final Logger log = LoggerFactory.getLogger(McpProductNode.class);

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public McpProductNode(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClientBuilder.build();
        this.mcpTools = mcpTools;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String productId = state.value("productId", "");
        log.info("[MCP节点] 进入节点, productId={}", productId);
        log.info("[MCP节点] 即将调用 ChatClient（大模型 + MCP 工具），可能卡在：1.连大模型 2.大模型返回工具调用 3.执行 MCP 工具 4.工具结果回传大模型");
        String result = chatClient.prompt()
                .user("请从数据库 demo.products 表中，查询商品 " + productId + " 的详细信息。")
                .toolCallbacks(mcpTools)
                .call()
                .content();
        log.info("[MCP节点] ChatClient 返回完成, result={}", result);
        return Map.of("result", result != null ? result : "");
    }
}
