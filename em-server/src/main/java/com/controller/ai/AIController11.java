package com.controller.ai;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Prompts 调用演示（AIController9=tools、AIController10=resources、本类=prompts）。
 *
 * MCP Prompt 是服务端预置的"提示模板"：客户端 prompts/list 发现模板，prompts/get 按参数渲染出
 * 现成消息，再把消息交给自己的大模型执行。本控制器通过 SSE 回环完成整个闭环：
 *
 *   GET /ai/mcp/prompt/list                列出 4 个预置模板及其参数说明
 *   GET /ai/mcp/prompt/get?name=&...       按参数渲染模板，仅返回消息原文（不调大模型、不调工具）
 *   GET /ai/mcp/prompt/run?name=&...       一键执行：渲染模板 → 大模型按模板指令回调 MCP 工具 → 输出报告
 *
 * 示例：
 *   /ai/mcp/prompt/list
 *   /ai/mcp/prompt/get?name=project-approval-review&projectCode=PRJ010
 *   /ai/mcp/prompt/run?name=investment-landscape-analysis&country=美国
 *   /ai/mcp/prompt/run?name=enterprise-profile&enterpriseName=环宇能源
 *   /ai/mcp/prompt/run?name=annual-investment-report&year=2026
 */
@RestController
@RequestMapping("/ai")
public class AIController11 {

    /** 复用 AIController9 的无记忆 ChatClient（底层模型相同），工具按请求动态挂载 */
    @Resource(name = "mcpAskDashScopeClient")
    private ChatClient mcpAskDashScopeClient;

    @Resource(name = "mcpSyncClients")
    private List<McpSyncClient> mcpSyncClients;

    private volatile boolean mcpReady = false;

    /** 首次调用建立 SSE 连接（同 AIController9/10：启动阶段 Tomcat 未监听，必须懒连接） */
    private McpSyncClient client() {
        if (!mcpReady) {
            synchronized (this) {
                if (!mcpReady) {
                    for (McpSyncClient c : mcpSyncClients) {
                        if (!c.isInitialized()) {
                            c.initialize();
                        }
                    }
                    mcpReady = true;
                }
            }
        }
        return mcpSyncClients.get(0);
    }

    /** 执行模板时的系统约束（与 AIController9 同一套实测规则：防编造、防猜 ID） */
    private static final String SYSTEM_PROMPT = """
					你是招商服务平台的报告撰写助手。规则：
					1. 报告中的所有业务数据必须通过调用提供的 MCP 工具获取，禁止凭自身知识编造数字或企业信息。
					2. 不要描述你准备调用什么工具，直接调用工具，取数完成后直接输出报告正文。
					3. 严格按任务指令的步骤分批取数：只要还有指令要求但你尚未调用的工具，就必须继续发起工具调用，
					   严禁在取数未完成时提前撰写报告。可以连续多轮调用工具，不要在第一批工具返回后就收尾。
					4. 报告里出现的每一个企业名称、项目名称、金额、年份都必须能在某一次工具返回结果中找到原文；
					   没有取到的数据一律写"暂未获取到相关数据"，严禁虚构（例如编造"美国A公司""某某产业园"或凭推测填写金额）。
					5. 列表类工具分页返回（每页20条），当 total 大于已拉取条数时必须继续翻页，禁止只看第一页就下结论。
					6. 按名称查企业/项目时，先用名称列表查询定位到具体记录，再按 id 调详情；禁止猜测 id。
					7. 名称带"公司""有限公司"等通用后缀且查询无结果时，去掉后缀重试。
					8. 输出中文正式报告，结构清晰、分节分点，数字与工具返回保持一致。
					""";

    /**
     * prompts/list：发现服务端预置模板。
     */
    @GetMapping("/mcp/prompt/list")
    public Map<String, Object> list() {
        ListPromptsResult result = client().listPrompts();
        List<Map<String, Object>> prompts = new ArrayList<>();
        for (Prompt p : result.prompts()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", p.name());
            item.put("title", p.title());
            item.put("description", p.description());
            List<Map<String, Object>> args = new ArrayList<>();
            for (PromptArgument a : p.arguments()) {
                Map<String, Object> arg = new LinkedHashMap<>();
                arg.put("name", a.name());
                arg.put("title", a.title());
                arg.put("description", a.description());
                arg.put("required", Boolean.TRUE.equals(a.required()));
                args.add(arg);
            }
            item.put("arguments", args);
            prompts.add(item);
        }
        return Map.of("mode", "MCP prompts/list", "count", prompts.size(), "prompts", prompts);
    }

    /**
     * prompts/get：按参数渲染模板原文，不经过大模型，便于客户端查看/二次编辑。
     */
    @GetMapping("/mcp/prompt/get")
    public Map<String, Object> get(@RequestParam("name") String name,
                                   @RequestParam(value = "country", required = false) String country,
                                   @RequestParam(value = "industry", required = false) String industry,
                                   @RequestParam(value = "projectCode", required = false) String projectCode,
                                   @RequestParam(value = "enterpriseName", required = false) String enterpriseName,
                                   @RequestParam(value = "year", required = false) String year) {

        McpSchema.GetPromptResult rendered = client().getPrompt(
                new McpSchema.GetPromptRequest(name, buildArgs(country, industry, projectCode, enterpriseName, year)));

        return Map.of("mode", "MCP prompts/get", "name", name,
                "description", rendered.description() == null ? "" : rendered.description(),
                "messages", describeMessages(rendered));
    }

    /**
     * 一键执行：prompts/get 渲染模板 → 模板消息交给大模型 → 模型回调 MCP 工具取数 → 输出报告。
     */
    @GetMapping("/mcp/prompt/run")
    public Map<String, Object> run(@RequestParam("name") String name,
                                   @RequestParam(value = "country", required = false) String country,
                                   @RequestParam(value = "industry", required = false) String industry,
                                   @RequestParam(value = "projectCode", required = false) String projectCode,
                                   @RequestParam(value = "enterpriseName", required = false) String enterpriseName,
                                   @RequestParam(value = "year", required = false) String year) {

        // 1) 通过 MCP 协议获取服务端渲染好的模板消息
        GetPromptResult rendered = client().getPrompt(
                new GetPromptRequest(name, buildArgs(country, industry, projectCode, enterpriseName, year)));
        String instruction = firstText(rendered);

        // 2) 交给大模型执行，挂载同一 MCP Server 的 21 个工具供其按模板指令回调
        String answer = mcpAskDashScopeClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(instruction)
                .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .call()
                .content();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("mode", "MCP prompts/get -> AI -> MCP tools/call");
        resp.put("promptName", name);
        resp.put("templateDescription", rendered.description() == null ? "" : rendered.description());
        resp.put("answer", answer == null ? "" : answer);
        return resp;
    }

    // ==================== 辅助方法 ====================

    /** 把 5 个模板参数中非空的装入 Map（MCP 要求 arguments 为 Map<String,Object>） */
    private Map<String, Object> buildArgs(String country, String industry, String projectCode,
                                          String enterpriseName, String year) {
        Map<String, Object> args = new LinkedHashMap<>();
        putIfPresent(args, "country", country);
        putIfPresent(args, "industry", industry);
        putIfPresent(args, "projectCode", projectCode);
        putIfPresent(args, "enterpriseName", enterpriseName);
        putIfPresent(args, "year", year);
        return args;
    }

    private void putIfPresent(Map<String, Object> args, String key, String value) {
        if (value != null && !value.isBlank()) {
            args.put(key, value.trim());
        }
    }

    private List<Map<String, Object>> describeMessages(GetPromptResult result) {
        List<Map<String, Object>> messages = new ArrayList<>();
        for (PromptMessage m : result.messages()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("role", m.role() == null ? "" : m.role().name().toLowerCase());
            Content content = m.content();
            if (content instanceof TextContent text) {
                item.put("type", "text");
                item.put("text", text.text() == null ? "" : text.text());
            }
            else {
                item.put("type", content == null ? "unknown" : content.getClass().getSimpleName());
            }
            messages.add(item);
        }
        return messages;
    }

    /** 取模板中第一条文本消息作为给大模型的指令 */
    private String firstText(GetPromptResult result) {
        for (PromptMessage m : result.messages()) {
            if (m.content() instanceof TextContent text && text.text() != null) {
                return text.text();
            }
        }
        return "";
    }
}
