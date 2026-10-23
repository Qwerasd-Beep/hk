package com.ai.mcp;

import com.ai.tool.ZhaoshangMcpExtraTools;
import com.ai.tool.ZhaoshangQueryTools;
import com.ai.tool.ZhaoshangWriteTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 核心配置（最小可用版本）。
 *
 * 设计：复用平台已有的 @Tool 工具对象（一套工具、两个入口）——
 * 同一批 @Tool 方法既供内部 ChatClient 的 ToolCalling 使用，
 * 也通过 MethodToolCallbackProvider 包装后暴露给外部 MCP 客户端。
 *
 * spring-ai-starter-mcp-server-webmvc 的自动装配会收集容器中所有 ToolCallbackProvider，
 * 把其中的工具注册进 MCP Server（SYNC 类型，SSE 传输）：
 *   - SSE 连接端点：GET /sse（返回消息端点地址 + sessionId）
 *   - JSON-RPC 消息端点：POST /sse/message?sessionId=xxx
 *
 * 这两个端点已在 SecurityConfig 和 JwtAuthFilter 中放行（与 /ai/** 同等演示待遇），
 * 生产环境应改为要求 MCP 客户端携带凭据。
 */
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider zhaoshangToolCallbackProvider(
            ZhaoshangQueryTools queryTools,
            ZhaoshangWriteTools writeTools,
            ZhaoshangMcpExtraTools extraTools) {  //在这里,多注册一个工具 extraTools
        return MethodToolCallbackProvider.builder()
                .toolObjects(queryTools, writeTools, extraTools) //这里也多一个
                .build();
    }
}

