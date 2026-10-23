package com.service.impl;

import com.constant.Const;
import com.entity.IntermediaryAgency;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mapper.IntermediaryAgencyMapper;
import com.service.IntermediaryAgencyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntermediaryAgencyServiceImpl implements IntermediaryAgencyService {
    @Resource
    private IntermediaryAgencyMapper mapper;

    public PageInfo<IntermediaryAgency> findAll(Integer pageIndex, IntermediaryAgency q) {
        PageHelper.startPage(pageIndex, Const.PAGE_SIZE);
        List<IntermediaryAgency> list =mapper.findAll(q);
        return  new PageInfo<>(list);
    }

    public IntermediaryAgency findByAgencyName(String 中介机构名称) {
        return mapper.findByAgencyName(中介机构名称);
    }

    public IntermediaryAgency findByAgencyId(Integer id) {
        return mapper.findByAgencyId(id);
    }

    public int insert(IntermediaryAgency agency) {
        return mapper.insert(agency);
    }

    public int update(IntermediaryAgency agency) {
        return mapper.update(agency);
    }

    public int deleteByAgencyName(String 中介机构名称) {
        return mapper.deleteByAgencyName(中介机构名称);
    }
}
