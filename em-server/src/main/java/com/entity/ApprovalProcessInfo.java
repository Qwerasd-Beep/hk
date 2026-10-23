package com.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ApprovalProcessInfo extends BaseEntity {
    // 显式指定日期序列化格式：Spring AI 工具结果用的是内置 ObjectMapper（默认输出时间戳），不读应用的全局格式配置
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date 办理时间;

    private int id;
    private int ownerId;  //所属人,注意,将来用这个字段,控制数据权限
    private String 项目代码;
    private Integer 审批事项ID;
    private String 办理处室;
    private String 办理状态;
    private String 办理意见;

    DictionaryItem 有效标识;  //关联数据字典
    private String 有效标识ID;  //非数据库字段,主要用于查询
}

