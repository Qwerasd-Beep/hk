package com.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ForeignEnterprise {
    private Integer id;

    //一对一, 关联企业类型表
    private EnterpriseType enterpriseType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date 录入时间;
    private String 录入人;
    private Integer 录入人部门id;
    private String 数据来源;
    private Integer 是否已删除;

    private Integer 用户表主键id;
    private String 企业状态;
    private String 企业代码;
    private String 企业名称;
    private String 新批或变更;
    private String 企业类型;
    private String 行业门类;
    private String 所属行业;
    private String 行业代码;
    private String 是否高技术产业;
    private String 统计部门;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date 成立日期;
    private String 设立方式;
    private String 企业地址;
    private String 经营范围;
    private String 联系人;
    private String 固定电话;
    private String 联系电话;
    private String 邮箱;
    private String 投资总额;
    private BigDecimal 注册资本;
    private String 外商出资金额;
    private String 外商出资增减;
    private String 投资者名称;
    private String 国别或地区;
    private String 资金来源地;
    private String 出资比例;
}
