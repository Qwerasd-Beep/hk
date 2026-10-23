package com.controller;

import com.ai.tool.ZhaoshangQueryTools;
import com.ai.tool.ZhaoshangWriteTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * ToolCalling（函数调用）示例：
 * 把招商平台查询/写操作工具交给大模型，模型根据用户问题自主决定是否调用、调用哪个、传什么参数，
 * 拿到真实业务数据后再组织自然语言回答。
 */
@RestController
@RequestMapping("/ai")
public class AIController8 {
    @Resource(name = "redisDashScopeClient")
    private ChatClient redisDashScopeClient;

    @Resource
    private ZhaoshangWriteTools zhaoshangWriteTools;



    @Resource(name = "dashScopeChatClient")
    private ChatClient chatClient;

    @Resource
    private ZhaoshangQueryTools zhaoshangQueryTools;


    /** 工具使用约束：实测 qwen-turbo 不约束时会编造数据或只做"口头计划"不真正调用 */
    private static final String TOOL_SYSTEM_PROMPT = """
							你是招商服务平台的数据查询助手。规则：
							1. 凡涉及系统中的企业、项目、中介机构、审批、字典等真实业务数据，必须调用提供的工具获取，禁止凭自己的知识编造。
							2. 不要描述你准备调用什么工具，直接调用工具，拿到结果后再组织回答。
							3. 需要先查列表拿到ID再查详情时，请连续调用多个工具，不要中途向用户反问。
							4. 工具查不到数据时，如实告诉用户没有找到。
							5. 工具返回的企业名称/项目名称与用户指定的明显不符时，绝不是同一个对象，禁止把它当作用户要查的对象回答，应说明未找到。
							6. 查询或操作某个具体企业/项目时，只要用户给出了名称，就必须把名称传给工具的名称参数，禁止不传名称空调用工具。
							""";


    /**
     * ToolCalling 问答（无记忆，只读工具）：模型自主选择并调用查询工具
     * 示例：
     * http://localhost:8080/ai/tool/ask?msg=现在库里有哪些外资企业
     * http://localhost:8080/ai/tool/ask?msg=帮我查一下项目代码为PRJ005的审批进度
     */
    @GetMapping("/tool/ask")
    public Map<String, Object> ask(@RequestParam("msg") String msg) {
        String answer = chatClient.prompt()
                .system(TOOL_SYSTEM_PROMPT)
                .user(msg)
                .tools(zhaoshangQueryTools) // 本次调用可用的工具集，模型自主决定是否调用
                .call()
                .content();
        return Map.of("question", msg, "mode", "ToolCalling", "answer", answer == null ? "" : answer);
    }

    /* 在查询规则基础上追加写操作确认规则（配合会话记忆实现多轮确认） */
    private static final String TOOL_SYSTEM_PROMPT_WITH_WRITE = TOOL_SYSTEM_PROMPT + """
			7. 涉及登记、修改、提交审批等写操作时，第一次调用工具不要传 confirm 参数；
			   拿到工具返回的“待确认”提示后，原样向用户复述操作内容并询问是否确认；
			   用户明确同意（如“确认”“好的”“执行”）后，再以完全相同的参数加 confirm=true 调用同一个工具完成写入。
			8. 用户拒绝或要求修改时，不要执行写操作。
			9. 禁止把工具调用请求以 JSON 文本形式当作回答输出；需要调用工具时直接发起工具调用，用户只应看到自然语言。
			""";

    /**
     * ToolCalling 多轮对话（带 Redis 会话记忆，查询+写操作工具）：
     * 写操作通过 confirm 参数实现二次确认——模型先复述操作内容询问用户，用户同意后才真正写入。
     * 同一个 chatId 内的多轮对话共享记忆。
     * 示例（多轮）：
     * 1. http://localhost:8080/ai/tool/chat?chatId=u1&msg=帮我登记一家美国的新能源企业，叫未来能源，注册资本8000万
     * 2. http://localhost:8080/ai/tool/chat?chatId=u1&msg=确认 */

    @GetMapping("/tool/chat")
    public Map<String, Object> chat(@RequestParam("msg") String msg,
                                    @RequestParam(value = "chatId", defaultValue = "tool-demo") String chatId) {
        String answer = redisDashScopeClient.prompt()
                .system(TOOL_SYSTEM_PROMPT_WITH_WRITE)
                .user(msg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)) // 把会话ID传给记忆组件
                .tools(zhaoshangQueryTools,zhaoshangWriteTools)
                .call()
                .content();
        return Map.of("question", msg, "chatId", chatId, "mode", "ToolCalling+Memory",
                "answer", answer == null ? "" : answer);
    }

}
