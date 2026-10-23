package com.ai.tool;

import com.entity.*;
import com.github.pagehelper.PageInfo;
import com.mapper.ApprovalProcessInfoMapper;
import com.mapper.ForeignEnterpriseMapper;
import com.mapper.LocalProjectInfoMapper;
import com.entity.query.ForeignEnterpriseQ;
import com.entity.query.LocalProjectInfoQ;
import com.service.DictionaryItemService;
import com.service.ForeignEnterpriseService;
import com.service.IntermediaryAgencyService;
import com.service.LocalProjectInfoService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 招商平台只读查询工具集（ToolCalling 第一批）。
 * 大模型根据用户问题自主选择并调用这些方法，把真实业务数据带回后组织回答。
 */
@Component
public class ZhaoshangQueryTools {
    @Resource
    private ForeignEnterpriseService foreignEnterpriseService;
    @Resource
    private LocalProjectInfoService localProjectInfoService;
    @Resource
    private IntermediaryAgencyService intermediaryAgencyService;
    @Resource
    private ApprovalProcessInfoMapper approvalProcessInfoMapper;
    @Resource
    private ForeignEnterpriseMapper foreignEnterpriseMapper;
    @Resource
    private LocalProjectInfoMapper localProjectInfoMapper;
    @Resource
    private DictionaryItemService dictionaryItemService;

    @Tool(description = "分页查询外资企业信息，可按企业名称（模糊）、所属行业、企业状态组合筛选，返回符合条件的总数和企业列表")
    public Map<String, Object> queryForeignEnterprises(
            @ToolParam(required = false, description = "企业名称，支持模糊匹配，如：汽车、电子") String enterpriseName,
            @ToolParam(required = false, description = "所属行业") String industry,
            @ToolParam(required = false, description = "企业状态") String status,
            @ToolParam(required = false, description = "页码，从1开始，默认1") Integer pageIndex) {
        ForeignEnterpriseQ q = new ForeignEnterpriseQ();
        q.set企业名称(enterpriseName);
        q.set所属行业(industry);
        q.set企业状态(status);
        PageInfo<ForeignEnterprise> page = foreignEnterpriseService.selectAll(pageIndex == null ? 1 : pageIndex, q);
        return Map.of("total", page.getTotal(), "list", page.getList());
    }

    @Tool(description = "按ID查询单个外资企业的完整档案，包含注册资本、投资总额、投资方、国别地区、联系方式等全部字段")
    public ForeignEnterprise getForeignEnterpriseDetail(
            @ToolParam(description = "外资企业ID") Integer id) {
        return foreignEnterpriseService.selectById(id);
    }

    @Tool(description = "查询全部外资企业类型列表（企业类型字典）")
    public List<EnterpriseType> listEnterpriseTypes() {
        return foreignEnterpriseService.loadEnterpriseTypes();
    }

    @Tool(description = "分页查询地方招商项目，可按项目名称（模糊）、项目类型、所属行业、建设地点组合筛选，返回符合条件的总数和项目列表")
    public Map<String, Object> queryLocalProjects(
            @ToolParam(required = false, description = "项目名称，支持模糊匹配") String projectName,
            @ToolParam(required = false, description = "项目类型") String projectType,
            @ToolParam(required = false, description = "所属行业") String industry,
            @ToolParam(required = false, description = "建设地点") String location,
            @ToolParam(required = false, description = "页码，从1开始，默认1") Integer pageIndex) {
        LocalProjectInfoQ q = new LocalProjectInfoQ();
        q.set项目名称(projectName);
        q.set项目类型(projectType);
        q.set所属行业(industry);
        q.set建设地点(location);
        PageInfo<LocalProjectInfo> page = localProjectInfoService.selectAll(pageIndex == null ? 1 : pageIndex, q);
        return Map.of("total", page.getTotal(), "list", page.getList());
    }

    @Tool(description = "按ID查询单个地方招商项目的完整信息，包含总投资额、项目资本金、建设规模及内容、拟开工/建成时间等全部字段")
    public LocalProjectInfo getLocalProjectDetail(
            @ToolParam(description = "项目ID") Integer id) {
        return localProjectInfoService.selectById(id);
    }

    @Tool(description = "按名称模糊查询中介机构，返回机构证照、法定代表人、联系人、电话等信息")
    public IntermediaryAgency findAgencyByName(
            @ToolParam(description = "中介机构名称，完整名称") String agencyName) {
        return intermediaryAgencyService.findByAgencyName(agencyName);
    }

    @Tool(description = "按项目代码查询招商项目的审批流转记录（办理处室、办理状态、办理意见、办理时间），用于回答“某项目审批到哪一步了”")
    public List<ApprovalProcessInfo> queryApprovalProgress(
            @ToolParam(description = "项目代码") String projectCode) {
        // 直接走 mapper：演示场景不做数据权限过滤，正式环境可改走 Service 层由 @DataAccess 自动按 ownerId 过滤
        return approvalProcessInfoMapper.selectByProjectCode(projectCode);
    }

    @Tool(description = "按字典类型编码查询数据字典项列表，用于获取业务字段的合法枚举值，如企业状态、有效标识等")
    public List<DictionaryItem> getDictionaryItems(
            @ToolParam(description = "字典类型编码") String typeCode) {
        return dictionaryItemService.getItemsByTypeCode(typeCode);
    }

    // ==================== 统计汇总类工具（第二批） ====================

    @Tool(description = "统计外资企业按国别或地区的数量分布，用于回答“外资企业主要来自哪些国家/地区”“各国企业各有多少家”")
    public List<Map<String, Object>> countEnterprisesByCountry() {
        return foreignEnterpriseMapper.countByCountry();
    }

    @Tool(description = "统计外资企业按所属行业的数量分布，用于回答“外资企业都分布在哪些行业”“哪个行业的企业最多”")
    public List<Map<String, Object>> countEnterprisesByIndustry() {
        return foreignEnterpriseMapper.countByIndustry();
    }

    @Tool(description = "按拟开工年份汇总招商项目的数量和总投资额（含折合美元），用于回答“各年度有多少项目开工”“每年总投资规模多大”")
    public List<Map<String, Object>> sumInvestmentByYear() {
        return localProjectInfoMapper.sumInvestmentByYear();
    }

    @Tool(description = "统计审批事项按办理状态的数量分布，用于回答“当前有多少项目在审批中”“已发证/已退回的各有多少”")
    public List<Map<String, Object>> countApprovalByStatus() {
        return approvalProcessInfoMapper.countByStatus();
    }
}
