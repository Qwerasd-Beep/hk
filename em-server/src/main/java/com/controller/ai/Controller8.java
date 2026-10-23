package com.controller.ai;

import com.ai.tool.EmTools;
import com.ai.tool.McpTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Controller8 {
    @Resource(name = "dashScopeChatClient")
    private ChatClient chatClient;

    @Resource
    private EmTools emTools;

    @Resource
    private McpTools mcpTools;

    /** 工具使用约束：实测 qwen-turbo 不约束时会编造数据或只做"口头计划"不真正调用 */
    String TOOL_SYSTEM_PROMPT = """
				你可以调用工具获取真实数据，**严禁使用你内部知识库编造时间、省份、企业信息，必须优先调用工具**。
				工具列表：
				  getTime：无参，获取服务器当前时间，用户询问时间相关问题，必须调用该工具。
				  getCityProvince(cityName)：传入城市名查省份；工具返回“未查到,请去百度查”时原样回复，禁止猜测省份。
				  queryForeignEnterprises(id)：传入企业ID字符串查询企业名称；查不到数据如实告知用户。
				
					规则：
					1. 只要问题属于工具可解决范围，**强制调用工具，禁止直接回答**。
					2. 准确提取用户给出的城市名、企业ID作为工具参数，不要修改原文。
					3. 如果缺少工具必需参数，请向用户反问索要参数，不要瞎填参数调用工具。
					4. 根据工具返回结果整理自然中文输出，不要暴露工具名、函数、底层协议。
					5. 工具返回空、未找到，如实告知用户，禁止虚构答案。
					6. 不属于工具范畴的普通问题，直接正常回答。
					7. 一个问题需要多个工具时，可以依次调用多个工具。
		         """;


    @GetMapping("/tool/ask")
    public Map<String, Object> ask(@RequestParam("msg") String msg) {
        String answer = chatClient.prompt()
                .system(TOOL_SYSTEM_PROMPT)
                .user(msg)
                .tools(emTools,mcpTools) // 本次调用可用的工具集，模型自主决定是否调用
                .call()
                .content();
        return Map.of("question", msg, "mode", "ToolCalling", "answer", answer == null ? "" : answer);
    }
}
