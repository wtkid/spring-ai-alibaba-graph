package com.example.graph.node.serial;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/** 串联图第二步：读 step1Out，写 step2Out = step1Out + " -> [step2]"。 */
public class SerialStep2Node implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String step1Out = state.value("step1Out", "");
        return Map.of("step2Out", step1Out + " -> [step2]");
    }
}
