package com.example.graph.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 公共 ChatClient 配置：提供默认的 ChatClient Bean，供各图与节点直接注入使用。
 * 框架会自动对 {@link ChatClient.Builder} 应用 {@link ChatClientLoggingConfig} 等 Customizer。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

	/**
	 * 大模型调用请求/响应详细日志，用于排障。
	 * <ul>
	 *   <li>通过 {@link SimpleLoggerAdvisor} 在每次 ChatClient 调用前后打印请求与响应（DEBUG 级别）。</li>
	 *   <li>需在 application.yml 中设置 {@code logging.level.org.springframework.ai.chat.client.advisor=DEBUG} 才能看到输出。</li>
	 *   <li>生产环境建议关闭：{@code spring.ai.chat.client.logging.enabled=false}</li>
	 * </ul>
	 */
	@Bean
	@ConditionalOnProperty(name = "spring.ai.chat.client.logging.enabled", havingValue = "true", matchIfMissing = true)
	public ChatClientCustomizer chatClientLoggingCustomizer() {
		return (builder) -> builder.defaultAdvisors(new SimpleLoggerAdvisor());
	}

}
