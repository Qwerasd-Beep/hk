package com.controller.ai;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class Controller1 {
    @Resource(name = "dashScopeChatClient")
    private ChatClient dashScopeClient;

    @Resource(name="dashScopeChatModel")
    private ChatModel chatModel;

    @Value("classpath:/prompt-templates/my-template.txt")
    private org.springframework.core.io.Resource resourceTxt;


    @RequestMapping("/speak8")
    public Flux<String> speak8(
            @RequestParam("sysTopic") String sysTopic,
            @RequestParam("userTopic") String userTopic,
            @RequestParam("format")String format){
        //系统提示词模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("你是{sysTopic}助手，只回答{sysTopic}其它不许回答，以{format}格式输出");
        Message sysMessage = systemPromptTemplate.createMessage(Map.of("sysTopic", sysTopic,"format",format));

        //用户提示词模板
        PromptTemplate userPromptTemplate = new PromptTemplate("解释一下{userTopic}");
        Message userMessage = userPromptTemplate.createMessage(Map.of("userTopic", userTopic));

        //组合多个 Message
        Prompt prompt = new Prompt(sysMessage, userMessage);

        return dashScopeClient.prompt(prompt)
                .stream()
                .content();
    }


    @RequestMapping("/speak7")
    public Flux<String> speak7(
            @RequestParam("person") String person,
            @RequestParam("format")String format,
            @RequestParam("wordcount")int wordcount) {

        PromptTemplate t=new PromptTemplate(resourceTxt);
        Map<String,Object> paramMap=Map.of("person",person,"format",format,"wordcount",wordcount);
        Prompt prompt=t.create(paramMap);

        return dashScopeClient.prompt(prompt)
                .stream()
                .content();
    }

    @RequestMapping("/speak6")
    public String speak6(@RequestParam("msg") String msg){
        //可以看到,调用后,返回的是 AssistantMessage
        AssistantMessage output = dashScopeClient.prompt()
                .system("你是java讲师,非常凶狠,经常打学生")
                .user(msg)
                .call()
                .chatResponse()
                .getResult()
                .getOutput();

        return  output.getText();
    }


    @RequestMapping("/speak5")
    public Flux<String> speak5(@RequestParam("msg") String msg){
        //系统提示词
        SystemMessage sysMsg=new SystemMessage("你是java讲师,只会java技术,不会做饭");

        //用户提示词
        UserMessage userMsg=new UserMessage(msg);

        Prompt prompt=new Prompt(sysMsg,userMsg);

        Flux<ChatResponse> stream = chatModel.stream(prompt);

        return stream.map(r->r.getResults().get(0).getOutput().getText());
    }



    @RequestMapping("/speak4")
    public Flux<ChatResponse> speak4(@RequestParam("msg") String msg){
        //系统提示词
        SystemMessage sysMsg=new SystemMessage("你是java讲师,只会java技术,不会做饭");

        //用户提示词
        UserMessage userMsg=new UserMessage(msg);

        Prompt prompt=new Prompt(sysMsg,userMsg);

        Flux<ChatResponse> stream = chatModel.stream(prompt);
        return  stream;
    }


    @RequestMapping("/speak3")
    public String speak3(@RequestParam("lan") String lan,@RequestParam("dbName") String dbName) {
        return dashScopeClient.prompt()
                .system("你是后端技术专家,返回html格式,返回的数据长度不要超过10行")
                .user(u->
                        u.text("请解翻译一下在 {q} 语言中,如何使用 {db} 这种数据库")
                                .param("q", lan)
                                .param("db", dbName)
                )
                .call()
                .content();
    }


    @RequestMapping("/speak2")
    public String speak2(@RequestParam("msg") String msg) {

        PromptTemplate t=new PromptTemplate("""
								基于下面的参考上下文回答问题，不知道就说不知道。
								上下文：{context}
								问题：{question}
								""");

        Prompt prompt=t.create(Map.of("context","AQS是Java锁的底层同步队列,Redis8支持向量检索","question",msg));

        ChatResponse resp = chatModel.call(prompt);
        return  resp.getResult().getOutput().getText();

    }


    @RequestMapping("/speak1")
    public String speak(@RequestParam("msg") String msg) {
        //系统提示词
        SystemMessage sysMsg=new SystemMessage("你是java讲师,语言精练,回答准确,返回html格式");

        //用户提示词
        UserMessage userMsg1=new UserMessage(msg);
        UserMessage userMsg2=new UserMessage("再介绍一下 int类型 特点 ");

        Prompt prompt=new Prompt(sysMsg,userMsg1,userMsg2);

        ChatResponse resp = chatModel.call(prompt);
        return  resp.getResult().getOutput().getText();
    }

}
