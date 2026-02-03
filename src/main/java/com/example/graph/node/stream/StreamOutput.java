package com.example.graph.node.stream;

/**
 * 流式输出通道：由图节点在存在时向页面推送 summary 或 answer 的每个 chunk。
 * Controller 实现并传入 state，不传则节点按非流式写 state。
 */
public interface StreamOutput {

    void sendSummary(String summary);

    void sendChunk(String chunk);

    void complete();

    void completeWithError(Throwable t);
}
