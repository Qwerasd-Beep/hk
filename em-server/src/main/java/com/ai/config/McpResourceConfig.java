package com.ai.config;

import com.ai.tool.ZhaoshangMcpExtraTools;
import com.entity.ForeignEnterprise;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapper.ForeignEnterpriseMapper;
import com.service.DictionaryItemService;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * MCP Resources 配置：把平台的可读取资源以标准 MCP 资源协议暴露，
 * 客户端无需调用工具即可直接拉取（resources/list + resources/read）。
 *
 * R1 zhaoshang://kb/policy              —— 招商政策/平台知识库全文（rag-storage 文件）
 * R2 zhaoshang://dict/{typeCode}        —— 数据字典（模板资源，按类型编码取枚举）
 * R3 zhaoshang://stats/dashboard        —— 实时首页看板指标 JSON（复用工具 20 的逻辑）
 * R4 zhaoshang://enterprises/recent     —— 最近登记的 10 家外资企业
 */
@Configuration
public class McpResourceConfig {
    private static final String JSON = "application/json";
    private static final String TEXT = "text/plain";
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> zhaoshangResources(
            ZhaoshangMcpExtraTools extraTools,
            ForeignEnterpriseMapper foreignEnterpriseMapper,
            ObjectMapper objectMapper) {

        // R1：知识库全文
        McpServerFeatures.SyncResourceSpecification kbPolicy =
                new McpServerFeatures.SyncResourceSpecification(
                        McpSchema.Resource.builder()
                                .uri("zhaoshang://kb/policy")
                                .name("招商政策与平台知识库")
                                .description("招商服务平台内置知识库全文：平台功能、外资企业档案字段、办事流程、使用帮助等")
                                .mimeType(TEXT)
                                .build(),
                        (exchange, request) -> textResult(request.uri(), TEXT, readKnowledgeFile()));

        // R3：实时看板指标（复用 getStatsOverview 工具的同一套统计逻辑，保证数字一致）
        McpServerFeatures.SyncResourceSpecification dashboard =
                new McpServerFeatures.SyncResourceSpecification(
                        McpSchema.Resource.builder()
                                .uri("zhaoshang://stats/dashboard")
                                .name("首页看板实时指标")
                                .description("外资企业/招商项目/投资总额/审批状态/中介机构的实时统计摘要 JSON，供客户端直接渲染看板")
                                .mimeType(JSON)
                                .build(),
                        (exchange, request) -> textResult(request.uri(), JSON,
                                toJson(objectMapper, extraTools.getStatsOverview())));

        // R4：最近登记的 10 家外企（selectAllForExport 已按 id DESC 排序）
        McpServerFeatures.SyncResourceSpecification recentEnterprises =
                new McpServerFeatures.SyncResourceSpecification(
                        McpSchema.Resource.builder()
                                .uri("zhaoshang://enterprises/recent")
                                .name("最近登记的外资企业")
                                .description("按登记时间倒序返回最近 10 家外资企业的完整档案 JSON")
                                .mimeType(JSON)
                                .build(),
                        (exchange, request) -> {
                            List<ForeignEnterprise> all = foreignEnterpriseMapper.selectAllForExport(null, null);
                            List<ForeignEnterprise> recent = all.size() <= 10 ? all : all.subList(0, 10);
                            return textResult(request.uri(), JSON, toJson(objectMapper, recent));
                        });

        return List.of(kbPolicy, dashboard, recentEnterprises);
    }

    @Bean
    public List<McpServerFeatures.SyncResourceTemplateSpecification> zhaoshangResourceTemplates(
            DictionaryItemService dictionaryItemService,
            ObjectMapper objectMapper) {

        // R2：数据字典模板资源，如 zhaoshang://dict/PROJECT_TYPE
        McpServerFeatures.SyncResourceTemplateSpecification dict =
                new McpServerFeatures.SyncResourceTemplateSpecification(
                        McpSchema.ResourceTemplate.builder()
                                .uriTemplate("zhaoshang://dict/{typeCode}")
                                .name("数据字典项")
                                .description("按字典类型编码读取枚举值，typeCode 如 PROJECT_TYPE（项目类型）、有效标识等，返回字典项 JSON 数组")
                                .mimeType(JSON)
                                .build(),
                        (exchange, request) -> {
                            String typeCode = extractPathSegment(request.uri());
                            List<?> items = dictionaryItemService.getItemsByTypeCode(typeCode);
                            String json = toJson(objectMapper,
                                    Map.of("typeCode", typeCode == null ? "" : typeCode, "count", items.size(),
                                            "items", items));
                            return textResult(request.uri(), JSON, json);
                        });

        return List.of(dict);
    }

    // ==================== 辅助方法 ====================

    /** 读取类路径知识库文件全文 */
    private String readKnowledgeFile() {
        try {
            ClassPathResource resource = new ClassPathResource("rag-storage/zhaoshang-knowledge-base.txt");
            if (!resource.exists()) {
                return "知识库文件不存在：classpath:rag-storage/zhaoshang-knowledge-base.txt";
            }
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "知识库读取失败：" + e.getMessage();
        }
    }

    /** 从 zhaoshang://dict/{typeCode} 中解析出 typeCode */
    private String extractPathSegment(String uri) {
        try {
            String path = URI.create(uri).getPath(); // 形如 /PROJECT_TYPE
            if (path == null || path.isBlank() || "/".equals(path)) {
                return "";
            }
            return URLDecoder.decode(path.startsWith("/") ? path.substring(1) : path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private String toJson(ObjectMapper objectMapper, Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"error\":true,\"message\":\"JSON 序列化失败：" + e.getMessage() + "\"}";
        }
    }

    private McpSchema.ReadResourceResult textResult(String uri, String mimeType, String text) {
        return new McpSchema.ReadResourceResult(
                List.of(new McpSchema.TextResourceContents(uri, mimeType, text == null ? "" : text)));
    }
}

