package com.service.impl;

import com.constant.Const;
import com.entity.LocalProjectInfo;
import com.entity.query.LocalProjectInfoQ;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mapper.LocalProjectInfoMapper;
import com.service.LocalProjectInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LocalProjectInfoServiceImpl implements LocalProjectInfoService {

    @Resource
    private LocalProjectInfoMapper mapper;

    @Override
    public List<Map<String, Object>> sumInvestmentByYear() {
        return mapper.sumInvestmentByYear();
    }

    @Override
    public LocalProjectInfo selectById(Integer id) {
        return mapper.selectById(id);
    }

    @Override
    public PageInfo<LocalProjectInfo> selectAll(int pageIndex, LocalProjectInfoQ q) {
        PageHelper.startPage(pageIndex, Const.PAGE_SIZE);
        return new PageInfo<>(mapper.selectAll(q));
    }

    @Override
    public void updateById(LocalProjectInfo info) {
        mapper.updateById(info);
    }
}
