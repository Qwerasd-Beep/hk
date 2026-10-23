package com.service;

import com.entity.EnterpriseType;
import com.entity.ForeignEnterprise;
import com.github.pagehelper.PageInfo;
import com.entity.query.ForeignEnterpriseQ;

import java.util.List;

public interface ForeignEnterpriseService {
    // 插入
    int insert(ForeignEnterprise e);
    // 删除
    int deleteById(Integer id);
    // 更新
    int update(ForeignEnterprise e);
    // 查询
    ForeignEnterprise selectById(Integer id);

    PageInfo<ForeignEnterprise> selectAll(Integer pageIndex, ForeignEnterpriseQ q);

    List<EnterpriseType> loadEnterpriseTypes();
}
