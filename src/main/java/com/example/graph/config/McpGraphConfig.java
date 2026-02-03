package com.example.graph.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.mcp.McpProductNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * MCP Demo 的 Graph 配置：START → mcpProduct → END。
 * 状态键：message（用户一句话入参）、result（通过 MCP 工具查商品后的回复）。
 */
@Configuration
public class McpGraphConfig {

    @Bean
    public StateGraph mcpGraph(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("message", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("McpGraph", stateFactory)
                .addNode("mcpProduct", node_async(new McpProductNode(chatClientBuilder, mcpTools)))
                .addEdge(StateGraph.START, "mcpProduct")
                .addEdge("mcpProduct", StateGraph.END);
    }
}
