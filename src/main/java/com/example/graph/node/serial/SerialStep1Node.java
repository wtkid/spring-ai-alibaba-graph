package com.example.graph.node.serial;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/** 串联图第一步：读 input，写 step1Out = "[step1] " + input。 */
public class SerialStep1Node implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input", "");
        return Map.of("step1Out", "[step1] " + input);
    }
}
