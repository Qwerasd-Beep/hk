package com.mapper;

import com.entity.ApprovalProcessInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ApprovalProcessInfoMapper {
    int insert(@Param("o") ApprovalProcessInfo o);
    int deleteById(int id);
    int update(ApprovalProcessInfo o);
    ApprovalProcessInfo selectById(int id);
    List<ApprovalProcessInfo> selectAll(ApprovalProcessInfo q);

    // 按项目代码查询审批流转记录（AI 工具用，绕开 @DataAccess 数据权限）
    List<ApprovalProcessInfo> selectByProjectCode(@Param("projectCode") String projectCode);
    // 按办理状态统计审批事项数量（AI 统计工具用）
    List<Map<String, Object>> countByStatus();

}
