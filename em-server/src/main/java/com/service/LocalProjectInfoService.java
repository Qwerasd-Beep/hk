package com.service;

import com.entity.LocalProjectInfo;
import com.entity.query.LocalProjectInfoQ;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface LocalProjectInfoService {
    List<Map<String, Object>> sumInvestmentByYear();

    void updateById(LocalProjectInfo info);

    LocalProjectInfo selectById(Integer id);

    PageInfo<LocalProjectInfo> selectAll(int pageIndex, LocalProjectInfoQ q);
}
