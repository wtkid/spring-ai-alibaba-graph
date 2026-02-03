package com.example.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/** 条件跳转图里走「负向」时的分支节点：固定写 branchResult 为「负数/负向分支」。 */
public class NegativeBranchNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        return Map.of("branchResult", "负数/负向分支");
    }
}
