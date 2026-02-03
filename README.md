# Spring AI Alibaba Graph 最简 Demo

基于 Spring AI Alibaba Graph 的极简示例：Java 21 + Gradle，单节点图（START → echo → END），通过 OpenAI 调用大模型。

## 环境

- JDK 21
- Gradle 8.x（或使用 IDE 自带的 Gradle）

## 配置

在 `application.yml` 中设置 OpenAI API Key，或设置环境变量：

```bash
export OPENAI_API_KEY=your-api-key
```

## 运行

```bash
gradle bootRun
```

或使用 IDE 运行 `GraphDemoApplication`。

## 调用

```bash
curl "http://localhost:8080/graph/echo?query=你好"
```

返回示例：`{"query":"你好","result":"大模型回复内容"}`
