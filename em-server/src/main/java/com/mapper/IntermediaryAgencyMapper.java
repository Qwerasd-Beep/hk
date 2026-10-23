package com.mapper;

import com.entity.IntermediaryAgency;

import java.util.List;

public interface IntermediaryAgencyMapper {
    List<IntermediaryAgency> findAll(IntermediaryAgency q);
    IntermediaryAgency findByAgencyName(String 中介机构名称);
    int insert(IntermediaryAgency agency);
    int update(IntermediaryAgency agency);
    int deleteByAgencyName(String 中介机构名称);

    IntermediaryAgency findByAgencyId(Integer id);
}
