package com.example.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

/**
 * 条件跳转图里的分类节点：从 state 读 input，按规则写 category。
 * 规则：输入包含「正」或「positive」则为 positive，否则为 negative；供后续 EdgeAction 路由。
 */
public class ClassifierNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input", "");
        String category = (input != null && (input.contains("正") || input.contains("positive")))
                ? "positive"
                : "negative";
        return Map.of("category", category);
    }
}
