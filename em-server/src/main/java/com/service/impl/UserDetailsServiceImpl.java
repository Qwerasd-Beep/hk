package com.service.impl;

import com.entity.MenuInfo;
import com.entity.RoleInfo;
import com.entity.UserInfo;
import com.mapper.PermissionMapper;
import com.mapper.UserMapper;
import com.security.userdetails.HKUserDetails;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userMapper.loginByName(username);
        if(user!=null){
            List<RoleInfo> roleInfoList = permissionMapper.getRoleListByUserId(user.getId());
            List<String > roleList = new ArrayList<String>();
            roleInfoList.forEach(roleInfo->{
                roleList.add(roleInfo.getRoleCode());
            });
            user.setRoleList(roleList);
            List < MenuInfo> permList = permissionMapper.getMenusByUserId(user.getId());
            List<String> permissionList = new ArrayList<>();
            permList.forEach(menu->{
                if(StringUtils.hasText(menu.getCode())){
                    permissionList.add(menu.getCode());
                }
            });
            user.setPermissionList(permissionList);
            return new HKUserDetails(user);
        }
        else{
            throw new UsernameNotFoundException("用户帐号不存在");
        }
    }
}
