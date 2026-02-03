package com.example.graph.action;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * 笑话图条件边：根据 state 中的 questionType 决定下一跳节点。
 * joke → jokeBranch，other → otherBranch。
 */
public class JokeTypeDispatcher implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String questionType = state.value("questionType", "other");
        return "joke".equalsIgnoreCase(questionType) ? "joke" : "other";
    }
}
