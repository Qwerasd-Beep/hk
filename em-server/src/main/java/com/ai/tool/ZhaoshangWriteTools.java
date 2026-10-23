package com.ai.tool;

import com.entity.*;
import com.service.ApprovalProcessInfoService;
import com.service.ForeignEnterpriseService;
import com.service.IntermediaryAgencyService;
import com.service.LocalProjectInfoService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 招商平台写操作工具集（ToolCalling 第三批）。
 * 所有写工具都带 confirm 参数实现“二次确认”：
 * 模型第一次调用不传 confirm，工具返回待确认提示；模型向用户复述操作内容，
 * 用户明确同意后再以 confirm=true 调用才真正落库（需要配合带会话记忆的接口使用）。
 */
@Component
public class ZhaoshangWriteTools {
    @Autowired
    private ForeignEnterpriseService foreignEnterpriseService;
    @Autowired
    private IntermediaryAgencyService intermediaryAgencyService;
    @Autowired
    private ApprovalProcessInfoService approvalProcessInfoService;
    @Autowired
    private LocalProjectInfoService localProjectInfoService;

    /**
     * 待确认操作登记：key 为操作内容摘要。
     * 第一次调用（confirm!=true）登记摘要；confirm=true 时必须能移除到相同摘要才执行，
     * 防止模型跳过用户确认环节、第一次调用就直接传 confirm=true 绕过人工把关。
     */
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    /** 生成待确认提示，模型会把它转述给用户 */
    private String pendingConfirm(String summary) {
        PENDING.add(summary);
        return "【待确认】" + summary
                + "。请把以上操作内容复述给用户并询问是否确认执行；用户明确同意后，再以完全相同的参数加 confirm=true 调用本工具完成写入。";
    }

    /** 校验 confirm 调用是否有对应的待确认登记；有则移除并放行 */
    private boolean confirmAllowed(String summary) {
        return PENDING.remove(summary);
    }

    private static final String NO_PENDING_HINT
            = "没有与本次操作内容完全一致的待确认记录。请先不要传 confirm 调用本工具生成待确认并向用户复述，用户同意后再以完全相同的参数加 confirm=true 调用。";

    /** 摘要归一化：null 与空串视为相同，避免模型两次调用对可选参数一个传 null 一个传 "" 导致摘要不匹配 */
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** 数字归一化：8000 与 8000.0 视为相同 */
    private static String num(BigDecimal n) {
        return n == null ? "" : n.stripTrailingZeros().toPlainString();
    }

    private static String num(Integer n) {
        return n == null ? "" : String.valueOf(n);
    }

    @Tool(description = "登记一家新的外资企业（写操作，需二次确认）")
    public String registerForeignEnterprise(
            @ToolParam(description = "企业名称") String enterpriseName,
            @ToolParam(description = "国别或地区，如：美国、日本") String country,
            @ToolParam(description = "所属行业，如：电子设备制造") String industry,
            @ToolParam(required = false, description = "注册资本（万元）") BigDecimal registeredCapital,
            @ToolParam(required = false, description = "投资者名称") String investor,
            @ToolParam(required = false, description = "联系人") String contactPerson,
            @ToolParam(required = false, description = "联系电话") String contactPhone,
            @ToolParam(required = false, description = "确认执行：用户明确同意后传 true") Boolean confirm) {
        String summary = "登记外资企业：名称=" + enterpriseName + "，国别=" + country + "，行业=" + industry
                + "，注册资本=" + num(registeredCapital) + "，投资者=" + nz(investor)
                + "，联系人=" + nz(contactPerson) + "，电话=" + nz(contactPhone);
        if (!Boolean.TRUE.equals(confirm)) {
            return pendingConfirm(summary);
        }
        if (!confirmAllowed(summary)) {
            return NO_PENDING_HINT;
        }

        ForeignEnterprise e = new ForeignEnterprise();
        e.set企业名称(enterpriseName);
        e.set国别或地区(country);
        e.set所属行业(industry);
        e.set注册资本(registeredCapital);
        e.set投资者名称(investor);
        e.set联系人(contactPerson);
        e.set联系电话(contactPhone);
        e.set录入时间(new Date());
        e.set是否已删除(0);
        e.set数据来源("AI助手登记");
        e.set企业状态("存量");
        foreignEnterpriseService.insert(e);
        return "登记成功：" + summary;
    }

    @Tool(description = "更新外资企业的联系信息或企业状态（写操作，需二次确认）。按ID或企业名称定位企业（优先用ID），只修改传入的字段，未传字段保持原值")
    public String updateForeignEnterprise(
            @ToolParam(required = false, description = "外资企业ID，与名称二选一，优先传ID") Integer id,
            @ToolParam(required = false, description = "企业名称，不知道ID时按名称定位") String enterpriseName,
            @ToolParam(required = false, description = "企业状态，如：存量、已注销") String status,
            @ToolParam(required = false, description = "联系人") String contactPerson,
            @ToolParam(required = false, description = "联系电话") String contactPhone,
            @ToolParam(required = false, description = "邮箱") String email,
            @ToolParam(required = false, description = "确认执行：用户明确同意后传 true") Boolean confirm) {
        // 名称定位在服务端完成，避免模型猜测ID导致张冠李戴
        ForeignEnterprise e = resolveEnterprise(id, enterpriseName);
        if (e == null) {
            return "未找到匹配的外资企业（ID=" + id + "，名称=" + enterpriseName
                    + "）。注意：如果用户在问题中给出了企业名称，请把该名称作为 enterpriseName 参数重新调用本工具重试；只有用户完全没提供名称和ID时才向用户反问。";
        }
        String summary = "更新外资企业「" + e.get企业名称() + "」(ID=" + e.getId() + ")：企业状态=" + nz(status)
                + "，联系人=" + nz(contactPerson) + "，电话=" + nz(contactPhone) + "，邮箱=" + nz(email);
        if (!Boolean.TRUE.equals(confirm)) {
            return pendingConfirm(summary);
        }
        if (!confirmAllowed(summary)) {
            return NO_PENDING_HINT;
        }

        if (status != null) e.set企业状态(status);
        if (contactPerson != null) e.set联系人(contactPerson);
        if (contactPhone != null) e.set联系电话(contactPhone);
        if (email != null) e.set邮箱(email);
        // update 是整行更新，先查出原记录再改，避免未传字段被清空
        foreignEnterpriseService.update(e);
        return "更新成功：" + summary;
    }

    /** 按ID或名称定位外资企业：ID优先；按名称模糊查，唯一匹配或精确匹配才认可，多结果返回 null 由调用方提示 */
    private ForeignEnterprise resolveEnterprise(Integer id, String enterpriseName) {
        if (id != null) {
            return foreignEnterpriseService.selectById(id);
        }
        if (enterpriseName == null || enterpriseName.isBlank()) {
            return null;
        }
        com.entity.query.ForeignEnterpriseQ q = new com.entity.query.ForeignEnterpriseQ();
        q.set企业名称(enterpriseName);
        java.util.List<ForeignEnterprise> list = foreignEnterpriseService.selectAll(1, q).getList();
        if (list.size() == 1) {
            return list.get(0); // 模糊结果唯一，直接认可
        }
        for (ForeignEnterprise item : list) {
            if (enterpriseName.equals(item.get企业名称())) {
                return item; // 多结果时只认精确匹配
            }
        }
        return null;
    }

    @Tool(description = "登记一家新的中介机构（写操作，需二次确认）")
    public String registerIntermediaryAgency(
            @ToolParam(description = "中介机构名称") String agencyName,
            @ToolParam(description = "中介机构证照类型，如：营业执照") String licenseType,
            @ToolParam(description = "中介机构证照号码") String licenseNumber,
            @ToolParam(required = false, description = "法定代表人") String legalPerson,
            @ToolParam(required = false, description = "联系人名称") String contactPerson,
            @ToolParam(required = false, description = "联系电话") String contactPhone,
            @ToolParam(required = false, description = "通讯地址") String address,
            @ToolParam(required = false, description = "确认执行：用户明确同意后传 true") Boolean confirm) {
        String summary = "登记中介机构：名称=" + agencyName + "，证照类型=" + licenseType + "，证照号码=" + licenseNumber
                + "，法定代表人=" + nz(legalPerson) + "，联系人=" + nz(contactPerson) + "，电话=" + nz(contactPhone) + "，地址=" + nz(address);
        if (!Boolean.TRUE.equals(confirm)) {
            return pendingConfirm(summary);
        }
        if (!confirmAllowed(summary)) {
            return NO_PENDING_HINT;
        }

        IntermediaryAgency agency = new IntermediaryAgency();
        agency.set中介机构名称(agencyName);
        agency.set中介机构证照类型(licenseType);
        agency.set中介机构证照号码(licenseNumber);
        agency.set法定代表人(legalPerson);
        agency.set联系人名称(contactPerson);
        agency.set联系电话(contactPhone);
        agency.set通讯地址(address);
        // 有效标识列为 NOT NULL，沿用现有数据使用的字典编码 29
        DictionaryItem flag = new DictionaryItem();
        flag.set字典编码("29");
        agency.set有效标识(flag);
        intermediaryAgencyService.insert(agency);
        return "登记成功：" + summary;
    }

    @Tool(description = "为某个招商项目提交一条审批办理记录（写操作，需二次确认），如：受理、审核通过、退回")
    public String submitApprovalOpinion(
            @ToolParam(description = "项目代码，如：PRJ001") String projectCode,
            @ToolParam(description = "办理处室") String department,
            @ToolParam(description = "办理状态，如：已受理、审核中、已发证、已退回") String status,
            @ToolParam(description = "办理意见") String opinion,
            @ToolParam(required = false, description = "确认执行：用户明确同意后传 true") Boolean confirm) {
        String summary = "提交审批记录：项目代码=" + projectCode + "，办理处室=" + department
                + "，办理状态=" + status + "，办理意见=" + opinion;
        if (!Boolean.TRUE.equals(confirm)) {
            return pendingConfirm(summary);
        }
        if (!confirmAllowed(summary)) {
            return NO_PENDING_HINT;
        }

        ApprovalProcessInfo o = new ApprovalProcessInfo();
        o.setOwnerId(1); // 演示环境没有登录人上下文，归属 admin
        o.set项目代码(projectCode);
        o.set办理处室(department);
        o.set办理状态(status);
        o.set办理意见(opinion);
        o.set办理时间(new Date());
        approvalProcessInfoService.insert(o);
        return "提交成功：" + summary;
    }

    @Tool(description = "更新招商项目的关键信息（写操作，需二次确认）。按ID或项目名称定位项目（优先用ID），只修改传入的字段，未传字段保持原值")
    public String updateProjectInfo(
            @ToolParam(required = false, description = "项目ID，与名称二选一，优先传ID") Integer id,
            @ToolParam(required = false, description = "项目名称，不知道ID时按名称定位") String projectName,
            @ToolParam(required = false, description = "总投资额（万元）") BigDecimal totalInvestment,
            @ToolParam(required = false, description = "拟开工年份，如：2026") Integer startYear,
            @ToolParam(required = false, description = "拟建成年份，如：2028") Integer finishYear,
            @ToolParam(required = false, description = "建设地点") String location,
            @ToolParam(required = false, description = "建设规模及内容") String scale,
            @ToolParam(required = false, description = "确认执行：用户明确同意后传 true") Boolean confirm) {
        // 名称定位在服务端完成，避免模型猜测ID导致张冠李戴
        LocalProjectInfo info = resolveProject(id, projectName);
        if (info == null) {
            return "未找到匹配的招商项目（ID=" + id + "，名称=" + projectName
                    + "）。注意：如果用户在问题中给出了项目名称，请把该名称作为 projectName 参数重新调用本工具重试；只有用户完全没提供名称和ID时才向用户反问。";
        }
        String summary = "更新招商项目「" + info.get项目名称() + "」(ID=" + info.getId() + ")：总投资额=" + num(totalInvestment)
                + "，拟开工=" + num(startYear) + "，拟建成=" + num(finishYear) + "，建设地点=" + nz(location) + "，建设规模=" + nz(scale);
        if (!Boolean.TRUE.equals(confirm)) {
            return pendingConfirm(summary);
        }
        if (!confirmAllowed(summary)) {
            return NO_PENDING_HINT;
        }

        if (totalInvestment != null) info.set总投资额(totalInvestment);
        if (startYear != null) info.set拟开工时间(startYear);
        if (finishYear != null) info.set拟建成时间(finishYear);
        if (location != null) info.set建设地点(location);
        if (scale != null) info.set建设规模及内容(scale);
        // 先查出原记录再修改，保留未传入的项目字段
        localProjectInfoService.updateById(info);
        return "更新成功：" + summary;
    }

    /** 按ID或名称定位招商项目：ID优先；按名称模糊查，唯一匹配或精确匹配才认可 */
    private LocalProjectInfo resolveProject(Integer id, String projectName) {
        if (id != null) {
            return localProjectInfoService.selectById(id);
        }
        if (projectName == null || projectName.isBlank()) {
            return null;
        }
        com.entity.query.LocalProjectInfoQ q = new com.entity.query.LocalProjectInfoQ();
        q.set项目名称(projectName);
        java.util.List<LocalProjectInfo> list = localProjectInfoService.selectAll(1, q).getList();
        if (list.size() == 1) {
            return list.get(0); // 模糊结果唯一，直接认可
        }
        for (LocalProjectInfo item : list) {
            if (projectName.equals(item.get项目名称())) {
                return item; // 多结果时只认精确匹配
            }
        }
        return null;
    }


}



