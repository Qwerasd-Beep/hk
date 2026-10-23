package com.controller.ai;

import com.entity.StudentInfo;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class Controller2 {
    @Resource(name = "dashScopeChatClient")
    private ChatClient dashScopeClient;

    @GetMapping("/talk1")
    public StudentInfo test1(@RequestParam("idCard") String idCard, @RequestParam("stuName") String stuName) {
        return dashScopeClient.prompt()
                .user(spec->{
                    spec.text("我的主键是一个随机数 ,身份证号{idCardXXX}，我的名字是{stuNameXXX},帮我随便生成个学校名,简单介绍一下java语言")
                            .param("idCardXXX", idCard)
                            .param("stuNameXXX", stuName);
                })
                .call()
                .entity(StudentInfo.class);

    }

    @GetMapping("/talk2")
    public StudentInfo test2(
            @RequestParam("idCard") String idCard,
            @RequestParam("stuName") String stuName
    ){

        return dashScopeClient
                .prompt()
                // 开启模型端原生结构化输出，服务端强制校验schema，减少key写错
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .user(spec -> {
                    spec.text(
                                    """
                                    根据下面学生信息，填充学生对象，只输出JSON，不要输出任何解释文字  //注意这里，提示词改了
                                    主键为 随机整数
                                    身份证号：{idCard}
                                    姓名：{stuName}
                                    学校只要不是阳光中学就行
                                    """ )
                            .param("idCard",idCard)
                            .param("stuName",stuName);
                })
                .call()
                .entity(StudentInfo.class);
    }

    //调试手段（排错神器）
    @GetMapping("/talk3")
    public StudentInfo talk3(@RequestParam("idCard") String idCard, @RequestParam("stuName") String stuName) {

        ChatClient.CallResponseSpec callSpec = dashScopeClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT).user(spec -> {
                    spec.text("""
											根据下面学生信息，填充学生对象，只输出JSON，不要输出任何解释文字
                      主键为 随机整数
                      身份证号：{idCard}
                      stuName：{stuName}
                      学校只要不是阳光中学就行
                      """).param("idCard", idCard).param("stuName", stuName);
                }).call();

        ChatResponse resp = callSpec.chatResponse();
        String rawText = resp.getResult().getOutput().getText();

        System.out.println("大模型原始输出:\n" + rawText);

        // 手动转换，复现entity底层逻辑
        BeanOutputConverter<StudentInfo> converter = new BeanOutputConverter<>(StudentInfo.class);
        StudentInfo stu = converter.convert(rawText);
        return stu;
    }

}
