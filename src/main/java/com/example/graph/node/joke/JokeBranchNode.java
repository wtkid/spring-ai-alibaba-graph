package com.example.graph.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.graph.node.stream.StreamOutput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

/**
 * 笑话分支：根据用户消息调大模型讲笑话，有 streamOutput 时流式推送每个 chunk。
 */
public class JokeBranchNode implements NodeAction {

    private static final PromptTemplate JOKE_PROMPT = new PromptTemplate(
            "用户说：{query}\n请根据用户的话讲一个简短的冷笑话，直接开始讲，不要前缀。");

    private final ChatClient chatClient;

    public JokeBranchNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        Flux<String> stream = chatClient.prompt()
                .user(u -> u.text(JOKE_PROMPT.getTemplate()).param("query", query))
                .stream()
                .content();
        Optional<StreamOutput> streamOutput = state.value("streamOutput")
                .filter(StreamOutput.class::isInstance)
                .map(StreamOutput.class::cast);
        if (streamOutput.isPresent()) {
            StreamOutput so = streamOutput.get();
            stream.subscribe(so::sendChunk, so::completeWithError, so::complete);
            return Map.of("result", "");
        }
        String result = stream.reduce("", (a, b) -> a + b).block();
        return Map.of("result", result != null ? result : "");
    }
}
