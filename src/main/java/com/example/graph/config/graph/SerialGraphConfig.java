package com.example.graph.config.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.serial.SerialStep1Node;
import com.example.graph.node.serial.SerialStep2Node;
import com.example.graph.node.serial.SerialStep3Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 多 State 串联 Demo 的 Graph 配置：START → step1 → step2 → step3 → END。
 * 状态键：input → step1Out → step2Out → result，依次在节点间传递。
 */
@Configuration
public class SerialGraphConfig {

    /** 定义串联图：三个节点顺序执行。 */
    @Bean
    public StateGraph serialGraph() throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("input", new ReplaceStrategy());
            state.registerKeyAndStrategy("step1Out", new ReplaceStrategy());
            state.registerKeyAndStrategy("step2Out", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        return new StateGraph("SerialGraph", stateFactory)
                .addNode("step1", node_async(new SerialStep1Node()))
                .addNode("step2", node_async(new SerialStep2Node()))
                .addNode("step3", node_async(new SerialStep3Node()))
                .addEdge(StateGraph.START, "step1")
                .addEdge("step1", "step2")
                .addEdge("step2", "step3")
                .addEdge("step3", StateGraph.END);
    }
}
