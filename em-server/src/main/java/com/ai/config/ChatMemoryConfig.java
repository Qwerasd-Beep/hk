package com.ai.config;

import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {
    //内存仓库,会话记忆存在内存中,重启丢失,这个类是内置的,不用引其他依赖
    @Bean
    public InMemoryChatMemoryRepository memoryRepo (){
        return new InMemoryChatMemoryRepository();
    }

    //滑动窗口记忆,最多保留10条记录
    @Bean
    public MessageWindowChatMemory chatMemoryWindow(InMemoryChatMemoryRepository repo) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repo)
                .maxMessages(10)
                .build();
    }

    //记忆增强,负责读历史,保存对话
    @Bean
    public MessageChatMemoryAdvisor advisor(MessageWindowChatMemory  chatMemoryWindow) {
        return MessageChatMemoryAdvisor.builder(chatMemoryWindow).build();
    }

    // ========== 2、Redis记忆Bean，命名 memoryRedisAdvisor ==========
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.password:}")
    private String password;
    @Value("${spring.data.redis.database}")
    private int database;

    @Bean
    public JedisRedisChatMemoryRepository jedisRedisChatMemoryRepository() {
        return JedisRedisChatMemoryRepository.builder()
                .host(host).port(port)
                .password(password.isBlank()?null:password)
                .database(database)
                .keyPrefix("ai:chat:mem:")  //这东西要不配置有默认的
                .build();
    }

    @Bean("redisChatMemory")
    public MessageWindowChatMemory redisChatMemory(JedisRedisChatMemoryRepository repo) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repo)
                .maxMessages(10).build();
    }

    @Bean("redisAdvisor")
    public MessageChatMemoryAdvisor memoryRedisAdvisor(@Qualifier("redisChatMemory") MessageWindowChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }


}
