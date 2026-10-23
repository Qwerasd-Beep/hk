package com.ai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {
    @Bean(name = "dashScopeChatClient")
    public ChatClient dashScope(DashScopeChatModel m) {
        return ChatClient.create(m);
    }

    @Bean(name = "ollamaChatClient")
    public ChatClient ollama(OllamaChatModel m) {
        return ChatClient.create(m);
    }

    //这是带记忆功能的
    @Bean(name = "memoryDashScopeClient")
    public ChatClient memoryDashScope(DashScopeChatModel m,  MessageChatMemoryAdvisor advisor) {
        // 1.1.2.0 版本要这样写
        return ChatClient.builder(m)
                .defaultAdvisors(advisor)  // 设置全局Advisor，所有请求自动带上记忆
                .build();
    }

    @Bean(name = "redisDashScopeClient")                     //注意这里要指明注谁
    public ChatClient redisDashScope(DashScopeChatModel m,@Qualifier("redisAdvisor")  MessageChatMemoryAdvisor advisor) {
        return ChatClient.builder(m)
                .defaultAdvisors(advisor)
                .build();
    }

    /**
     * MCP Client 演示（无记忆版）：工具在 AIController9 中按请求解析——
     * 本应用作为 MCP 客户端懒连接自己的 MCP Server（SSE 回环），工具执行走完整网络闭环。
     * 注意：不能用 spring.ai.mcp.client.toolcallback.enabled=true 的自动 Bean，
     * 否则会被 Server 自动装配再次注册成同名工具。
     */
    @Bean(name = "mcpAskDashScopeClient")
    public ChatClient mcpAskDashScope(DashScopeChatModel m) {
        return ChatClient.create(m);
    }

    /** MCP Client 演示（带 Redis 会话记忆版）：支持写操作两轮确认的多轮对话 */
    @Bean(name = "mcpDashScopeClient")
    public ChatClient mcpDashScope(DashScopeChatModel m,
                                   @Qualifier("redisAdvisor") MessageChatMemoryAdvisor advisor) {
        return ChatClient.builder(m)
                .defaultAdvisors(advisor)
                .build();
    }



}
