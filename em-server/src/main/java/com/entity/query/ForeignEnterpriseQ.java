package com.entity.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ForeignEnterpriseQ {
    private String  企业名称 ;
    private String  所属行业 ;
    private String  企业状态 ;
    private Integer 企业类型ID;  //将来查询企业类型的时候用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
