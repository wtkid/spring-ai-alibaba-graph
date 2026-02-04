package com.example.graph.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.EchoNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 简单 Echo Demo 的 Graph 配置：单节点 START → echo → END。
 * 状态键：query（入参）、result（大模型回复）。
 */
@Configuration
public class GraphConfig {

    /** 定义单节点图：echo 节点读 query、调大模型、写 result。 */
    @Bean
    public StateGraph simpleGraph(ChatClient chatClient) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("query", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("SimpleEchoGraph", stateFactory)
                .addNode("echo", node_async(new EchoNode(chatClient)))
                .addEdge(StateGraph.START, "echo")
                .addEdge("echo", StateGraph.END);
    }
}
