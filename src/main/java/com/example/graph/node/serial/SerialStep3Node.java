package com.example.graph.node.serial;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/** 串联图第三步：读 step2Out，写 result = step2Out + " -> [step3] done"。 */
public class SerialStep3Node implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String step2Out = state.value("step2Out", "");
        return Map.of("result", step2Out + " -> [step3] done");
    }
}
