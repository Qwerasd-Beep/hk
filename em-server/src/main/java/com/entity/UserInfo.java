package com.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
public class UserInfo extends BaseEntity {
    private int id;
    private String name;
    @JsonIgnore
    private String password;
    private String note;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date editTime;

    //用户所拥用的全部菜单(它不参与token的生成,因为它内容太多,将来用到的时候现查)
    private  List<MenuInfo> menuList;

    //用户拥有的角色列表,存的是roleCode字段 , 实际上系统中目前并没用上这个 roleLis
    private List<String> roleList;

    //这个存的是具体的功能权限(ui权限), 比如 enterprise:add , 因为一个用户有多个角色,所以底层查询用了 distinct
    private List<String> permissionList;


    public Collection<? extends GrantedAuthority> authorities(){
        List<String> list = new ArrayList<>();
        if (roleList != null) {
            list.addAll(roleList);
        }
        if (permissionList != null) {
            list.addAll(permissionList);
        }
        return AuthorityUtils.createAuthorityList(list);
    }
}
