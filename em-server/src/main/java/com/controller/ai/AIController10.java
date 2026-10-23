package com.controller.ai;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.*;
import io.modelcontextprotocol.spec.McpSchema.*;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Resources 调用演示（AIController9 的资源版姊妹篇）。
 *
 * AIController9 演示的是 MCP tools（tools/list、tools/call）；
 * 本控制器演示 MCP resources（resources/list、resources/templates/list、resources/read），
 * 调用全程走 MCP 协议 SSE 回环：本应用 → MCP Client → /sse → MCP Server → 资源处理器。
 *
 * 端点：
 *   GET /ai/mcp/res/list                    列出静态资源和资源模板
 *   GET /ai/mcp/res/read?uri=               读取指定资源原文（不经大模型）
 *   GET /ai/mcp/res/ask?msg=                按问题自动选择资源，读取后交给大模型基于资源回答（无记忆）
 *   GET /ai/mcp/res/chat?chatId=&msg=       同上，带 Redis 会话记忆（多轮追问资源内容）
 *
 * 与 tools 的区别：resources 不会被大模型自动回调，需由程序主动 readResource 拉取后，
 * 把资源文本作为上下文拼进 Prompt——这是 MCP 资源最典型的使用方式。
 */
@RestController
@RequestMapping("/ai")
public class AIController10 {

    /** 无记忆问答：复用最朴素的 ChatClient（不挂任何工具） */
    @Resource(name = "dashScopeChatClient")
    private ChatClient dashScopeChatClient;

    /** 带记忆问答：复用带 Redis 记忆 Advisor 的 ChatClient（会话内多轮追问） */
    @Resource(name = "redisDashScopeClient")
    private ChatClient redisDashScopeClient;

    /** MCP 客户端自动装配的客户端列表（application.properties 中的 self 连接，initialized=false 懒连接） */
    @Resource(name = "mcpSyncClients")
    private List<McpSyncClient> mcpSyncClients;

    private volatile boolean mcpReady = false;

    /** 首次调用时建立与 MCP Server 的 SSE 连接（容器启动阶段 Tomcat 未监听，不能启动即连） */
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

    private static final String KB_URI = "zhaoshang://kb/policy";
    private static final String DASHBOARD_URI = "zhaoshang://stats/dashboard";
    private static final String RECENT_URI = "zhaoshang://enterprises/recent";

    /** 资源增强问答的系统提示：要求只基于拉取到的资源内容回答 */
    private static final String RESOURCE_SYSTEM_PROMPT = """
					你是招商服务平台的资源问答助手。下面提供了通过 MCP 资源接口实时读取的平台资料，
					请严格依据这些【资源内容】回答用户问题：
					1. 答案必须完全基于资源内容，不要编造资源中没有的数据或政策。
					2. 数字、枚举、名称要与资源原文一致，引用统计数字时直接给出。
					3. 资源内容不足以回答时，直接说明“当前读取的资源中没有相关信息”。
					4. 用简洁自然的中文回答，可适当分点，不要输出“根据资源内容”这类机械表述。
					""";

    /**
     * resources/list + resources/templates/list：查看 MCP Server 暴露了哪些可读资源。
     * 示例：http://localhost:8080/ai/mcp/res/list
     */
    @GetMapping("/mcp/res/list")
    public Map<String, Object> list() {
        McpSyncClient c = client();
        McpSchema.ListResourcesResult rr = c.listResources();
        McpSchema.ListResourceTemplatesResult tr = c.listResourceTemplates();

        List<Map<String, Object>> resources = new ArrayList<>();
        for (io.modelcontextprotocol.spec.McpSchema.Resource r : rr.resources()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("uri", r.uri());
            item.put("name", r.name());
            item.put("description", r.description());
            item.put("mimeType", r.mimeType());
            resources.add(item);
        }
        List<Map<String, Object>> templates = new ArrayList<>();
        for (io.modelcontextprotocol.spec.McpSchema.ResourceTemplate t : tr.resourceTemplates()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("uriTemplate", t.uriTemplate());
            item.put("name", t.name());
            item.put("description", t.description());
            item.put("mimeType", t.mimeType());
            templates.add(item);
        }
        return Map.of("mode", "MCP resources/list", "resources", resources, "resourceTemplates", templates);
    }

    /**
     * resources/read：按 URI 读取资源原文，不经过大模型。
     * 示例：
     *   http://localhost:8080/ai/mcp/res/read?uri=zhaoshang://stats/dashboard
     *   http://localhost:8080/ai/mcp/res/read?uri=zhaoshang://dict/PROJECT_TYPE
     */
    @GetMapping("/mcp/res/read")
    public Map<String, Object> read(@RequestParam("uri") String uri) {
        McpSyncClient c = client();
        McpSchema.ReadResourceResult result = c.readResource(new ReadResourceRequest(uri));

        List<Map<String, Object>> contents = new ArrayList<>();
        StringBuilder merged = new StringBuilder();
        for (ResourceContents rc : result.contents()) {
            if (rc instanceof TextResourceContents text) {
                contents.add(Map.of("uri", text.uri(), "mimeType", text.mimeType() == null ? "" : text.mimeType(),
                        "text", text.text() == null ? "" : text.text()));
                merged.append(text.text() == null ? "" : text.text());
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("mode", "MCP resources/read");
        resp.put("uri", uri);
        resp.put("contents", contents);
        resp.put("length", merged.length());
        return resp;
    }

    /**
     * 资源增强问答（无记忆）：根据问题自动选择相关资源 → MCP 回环读取 → 大模型基于资源回答。
     * 示例：http://localhost:8080/ai/mcp/res/ask?msg=平台整体数据情况怎么样
     */
    @GetMapping("/mcp/res/ask")
    public Map<String, Object> ask(@RequestParam("msg") String msg,
                                   @RequestParam(value = "typeCode", required = false) String typeCode) {
        List<String> uris = selectResources(msg, typeCode);
        String resourceContext = loadResources(uris);

        String answer = dashScopeChatClient.prompt()
                .system(RESOURCE_SYSTEM_PROMPT)
                .user("【资源内容】\n" + resourceContext + "\n\n【用户问题】\n" + msg)
                .call()
                .content();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("question", msg);
        resp.put("mode", "MCP resources + AI");
        resp.put("usedResources", uris);
        resp.put("contextLength", resourceContext.length());
        resp.put("answer", answer == null ? "" : answer);
        return resp;
    }

    /**
     * 资源增强问答（带 Redis 会话记忆）：可围绕已读取的资源多轮追问。
     * 示例：
     *   http://localhost:8080/ai/mcp/res/chat?chatId=r1&msg=平台整体数据情况怎么样
     *   http://localhost:8080/ai/mcp/res/chat?chatId=r1&msg=那外资企业主要来自哪些国家
     */
    @GetMapping("/mcp/res/chat")
    public Map<String, Object> chat(@RequestParam("msg") String msg,
                                    @RequestParam(value = "chatId", defaultValue = "mcp-res-demo") String chatId,
                                    @RequestParam(value = "typeCode", required = false) String typeCode) {
        List<String> uris = selectResources(msg, typeCode);
        String resourceContext = loadResources(uris);

        String answer = redisDashScopeClient.prompt()
                .system(RESOURCE_SYSTEM_PROMPT)
                .user("【资源内容】\n" + resourceContext + "\n\n【用户问题】\n" + msg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("question", msg);
        resp.put("chatId", chatId);
        resp.put("mode", "MCP resources + AI + Memory");
        resp.put("usedResources", uris);
        resp.put("answer", answer == null ? "" : answer);
        return resp;
    }

    // ==================== 资源选择与读取 ====================

    /**
     * 按问题关键词选择相关资源（可叠加多个）。兜底返回知识库全文。
     * 显式传 typeCode 时附带对应字典资源。
     */
    private List<String> selectResources(String msg, String typeCode) {
        List<String> uris = new ArrayList<>();
        String q = msg == null ? "" : msg;

        if (containsAny(q, "数据", "概况", "总览", "看板", "总数", "统计", "投资", "多少家", "分布", "整体")) {
            uris.add(DASHBOARD_URI);
        }
        if (containsAny(q, "最近", "最新", "刚登记", "新登记", "新来")) {
            uris.add(RECENT_URI);
        }
        if (containsAny(q, "字典", "枚举", "项目类型", "企业类型", "有哪些类型", "类型编码")) {
            uris.add("zhaoshang://dict/" + (typeCode == null || typeCode.isBlank() ? "PROJECT_TYPE" : typeCode.trim()));
        }
        if (containsAny(q, "政策", "功能", "流程", "帮助", "知识库", "字段", "档案", "热线", "模块", "平台是")) {
            uris.add(KB_URI);
        }
        // 显式指定字典类型时即使没命中关键词也读取
        if (typeCode != null && !typeCode.isBlank()) {
            String dictUri = "zhaoshang://dict/" + typeCode.trim();
            if (!uris.contains(dictUri)) {
                uris.add(dictUri);
            }
        }
        // 兜底：知识库是通用背景资料
        if (uris.isEmpty()) {
            uris.add(KB_URI);
        }
        return uris;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /** 通过 MCP 协议逐个读取资源并拼接成上下文本；单个资源失败不影响其他资源 */
    private String loadResources(List<String> uris) {
        McpSyncClient c = client();
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (String uri : uris) {
            sb.append("----- 资源 ").append(index++).append("：").append(uri).append(" -----\n");
            try {
                ReadResourceResult r = c.readResource(new ReadResourceRequest(uri));
                for (ResourceContents rc : r.contents()) {
                    if (rc instanceof TextResourceContents text && text.text() != null) {
                        sb.append(text.text()).append('\n');
                    }
                }
            } catch (Exception e) {
                sb.append("（资源读取失败：").append(e.getMessage()).append("）\n");
            }
        }
        return sb.toString();
    }
}
