package com.example.graph.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * 条件跳转图里的边动作：根据 state 中的 category 决定下一跳节点名。
 * 返回 "positive" 则走 positiveBranch，返回 "negative" 则走 negativeBranch。
 */
public class CategoryDispatcher implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String category = state.value("category", "negative");
        return "positive".equalsIgnoreCase(category) ? "positive" : "negative";
    }
}
