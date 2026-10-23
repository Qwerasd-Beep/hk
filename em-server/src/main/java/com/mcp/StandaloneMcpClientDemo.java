package com.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema.*;

import java.time.Duration;
import java.util.Map;

/**
 * 外部 MCP 客户端调用示例（不依赖 Spring 容器，纯 MCP Java SDK 写法）。
 *
 * 演示：一个独立的 Java 程序如何连接 zhaoshang-server 的 MCP Server（SSE 传输），
 * 完成 握手 -> tools/list -> tools/call -> resources/list -> resources/read 全流程。
 * 其他业务系统接入时照抄本类即可，只需要 mcp-core 一个依赖。
 *
 * 运行前提：zhaoshang-server 已在 8080 启动。
 * 运行方式（项目根目录）：
 *   mvn -q test-compile
 *   mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt -q
 *   java -cp "target/classes;target/test-classes;$(Get-Content target/cp.txt)" hk.mcp.StandaloneMcpClientDemo
 * （Linux/macOS 用冒号 : 替换分号 ;）
 */
public class StandaloneMcpClientDemo {

    /** MCP Server 的 SSE 根地址（只需这一个连接参数） */
    private static final String SERVER_BASE_URL = "http://localhost:8080";

    public static void main(String[] args) {
        // 1. 创建 SSE 传输：SDK 会先 GET {baseUrl}/sse 拿到 sessionId 和消息端点
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(SERVER_BASE_URL)
                .sseEndpoint("/sse")
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 2. 创建同步客户端（不自动 initialize，便于演示每一步）
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(60))
                .build();

        try {
            // 3. 握手：initialize，拿到服务端信息与能力声明
            InitializeResult init = client.initialize();
            System.out.println("[握手成功] 服务端：" + init.serverInfo().name()
                    + " v" + init.serverInfo().version()
                    + "，协议版本：" + init.protocolVersion());

            // 4. tools/list：发现服务端全部工具（当前 21 个）
            ListToolsResult toolsResult = client.listTools();
            System.out.println("\n[工具清单] 共 " + toolsResult.tools().size() + " 个工具：");
            toolsResult.tools().forEach(t -> System.out.println("  - " + t.name()));

            // 5. tools/call 示例一：无参工具 getStatsOverview（首页看板指标）
            System.out.println("\n[调用工具] getStatsOverview（无参）：");
            CallToolResult overview = client.callTool(new CallToolRequest("getStatsOverview", Map.of()));
            printToolResult(overview);

            // 6. tools/call 示例二：带参工具 searchKnowledgeBase（RAG 语义检索）
            System.out.println("\n[调用工具] searchKnowledgeBase（带参 query/topK）：");
            CallToolResult kb = client.callTool(new CallToolRequest("searchKnowledgeBase",
                    Map.of("query", "外资企业设立流程", "topK", 1)));
            printToolResult(kb);

            // 7. resources/list：发现可读取资源
            ListResourcesResult resourcesResult = client.listResources();
            System.out.println("\n[资源清单] 共 " + resourcesResult.resources().size() + " 个静态资源：");
            resourcesResult.resources().forEach(r -> System.out.println("  - " + r.uri() + "  (" + r.name() + ")"));
            System.out.println("  另有模板资源：zhaoshang://dict/{typeCode}（list 中不展开，直接按 URI 读取）");

            // 8. resources/read 示例一：固定 URI 资源（实时看板 JSON）
            System.out.println("\n[读取资源] zhaoshang://stats/dashboard（前 300 字）：");
            ReadResourceResult dashboard = client.readResource(
                    new ReadResourceRequest("zhaoshang://stats/dashboard"));
            printResourceResult(dashboard, 300);

            // 9. resources/read 示例二：模板 URI 资源（把 {typeCode} 替换成实际编码）
            System.out.println("\n[读取资源] zhaoshang://dict/PROJECT_TYPE：");
            ReadResourceResult dict = client.readResource(
                    new ReadResourceRequest("zhaoshang://dict/PROJECT_TYPE"));
            printResourceResult(dict, 500);

            System.out.println("\n[完成] 全部 MCP 调用演示结束。");
        } finally {
            // 10. 优雅关闭（释放 SSE 连接）
            client.closeGracefully();
        }
    }

    /** 打印工具调用返回的文本内容 */
    private static void printToolResult(CallToolResult result) {
        if (result.isError()) {
            System.out.println("  工具返回错误！");
        }
        for (Content content : result.content()) {
            if (content instanceof TextContent text) {
                String s = text.text();
                System.out.println("  " + (s.length() > 600 ? s.substring(0, 600) + " ...（截断）" : s));
            }
        }
    }

    /** 打印资源读取结果，超过 maxChars 截断 */
    private static void printResourceResult(ReadResourceResult result, int maxChars) {
        for (ResourceContents content : result.contents()) {
            if (content instanceof TextResourceContents text) {
                String s = text.text();
                System.out.println("  [" + text.mimeType() + "] "
                        + (s.length() > maxChars ? s.substring(0, maxChars) + " ...（截断）" : s));
            }
        }
    }
}
