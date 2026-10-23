package com.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
//    @Bean(name = "dashScopeChatClient")
    public ChatClient c1(DashScopeChatModel m) {
        return ChatClient.create(m);
    }
}