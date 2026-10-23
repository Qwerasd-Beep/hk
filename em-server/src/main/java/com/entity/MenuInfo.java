package com.entity;

import lombok.Data;

import java.util.List;

@Data
public class MenuInfo {
    private int id;
    private String menuName;
    private String code;
    private String url;
    private String type;
    private int parentId;
    private int orderNo;
    private String icon;

    private List<MenuInfo> subMenuList;	//子菜单

}
