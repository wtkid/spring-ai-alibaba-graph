package com.example.graph.config;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.graph.action.JokeTypeDispatcher;
import com.example.graph.node.joke.JokeBranchNode;
import com.example.graph.node.joke.JokeTypeClassifierNode;
import com.example.graph.node.joke.OtherBranchNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 笑话图：START → classify（大模型判断 joke/other）→ 条件边 → joke 分支（大模型讲笑话，流式）/ other 分支（固定文案，流式）→ END。
 * 状态键：query、questionType、streamOutput、result。
 */
@Configuration
public class JokeGraphConfig {

    @Bean
    public StateGraph jokeGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("query", new ReplaceStrategy());
            state.registerKeyAndStrategy("questionType", new ReplaceStrategy());
            state.registerKeyAndStrategy("streamOutput", new ReplaceStrategy());
            state.registerKeyAndStrategy("result", new ReplaceStrategy());
            return state;
        };
        JokeTypeDispatcher dispatcher = new JokeTypeDispatcher();
        return new StateGraph("JokeGraph", stateFactory)
                .addNode("classify", node_async(new JokeTypeClassifierNode(chatClientBuilder)))
                .addNode("jokeBranch", node_async(new JokeBranchNode(chatClientBuilder)))
                .addNode("otherBranch", node_async(new OtherBranchNode()))
                .addEdge(StateGraph.START, "classify")
                .addConditionalEdges("classify", edge_async(dispatcher),
                        Map.of("joke", "jokeBranch", "other", "otherBranch"))
                .addEdge("jokeBranch", StateGraph.END)
                .addEdge("otherBranch", StateGraph.END);
    }
}
