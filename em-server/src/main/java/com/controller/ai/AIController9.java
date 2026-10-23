package com.controller.ai;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MCP Client 演示：本应用的 AI 通过 MCP 协议（SSE 回环）调用本应用 MCP Server 暴露的工具，
 * 展示完整闭环：ChatClient → MCP Client → HTTP/SSE → MCP Server → 工具执行 → 结果返回。
 * 与 AIController8 的区别：工具不是进程内 @Tool 对象，而是通过 MCP 协议远程发现的。
 *
 * 懒连接说明：容器启动阶段 Tomcat 尚未监听 8080，若在启动时 initialize 会 ConnectException，
 * 因此配置 spring.ai.mcp.client.initialized=false，首次请求时才真正建立 SSE 连接并发现工具。
 */
@RestController
@RequestMapping("/ai")
public class AIController9 {

    @Resource(name = "mcpAskDashScopeClient")
    private ChatClient mcpAskDashScopeClient;

    @Resource(name = "mcpDashScopeClient")
    private ChatClient mcpDashScopeClient;

    /** MCP 客户端自动装配创建的客户端列表（对应 application.properties 的 self 连接） */
    @Resource(name = "mcpSyncClients")
    private List<McpSyncClient> mcpSyncClients;

    /** 懒初始化标记：首次调用时建立与 MCP Server 的连接 */
    private volatile boolean mcpReady = false;

    /**
     * 每次请求解析 MCP 工具（顺带感知服务端工具变化），
     * 首次调用先 initialize 建立 SSE 连接。
     */
    private SyncMcpToolCallbackProvider mcpTools() {
        if (!mcpReady) {
            synchronized (this) {
                if (!mcpReady) {
                    for (McpSyncClient client : mcpSyncClients) {
                        if (!client.isInitialized()) {
                            client.initialize();
                        }
                    }
                    mcpReady = true;
                }
            }
        }
        return new SyncMcpToolCallbackProvider(mcpSyncClients);
    }

    /** MCP 场景的使用约束（同 ToolCalling 的实测经验：qwen 不约束会编造、猜 ID 或只做口头计划） */
    private static final String MCP_SYSTEM_PROMPT = """
						你是招商服务平台的数据查询助手。规则：
						1. 凡涉及系统中的企业、项目、中介机构、审批、字典等真实业务数据，必须调用提供的工具获取，禁止凭自己的知识编造。
						2. 不要描述你准备调用什么工具，直接调用工具，拿到结果后再组织回答。
						3. 工具查不到数据时，如实告诉用户没有找到。
						4. 按名称查企业/项目时，必须先用名称列表查询工具（queryForeignEnterprises / queryLocalProjects）定位到具体记录，
						   再对查到的 id 调详情工具；禁止猜测或编造 id 直接调 getForeignEnterpriseDetail / getLocalProjectDetail。
						5. 查询结果中的名称与用户所说名称明显不符时，禁止当作同一对象回答。
						6. 按名称搜索时，若用户表述带有“公司”“有限公司”等通用后缀，去掉后缀再传给工具（如“环宇能源公司”→“环宇能源”）。
						""";

    private static final String MCP_SYSTEM_PROMPT_WITH_WRITE = MCP_SYSTEM_PROMPT + """
						7. 涉及登记、修改、提交审批等写操作时，第一次调用工具不要传 confirm 参数；
						   拿到工具返回的“待确认”提示后，原样向用户复述操作内容并询问是否确认；
						   用户明确同意（如“确认”“好的”“执行”）后，必须立即以完全相同的参数加 confirm=true 调用同一个工具完成写入。
						8. 用户拒绝或要求修改时，不要执行写操作。
						9. 禁止把工具调用请求以 JSON 文本形式当作回答输出；需要调用工具时直接发起工具调用，用户只应看到自然语言。
						""";

    /**
     * 通过 MCP 工具回答（无记忆）。
     * 示例：http://localhost:8080/ai/mcp/ask?msg=外资企业主要来自哪些国家
     */
    @GetMapping("/mcp/ask")
    public Map<String, Object> ask(@RequestParam("msg") String msg) {
        String answer = mcpAskDashScopeClient.prompt()
                .system(MCP_SYSTEM_PROMPT)
                .user(msg)
                .toolCallbacks(mcpTools())
                .call()
                .content();
        return Map.of("question", msg, "mode", "MCP Client->MCP Server", "answer", answer == null ? "" : answer);
    }

    /**
     * 通过 MCP 工具多轮对话（带 Redis 会话记忆，支持写操作两轮确认）。
     * 示例（多轮）：
     * 1. http://localhost:8080/ai/mcp/chat?chatId=m1&msg=帮我登记一家美国的新能源企业，叫环宇能源，注册资本9000万
     * 2. http://localhost:8080/ai/mcp/chat?chatId=m1&msg=确认
     */
    @GetMapping("/mcp/chat")
    public Map<String, Object> chat(@RequestParam("msg") String msg,
                                    @RequestParam(value = "chatId", defaultValue = "mcp-demo") String chatId) {
        String answer = mcpDashScopeClient.prompt()
                .system(MCP_SYSTEM_PROMPT_WITH_WRITE)
                .user(msg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(mcpTools())
                .call()
                .content();
        return Map.of("question", msg, "chatId", chatId, "mode", "MCP Client->MCP Server+Memory",
                "answer", answer == null ? "" : answer);
    }
}

