package com.mapper;

import com.entity.LocalProjectInfo;
import com.entity.query.LocalProjectInfoQ;

import java.util.List;
import java.util.Map;

public interface LocalProjectInfoMapper {
    // 按拟开工年份汇总项目数量和总投资额（AI 统计工具用）
    List<Map<String, Object>> sumInvestmentByYear();

    LocalProjectInfo selectById(Integer id);

    List<LocalProjectInfo> selectAll(LocalProjectInfoQ q);

    int updateById(LocalProjectInfo info);
}
