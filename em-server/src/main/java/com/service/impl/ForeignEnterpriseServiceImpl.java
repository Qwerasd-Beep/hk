package com.service.impl;

import com.constant.Const;
import com.entity.EnterpriseType;
import com.entity.ForeignEnterprise;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mapper.ForeignEnterpriseMapper;
import com.entity.query.ForeignEnterpriseQ;
import com.service.ForeignEnterpriseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForeignEnterpriseServiceImpl implements ForeignEnterpriseService {
    @Resource
    private ForeignEnterpriseMapper mapper;

    public int insert(ForeignEnterprise e) {
        return mapper.insert(e);
    }
    public int deleteById(Integer id) {
        return mapper.deleteById(id);
    }
    public int update(ForeignEnterprise e) {
        return mapper.update(e);
    }
    public ForeignEnterprise selectById(Integer id) {
        return mapper.selectById(id);
    }
    public PageInfo<ForeignEnterprise> selectAll(Integer pageIndex, ForeignEnterpriseQ q) {
        PageHelper.startPage(pageIndex, Const.PAGE_SIZE);
        List<ForeignEnterprise> list = mapper.selectAll(q);
        return new PageInfo<>(list);
    }

    @Override
    public List<EnterpriseType> loadEnterpriseTypes() {
        return mapper.loadEnterpriseTypes();
    }

}
