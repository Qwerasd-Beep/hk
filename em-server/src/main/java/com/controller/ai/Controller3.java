package com.controller.ai;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class Controller3 {
    @Resource(name="dashScopeChatClient")
    private ChatClient dashScopeClient;

    @Resource(name = "memoryDashScopeClient")
    private ChatClient memoryDashScopeClient; //注进来这个ChatClient

    @Resource(name = "redisDashScopeClient")
    private ChatClient redisDashScopeClient;



    @Resource(name = "advisor")
    private MessageChatMemoryAdvisor memoryAdvisor;

    @GetMapping("/say1")
    public Flux<String> say1(@RequestParam("msg") String msg, @RequestParam("chatId") String chatId) {
        return dashScopeClient.prompt()
                .system("你是java讲师,语言简洁")
                .user(msg)
                .advisors(memoryAdvisor) //把 Advisor对象添加到请求中,告诉 chatClien ,本次会话要进行记忆
                .advisors(spec->spec.param(ChatMemory.CONVERSATION_ID, chatId))  //给已经添加的 Advisor 对象添加参数
                .stream()
                .content();
    }


    @GetMapping("/say2")
    public Flux<String> say2(@RequestParam("msg") String msg, @RequestParam("chatId") String chatId) {
        return dashScopeClient.prompt()
                .system("你是java讲师,语言简洁")
                .user(msg)

                //把上例的写法稍改了一小下
                .advisors(spec->{
                    spec.advisors(memoryAdvisor);
                    spec.param(ChatMemory.CONVERSATION_ID, chatId);
                })

                .stream()
                .content();
    }

    @GetMapping("/say3")
    public Flux<String> say3(@RequestParam("msg") String msg,
                             @RequestParam("chatId") String chatId) {
        return memoryDashScopeClient.prompt()
                .system("你是java讲师,精通数学和java语言,别说废话,直接给出答案")
                .user(msg)

                // 全局Advisor已经生效，只传递会话ID参数就可以了
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @GetMapping("/say4")
    public Flux<String> say4(@RequestParam("msg") String msg,
                             @RequestParam("chatId") String chatId) {
        return redisDashScopeClient.prompt()
                .system("你是java讲师,精通数学和java语言,别说废话,直接给出答案")
                .user(msg)
                // 全局Advisor已经生效，只传递会话ID参数
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }



}
