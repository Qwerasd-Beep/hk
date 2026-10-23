package com.ai.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.modelcontextprotocol.spec.McpSchema.*;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP Prompts 配置：预置 4 个可一键调用的提示模板（prompts/list + prompts/get）。
 *
 * P1 investment-landscape-analysis  输入国别/行业 → 招商格局分析（引导客户端组合统计工具）
 * P2 project-approval-review        输入项目代码 → 审批进度审查报告
 * P3 enterprise-profile             输入企业名   → 企业画像简报
 * P4 annual-investment-report       输入年度(可选) → 年度招商投资汇总报告
 *
 * 说明：prompts/get 只负责按参数渲染消息模板（role=user 的任务指令），
 * 真正取数由 MCP 客户端把渲染结果交给大模型、并由其回调 tools 完成（见 AIController11 的 /run）。
 */
@Configuration
public class McpPromptConfig {

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> zhaoshangPrompts() {
        List<McpServerFeatures.SyncPromptSpecification> prompts = new ArrayList<>();

        // P1：招商格局分析
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
                new Prompt(
                        "investment-landscape-analysis",
                        "招商格局分析",
                        "输入国别/地区或行业（至少一个，可都给），生成该领域的招商格局分析：企业数量、代表企业、项目布局、投资规模与态势建议",
                        List.of(
                                new PromptArgument("country", "国别/地区", "国别或地区，如：美国、德国、日本", false),
                                new PromptArgument("industry", "所属行业", "行业名称，如：新能源、汽车零部件", false))),
                (exchange, request) -> {
                    String country = arg(request, "country");
                    String industry = arg(request, "industry");
                    if (country.isBlank() && industry.isBlank()) {
                        return missing("请至少提供 country（国别/地区）或 industry（所属行业）中的一个参数。");
                    }
                    String scope = joinScope(country, industry);
                    String text = """
									请生成一份【%s】领域的招商格局分析报告。
		
									请依次调用工具取数（每一步都必须实际调用，不能只调用第一个就开始写报告）：
									1. 调用 countEnterprisesByCountry、countEnterprisesByIndustry 获取整体分布；
									   如需引用外资企业总数，以各国别数量之和为准，不要用国别个数代替企业总数。
									2. 调用 queryEnterprisesByCountry（国别传 country="%s"、行业传 industry="%s"）
									   一次性获取本领域全部企业名单及档案，报告里的代表企业名称、注册资本、投资者必须来自该结果。
									3. 调用 queryLocalProjects 和 sumInvestmentByYear 获取项目布局与年度投资数据
									   （只指定国别时，项目作为区域整体背景介绍，不要声称是某国别企业投资的）。
		
									取齐以上工具结果后，输出报告，包含：总体概况、企业格局（数量/代表企业/资本规模）、
									项目与投资布局、趋势研判与招商建议。
									要求：所有名称和数字与工具返回完全一致；某工具返回为空就如实说明，禁止编造；
									不要输出占位符，不要在报告里建议读者再去调用工具。
									""".formatted(scope, na(country), na(industry));
                    return result("%s招商格局分析模板".formatted(scope), text);
                }));

        // P2：审批进度审查
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
                new Prompt(
                        "project-approval-review",
                        "项目审批进度审查",
                        "输入项目代码（如 PRJ001），生成该项目的审批进度审查报告：流转节点、当前状态、停留时长与风险提示",
                        List.of(
                                new PromptArgument("projectCode", "项目代码", "项目代码，如：PRJ001、PRJ010", true))),
                (exchange, request) -> {
                    String projectCode = arg(request, "projectCode");
                    if (projectCode.isBlank()) {
                        return missing("缺少必填参数 projectCode（项目代码，如 PRJ001）。");
                    }
                    String text = """
									请对项目代码【%s】生成一份审批进度审查报告，所有信息必须通过调用平台工具获取。
		
									请按以下步骤取数：
									1. 调用 queryApprovalProgress（projectCode=%s）获取完整审批流转记录：
									   逐条梳理办理处室、办理状态、办理意见、办理时间。
									2. 调用 queryLocalProjects 定位项目基础信息（注意：该工具只支持按项目名称、类型、行业、
									   建设地点筛选，不支持按代码筛选；请分页拉取项目列表，在返回结果中按“项目代码”字段
									   匹配 %s），找到记录后用 getLocalProjectDetail 按其 id 补充总投资、建设地点、拟开工时间等详情；
									   禁止猜测 id。
									3. 调用 countApprovalByStatus 获取全局审批状态分布作为对照。
		
									报告结构：项目概况、审批流转时间线（按时间排序）、当前所处节点与状态、
									停留时长/堵点分析（结合办理时间与拟开工时间判断是否滞后）、风险提示与下一步建议。
									若查询不到该项目代码的任何审批记录，直接说明“未查询到项目 %s 的审批记录”，不要编造流转环节。
									""".formatted(projectCode, projectCode, projectCode, projectCode);
                    return result("项目 %s 审批进度审查模板".formatted(projectCode), text);
                }));

        // P3：企业画像
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
                new Prompt(
                        "enterprise-profile",
                        "外资企业画像简报",
                        "输入企业名称，生成企业画像简报：工商档案、资本与投资、来源国别、联系方式与关联项目",
                        List.of(
                                new PromptArgument("enterpriseName", "企业名称", "外资企业名称（支持模糊匹配），如：环宇能源", true))),
                (exchange, request) -> {
                    String enterpriseName = arg(request, "enterpriseName");
                    if (enterpriseName.isBlank()) {
                        return missing("缺少必填参数 enterpriseName（企业名称）。");
                    }
                    String text = """
									请为企业【%s】生成一份企业画像简报，所有信息必须通过调用平台工具获取。
		
									请按以下步骤取数：
									1. 先调用 queryForeignEnterprises（enterpriseName=%s）按名称模糊查询定位企业，
									   若名称带“公司”“有限公司”等通用后缀导致无结果，去掉后缀重试；
									   命中多条时，结合行业、国别、资本等信息列出候选请用户确认，不要强行当作同一家。
									2. 拿到准确 id 后调用 getForeignEnterpriseDetail 获取完整档案：
									   企业类型、国别地区、所属行业、企业状态、注册资本、投资总额、投资者名称、联系人与电话、企业地址。
									3. 必要时调用 searchKnowledgeBase 补充该行业/企业类型相关的招商政策背景。
		
									简报结构：企业概览（名称/类型/状态）、来源与行业、资本与投资规模、
									投资方与联系方式、政策背景（如有）、画像小结。
									查不到该企业时如实说明，禁止虚构档案数据。
									""".formatted(enterpriseName, enterpriseName);
                    return result("企业 %s 画像简报模板".formatted(enterpriseName), text);
                }));

        // P4：年度招商投资汇总报告
        prompts.add(new McpServerFeatures.SyncPromptSpecification(
                new Prompt(
                        "annual-investment-report",
                        "年度招商投资汇总报告",
                        "生成指定年度（默认当前年度）的招商投资汇总报告：项目数量、投资总额、外资企业与审批情况汇总",
                        List.of(
                                new PromptArgument("year", "年度", "4 位年份，如 2026；不传则默认当前年度", false))),
                (exchange, request) -> {
                    String year = arg(request, "year");
                    if (year.isBlank()) {
                        year = String.valueOf(Year.now().getValue());
                    }
                    String text = """
									请生成【%s 年度】招商投资汇总报告，所有数据必须通过调用平台工具获取，禁止编造。
		
									请按以下步骤取数：
									1. 调用 getStatsOverview 获取全量看板指标：外资企业总数、项目总数、总投资额（人民币/美元）、
									   中介机构数、审批总数及状态分布、国别/行业分布。
									2. 调用 sumInvestmentByYear 获取各年度开工项目数量与投资额，
									   重点摘出 %s 年的数据，并与相邻年度对比说明增减。
									3. 调用 queryLocalProjects 配合年份信息查看 %s 年重点项目（可用分页查看），
									   挑出投资规模较大的代表项目；调用 countApprovalByStatus 汇总本年度审批推进情况。
									4. 调用 countEnterprisesByCountry、countEnterprisesByIndustry 说明本年度外资来源结构。
		
									报告结构：年度概览（核心指标一览表）、项目与投资情况（含年度对比）、
									外资来源结构（国别/行业）、重点项目巡礼、审批推进情况、总结与下一年度工作建议。
									若 %s 年暂无开工项目，如实说明并重点呈现存量数据，不要虚构项目。
									""".formatted(year, year, year, year);
                    return result("%s 年度招商投资汇总报告模板".formatted(year), text);
                }));

        return prompts;
    }

    // ==================== 辅助方法 ====================

    /** 安全读取模板参数，缺失或 null 返回空串 */
    private static String arg(GetPromptRequest request, String key) {
        Map<String, Object> args = request.arguments();
        if (args == null) {
            return "";
        }
        Object v = args.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private static String na(String s) {
        return s == null || s.isBlank() ? "（不指定）" : s;
    }

    private static String joinScope(String country, String industry) {
        String c = country == null ? "" : country.trim();
        String i = industry == null ? "" : industry.trim();
        if (!c.isEmpty() && !i.isEmpty()) {
            return c + "·" + i;
        }
        return c.isEmpty() ? i : c;
    }

    /** 必填参数缺失时的友好提示模板 */
    private static GetPromptResult missing(String message) {
        return result("参数缺失", message);
    }

    private static GetPromptResult result(String description, String userInstruction) {
        return new GetPromptResult(description,
                List.of(new PromptMessage(Role.USER, new TextContent(userInstruction))));
    }
}
