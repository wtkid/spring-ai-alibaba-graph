package com.example.graph.config.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.node.ClassifierNode;
import com.example.graph.node.NegativeBranchNode;
import com.example.graph.node.PositiveBranchNode;
import com.example.graph.action.CategoryDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 条件跳转 Demo 的 Graph 配置：classifier → 条件边 → positiveBranch / negativeBranch。
 * 状态键：input（入参）、category（分类结果）、branchResult（分支输出）。
 */
@Configuration
public class ConditionalGraphConfig {

    /** 定义带条件边的图：分类节点写 category，EdgeAction 根据 category 决定走正向或负向分支。 */
    @Bean
    public StateGraph conditionalGraph() throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("input", new ReplaceStrategy());
            state.registerKeyAndStrategy("category", new ReplaceStrategy());
            state.registerKeyAndStrategy("branchResult", new ReplaceStrategy());
            return state;
        };
        CategoryDispatcher dispatcher = new CategoryDispatcher();
        return new StateGraph("ConditionalBranchGraph", stateFactory)
                .addNode("classifier", node_async(new ClassifierNode()))
                .addNode("positiveBranch", node_async(new PositiveBranchNode()))
                .addNode("negativeBranch", node_async(new NegativeBranchNode()))
                .addEdge(StateGraph.START, "classifier")
                .addConditionalEdges("classifier", edge_async(dispatcher),
                        Map.of("positive", "positiveBranch", "negative", "negativeBranch"))
                .addEdge("positiveBranch", StateGraph.END)
                .addEdge("negativeBranch", StateGraph.END);
    }
}
