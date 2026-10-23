package com.service;

import com.entity.IntermediaryAgency;
import com.github.pagehelper.PageInfo;

public interface IntermediaryAgencyService {
    PageInfo<IntermediaryAgency> findAll(Integer pageIndex, IntermediaryAgency q);
    IntermediaryAgency findByAgencyName(String 中介机构名称);
    int insert(IntermediaryAgency agency);
    int update(IntermediaryAgency agency);
    int deleteByAgencyName(String 中介机构名称);
    IntermediaryAgency findByAgencyId(Integer id);

}
