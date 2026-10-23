package com.entity;

import lombok.Data;

@Data
public class IntermediaryAgency {
    private DictionaryItem 有效标识;  //注意这里这个数据类型,用的是字典类型
    private Integer 有效标识ID;  //非数据库字段,主要用于查询
    private String 中介机构名称;
    private String 中介机构证照类型;
    private String 中介机构证照号码;
    private String 法定代表人;
    private String 联系人名称;
    private String 联系电话;
    private String 联系手机;
    private String 联系人邮箱;
    private String 传真;
    private String 通讯地址;
}
