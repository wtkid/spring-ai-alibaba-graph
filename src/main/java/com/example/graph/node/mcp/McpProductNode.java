package com.example.graph.node.mcp;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Map;

/**
 * MCP 商品查询节点：从 state 读用户一句话 message，通过 MCP 工具调大模型完成查询，写回 result。
 */
public class McpProductNode implements NodeAction {

    private static final Logger log = LoggerFactory.getLogger(McpProductNode.class);

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public McpProductNode(ChatClient chatClient, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String message = state.value("message", "");
        log.info("[MCP节点] 进入节点, message={}", message);
        log.info("[MCP节点] 即将调用 ChatClient（大模型 + MCP 工具）");
        String result = chatClient.prompt()
                .system("""
        Answer in Chinese. USE database demo only. All product queries must use table demo.products.
        Example: select * from demo.products where ...;  or  select * from demo.products where ...;""")
                .user(message)
                .toolCallbacks(mcpTools)
                .call()
                .content();
        log.info("[MCP节点] ChatClient 返回完成, result={}", result);
        return Map.of("result", result != null ? result : "");
    }
}
