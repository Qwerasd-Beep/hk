package com.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class LocalProjectInfo {

    private Integer id;
    private String 项目代码;
    private String 所属行政区划代码;
    private String 是否外商投资或境外投资;
    private String 项目名称;
    private String 项目类型;
    private String 建设性质;
    private Integer 拟开工时间;
    private Integer 拟建成时间;
    private BigDecimal 总投资额;
    private String 总投资额为零时说明;
    private String 建设地点;
    private String 建设地点详情;
    private String 国标行业;
    private String 所属行业;
    private String 建设规模及内容;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date 申报时间;

    private String 投资项目目录编码;
    private String 项目属性;
    private String 有效标识;
    private String 是否涉及国家安全;
    private String 安全审查决定文号;
    private String 投资方式;
    private BigDecimal 总投资额折合美元;
    private BigDecimal 总投资额使用的汇率;
    private BigDecimal 项目资本金;
    private BigDecimal 项目资本金折合美元;
    private BigDecimal 项目资本金使用的汇率;
    private String 适用产业政策条目类型;
    private String 适用产业政策条目;
    private String 其他投资方式需予以申报的情况;
    private String 提供交易双方情况;
    private String 并购安排;
    private String 并购后经营方式及经营范围;
    private String 土地获取方式;
    private BigDecimal 总用地面积;
    private BigDecimal 总建筑面积;
    private String 是否新增设备;
    private String 拟进口设备数量及金额;
    private String 项目所在地;
    private BigDecimal 中方投资额;
    private String 境外投资项目投资方式;
}
