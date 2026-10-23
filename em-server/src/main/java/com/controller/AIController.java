package com.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {
    @Resource(name = "dashScopeChatClient")
    private ChatClient dashScopeClient;

    @Resource(name = "ollamaChatClient")
    private  ChatClient ollamaClient;

    @Resource(name="dashScopeChatModel")
    private ChatModel chatModel;

    @GetMapping("/dashscope-ask")
    public Flux<String> dashscopeAsk(@RequestParam("msg") String msg) {
        return dashScopeClient.prompt()
                .system("你是Java高级讲师，不爱吃鱼")
                .user(msg)
                .stream()
                .content();
    }

    @GetMapping("/ollama-ask")
    public Flux<String> ollamaAsk(@RequestParam("msg") String msg) {
        return ollamaClient.prompt()
                .system("你是中国男足，喜欢吃海参和踢足球")
                .user(msg)
                .stream()
                .content();
    }
    //能处理post请求
    @PostMapping("/dashscope-postask")
    public Flux<String> dashscopePostAsk(@RequestBody Map<String,String> map) {
        map.forEach((k,v)->{
            System.out.println(k+":"+v);
        });

        return dashScopeClient.prompt()
                .system("你是中国男足，喜欢吃海参和踢足球")
                .user(map.get("msg"))
                .stream()
                .content();
    }


}
