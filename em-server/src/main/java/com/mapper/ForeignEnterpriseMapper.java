package com.mapper;

import com.entity.EnterpriseType;
import com.entity.ForeignEnterprise;
import com.entity.query.ForeignEnterpriseQ;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author w
* @description 针对表【外企监测_企业库_企业信息】的数据库操作Mapper
* @createDate 2026-10-09 09:44:42
* @Entity com.entity.ForeignEnterprise
*/
@Mapper
public interface ForeignEnterpriseMapper {

    // 插入
    int insert(ForeignEnterprise e);
    // 删除
    int deleteById(Integer id);
    // 更新
    int update(ForeignEnterprise e);
    // 查询
    ForeignEnterprise selectById(Integer id);
    // 分页查询
    List<ForeignEnterprise> selectAll(ForeignEnterpriseQ q);


    List<EnterpriseType> loadEnterpriseTypes();

    // 按国别或地区统计企业数量（AI 统计工具用）
    List<Map<String, Object>> countByCountry();
    // 按所属行业统计企业数量（AI 统计工具用）
    List<Map<String, Object>> countByIndustry();

    // AI 报表导出工具用：全量未删除企业（不分页），可按所属行业、国别或地区模糊筛选
    List<ForeignEnterprise> selectAllForExport(@Param("industry") String industry, @Param("country") String country);
}
