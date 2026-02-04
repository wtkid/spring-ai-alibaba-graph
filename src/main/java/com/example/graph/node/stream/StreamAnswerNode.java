package com.example.graph.node.stream;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;

/** 流式图节点：读 query，调大模型直接回答；有 streamOutput 时每 chunk 推送，否则写 result。 */
public class StreamAnswerNode implements NodeAction {

    private static final PromptTemplate PROMPT = new PromptTemplate(
            "问题：{query}\n请用一两句话直接回答。");

    private final ChatClient chatClient;

    public StreamAnswerNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        Flux<String> stream = chatClient.prompt()
                .user(u -> u.text(PROMPT.getTemplate()).param("query", query))
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
