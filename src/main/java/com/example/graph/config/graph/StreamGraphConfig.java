package com.example.graph.config.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.stream.StreamAnswerNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 流式返回 Demo 的 Graph 配置：START → answer（大模型回答）→ END。
 * 状态键：query（入参）、threadId、result（最终回答）、streamOutput（流式输出通道，由 Controller 放入 input 传入）。
 */
@Configuration
public class StreamGraphConfig {

    /** 单节点：根据 query 调大模型直接回答。流式时 Controller 将 StreamOutput 放入 input，节点从 state 取出并推送。 */
    @Bean
    public StateGraph streamGraph(ChatClient chatClient) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("query", new ReplaceStrategy());
            state.registerKeyAndStrategy("threadId", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            state.registerKeyAndStrategy("streamOutput", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("StreamGraph", stateFactory)
                .addNode("answer", node_async(new StreamAnswerNode(chatClient)))
                .addEdge(StateGraph.START, "answer")
                .addEdge("answer", StateGraph.END);
    }

}
