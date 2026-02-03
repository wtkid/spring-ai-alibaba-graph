package com.example.graph.node.joke;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.graph.node.stream.StreamOutput;

import java.util.Map;
import java.util.Optional;

/**
 * 其他分支：不调大模型，直接通过流式通道告诉用户「只会讲笑话」，以兼容 joke 分支的流式返回。
 */
public class OtherBranchNode implements NodeAction {

    private static final String MESSAGE = "我只会讲笑话，请发一个想听笑话的请求哦。";

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        Optional<StreamOutput> streamOutput = state.value("streamOutput")
                .filter(StreamOutput.class::isInstance)
                .map(StreamOutput.class::cast);
        if (streamOutput.isPresent()) {
            StreamOutput so = streamOutput.get();
            so.sendChunk(MESSAGE);
            so.complete();
            return Map.of("result", MESSAGE);
        }
        return Map.of("result", MESSAGE);
    }
}
