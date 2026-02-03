package com.example.graph.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.example.graph.node.stream.StreamOutput;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 笑话图流式接口：先分类（joke/other），joke 分支大模型讲笑话流式返回，other 分支固定文案流式返回。
 * 接口：GET /joke/chat?query=讲个笑话
 */
@RestController
@RequestMapping("/joke")
public class JokeGraphController {

    private static final long SSE_TIMEOUT = 120_000L;

    private final CompiledGraph jokeGraph;
    private final Executor executor;

    public JokeGraphController(
            @Qualifier("jokeGraph") StateGraph jokeGraph,
            Executor applicationTaskExecutor) throws GraphStateException {
        this.jokeGraph = jokeGraph.compile();
        this.executor = applicationTaskExecutor;
    }

    @GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chat(
            @RequestParam(value = "query", defaultValue = "讲个笑话") String query,
            @RequestParam(value = "threadId", defaultValue = "default") String threadId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        MediaType utf8 = new MediaType("text", "plain", StandardCharsets.UTF_8);

        StreamOutput streamOutput = new StreamOutput() {
            @Override
            public void sendSummary(String summary) {
                try {
                    emitter.send(SseEmitter.event().name("summary").data(summary, utf8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void sendChunk(String chunk) {
                try {
                    emitter.send(SseEmitter.event().name("result").data(chunk, utf8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void complete() {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }

            @Override
            public void completeWithError(Throwable t) {
                emitter.completeWithError(t);
            }
        };

        executor.execute(() -> {
            try {
                RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
                Map<String, Object> input = new HashMap<>();
                input.put("query", query);
                input.put("streamOutput", streamOutput);
                jokeGraph.invoke(input, config);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}
