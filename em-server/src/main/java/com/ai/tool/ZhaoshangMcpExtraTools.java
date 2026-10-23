package com.ai.tool;

import com.entity.ForeignEnterprise;
import com.entity.IntermediaryAgency;
import com.entity.UserInfo;
import com.mapper.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * MCP 新增工具集（第四批，现有 17 个业务工具未覆盖的能力）：
 * 18 searchKnowledgeBase    —— RAG 招商政策/知识库语义检索
 * 19 queryUsers              —— 系统用户只读列表（管理类客户端用，不含密码）
 * 20 getStatsOverview        —— 首页看板全量指标一次返回
 * 21 exportEnterpriseReport  —— 导出外企名单 CSV 报表文件
 *
 * 工具方法统一做参数钳制和全异常捕获：异常时返回带 error 说明的结构化结果，
 * 不向 MCP 会话抛异常，避免中断对话（MCP 工具健壮性约定）。
 */
@Component
public class ZhaoshangMcpExtraTools {

    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ForeignEnterpriseMapper foreignEnterpriseMapper;
    @Autowired
    private LocalProjectInfoMapper localProjectInfoMapper;
    @Autowired
    private ApprovalProcessInfoMapper approvalProcessInfoMapper;
    @Autowired
    private IntermediaryAgencyMapper intermediaryAgencyMapper;

    // ==================== 18. RAG 知识库检索 ====================

    @Tool(description = "语义检索招商政策与平台知识库，返回最相关的知识片段（文本+相似度分数+来源），"
            + "用于回答招商政策、平台功能、办事流程、使用帮助等非结构化知识类问题。"
            + "注意：查具体企业/项目业务数据不要用本工具，应使用 queryForeignEnterprises 等业务查询工具")
    public Map<String, Object> searchKnowledgeBase(
            @ToolParam(description = "检索问题或关键词，如：外资企业设立流程、平台有哪些功能") String query,
            @ToolParam(required = false, description = "返回片段数量，1-10之间，默认4") Integer topK) {
        if (query == null || query.isBlank()) {
            return Map.of("count", 0, "results", List.of(), "message", "query 不能为空");
        }
        int k = topK == null ? 4 : Math.max(1, Math.min(10, topK));
        try {
            List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(k)
                    .similarityThreshold(0.4)
                    .build());
            List<Map<String, Object>> results = new ArrayList<>();
            if (docs != null) {
                for (Document doc : docs) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", doc.getId());
                    item.put("text", doc.getText() == null ? "" : doc.getText());
                    item.put("score", doc.getScore() == null ? 0.0 : doc.getScore());
                    item.put("source", doc.getMetadata() == null ? "" : doc.getMetadata().getOrDefault("source", ""));
                    results.add(item);
                }
            }
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("query", query);
            resp.put("count", results.size());
            resp.put("results", results);
            if (results.isEmpty()) {
                resp.put("message", "知识库中没有检索到相关内容");
            }
            return resp;
        } catch (Exception e) {
            return errorResult("知识库检索失败：" + e.getMessage());
        }
    }

    // ==================== 19. 用户只读查询 ====================

    @Tool(description = "分页查询系统用户的只读信息（ID、用户名、备注、编辑时间），可按姓名模糊筛选；"
            + "不返回密码等敏感字段，供管理类客户端回答“系统里有哪些用户/账号”类问题")
    public Map<String, Object> queryUsers(
            @ToolParam(required = false, description = "用户名关键词，支持模糊匹配，不传则返回全部用户") String keyword,
            @ToolParam(required = false, description = "页码，从1开始，默认1（每页20条）") Integer pageIndex) {
        try {
            List<UserInfo> all = userMapper.selectAllUsers(keyword);
            int pageSize = 20;
            int page = pageIndex == null || pageIndex < 1 ? 1 : pageIndex;
            int from = Math.min((page - 1) * pageSize, all.size());
            int to = Math.min(from + pageSize, all.size());
            List<UserInfo> pageList = all.subList(from, to);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("total", all.size());
            resp.put("pageIndex", page);
            resp.put("pageSize", pageSize);
            resp.put("list", pageList);
            return resp;
        } catch (Exception e) {
            return errorResult("用户查询失败：" + e.getMessage());
        }
    }

    // ==================== 20. 首页看板总览 ====================

    @Tool(description = "一次返回招商首页看板的全部核心指标：外资企业总数、招商项目总数、项目总投资额（人民币/美元）、"
            + "中介机构总数、审批记录总数及状态分布，以及外资企业按国别、按行业的完整分布，"
            + "用于回答“平台整体数据情况”“给我一个总览/看板数据”类问题")
    public Map<String, Object> getStatsOverview() {
        try {
            Map<String, Object> overview = new LinkedHashMap<>();

            // 外企国别分布（同时用于求企业总数）
            List<Map<String, Object>> byCountry = foreignEnterpriseMapper.countByCountry();
            long enterpriseTotal = sumLong(byCountry);
            overview.put("外资企业总数", enterpriseTotal);
            overview.put("外企国别分布", byCountry);
            overview.put("外企行业分布", foreignEnterpriseMapper.countByIndustry());

            // 项目：按年度汇总行求和得到总数与投资总额
            List<Map<String, Object>> byYear = localProjectInfoMapper.sumInvestmentByYear();
            long projectTotal = 0;
            BigDecimal investmentTotal = BigDecimal.ZERO;
            BigDecimal investmentUsdTotal = BigDecimal.ZERO;
            for (Map<String, Object> row : byYear) {
                projectTotal += asLong(row.get("项目数量"));
                investmentTotal = investmentTotal.add(asDecimal(row.get("总投资额合计")));
                investmentUsdTotal = investmentUsdTotal.add(asDecimal(row.get("总投资额折合美元合计")));
            }
            overview.put("招商项目总数", projectTotal);
            overview.put("项目总投资额合计_万元", investmentTotal);
            overview.put("项目总投资额折合美元合计_万元", investmentUsdTotal);
            overview.put("项目按年度分布", byYear);

            // 审批：总数 + 状态分布
            List<Map<String, Object>> byStatus = approvalProcessInfoMapper.countByStatus();
            overview.put("审批记录总数", sumLong(byStatus));
            overview.put("审批状态分布", byStatus);

            // 中介机构总数
            List<IntermediaryAgency> agencies = intermediaryAgencyMapper.findAll(new IntermediaryAgency());
            overview.put("中介机构总数", agencies == null ? 0 : agencies.size());

            return overview;
        } catch (Exception e) {
            return errorResult("看板指标统计失败：" + e.getMessage());
        }
    }

    // ==================== 21. 外企名单 CSV 导出 ====================

    @Tool(description = "导出外资企业名单为 CSV 报表文件（Excel 可直接打开），可按所属行业、国别或地区筛选；"
            + "返回导出文件的服务器路径、记录行数、字段列表和前几行预览，用于回答“导出/生成外企名单报表”类需求")
    public Map<String, Object> exportEnterpriseReport(
            @ToolParam(required = false, description = "按所属行业筛选（模糊），如：汽车、电子；不传则全部行业") String industry,
            @ToolParam(required = false, description = "按国别或地区筛选（模糊），如：美国、德国；不传则全部国别") String country) {
        try {
            List<ForeignEnterprise> list = foreignEnterpriseMapper.selectAllForExport(
                    blankToNull(industry), blankToNull(country));

            String[] headers = { "ID", "企业名称", "国别或地区", "所属行业", "企业状态", "注册资本_万元",
                    "投资总额", "投资者名称", "联系人", "联系电话", "固定电话", "邮箱", "企业地址",
                    "成立日期", "录入时间", "数据来源" };

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
            StringBuilder csv = new StringBuilder();
            csv.append('?').append(String.join(",", headers)).append("\r\n"); // UTF-8 BOM，Excel 打开不乱码
            for (ForeignEnterprise e : list) {
                String[] cells = {
                        str(e.getId()),
                        e.get企业名称(),
                        e.get国别或地区(),
                        e.get所属行业(),
                        e.get企业状态(),
                        e.get注册资本() == null ? "" : e.get注册资本().toPlainString(),
                        e.get投资总额(),
                        e.get投资者名称(),
                        e.get联系人(),
                        e.get联系电话(),
                        e.get固定电话(),
                        e.get邮箱(),
                        e.get企业地址(),
                        e.get成立日期() == null ? "" : dayFmt.format(e.get成立日期()),
                        e.get录入时间() == null ? "" : sdf.format(e.get录入时间()),
                        e.get数据来源()
                };
                for (int i = 0; i < cells.length; i++) {
                    if (i > 0) {
                        csv.append(',');
                    }
                    csv.append(csvEscape(cells[i]));
                }
                csv.append("\r\n");
            }

            // 落盘到项目 uploads/export 目录（与 uploads/ai-image 同级）
            String fileName = "外企名单_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";
            Path dir = Paths.get("uploads", "export");
            Files.createDirectories(dir);
            Path filePath = dir.resolve(fileName);
            Files.writeString(filePath, csv.toString(), StandardCharsets.UTF_8);

            // 预览前 6 行（表头 + 前 5 条数据）
            String[] lines = csv.toString().split("\r\n", -1);
            List<String> preview = new ArrayList<>();
            for (int i = 0; i < Math.min(6, lines.length); i++) {
                preview.add(lines[i]);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("fileName", fileName);
            resp.put("filePath", filePath.toAbsolutePath().toString());
            resp.put("rows", list.size());
            resp.put("filterIndustry", industry == null ? "" : industry);
            resp.put("filterCountry", country == null ? "" : country);
            resp.put("columns", List.of(headers));
            resp.put("preview", preview);
            return resp;
        } catch (Exception e) {
            return errorResult("报表导出失败：" + e.getMessage());
        }
    }

    // ==================== 22. 按国别/行业列出企业（格局分析专用，免翻页） ====================
    @Tool(description = "按国别或地区直接列出全部匹配的外资企业完整档案（不分页），也可同时叠加行业模糊筛选；"
            + "用于招商格局分析等需要一次拿到某国别所有企业的场景，避免分页逐次拉取。"
            + "注意：country 与 industry 至少传一个；返回 total 和 list（按登记时间倒序）")
    public Map<String, Object> queryEnterprisesByCountry(
            @ToolParam(required = false, description = "国别或地区，支持模糊匹配，如：美国、德国") String country,
            @ToolParam(required = false, description = "所属行业，支持模糊匹配，如：新能源；与国别可同时传入") String industry) {
        try {
            String c = blankToNull(country);
            String i = blankToNull(industry);
            if (c == null && i == null) {
                return errorResult("country（国别或地区）与 industry（所属行业）至少需要传入一个");
            }
            List<ForeignEnterprise> list = foreignEnterpriseMapper.selectAllForExport(i, c);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("total", list.size());
            resp.put("filterCountry", c == null ? "" : c);
            resp.put("filterIndustry", i == null ? "" : i);
            resp.put("list", list);
            return resp;
        } catch (Exception e) {
            return errorResult("按国别查询企业失败：" + e.getMessage());
        }
    }


    // ==================== 私有辅助方法 ====================

    private long sumLong(List<Map<String, Object>> rows) {
        long total = 0;
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                total += asLong(row.get("数量"));
            }
        }
        return total;
    }

    private long asLong(Object v) {
        return v == null ? 0L : ((Number) v).longValue();
    }

    private BigDecimal asDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v instanceof BigDecimal bd ? bd : new BigDecimal(v.toString());
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** CSV 单元格转义：含逗号、引号、换行时用双引号包裹，内部双引号翻倍 */
    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private Map<String, Object> errorResult(String message) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", true);
        err.put("message", message);
        return err;
    }
}
