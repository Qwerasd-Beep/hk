package com.security.userdetails;

import com.entity.MenuInfo;
import com.entity.RoleInfo;
import com.entity.UserInfo;
import com.mapper.PermissionMapper;
import com.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userMapper.loginByName(username);
        if(user!=null){
            List<String> roleList = new ArrayList<>();
            for (RoleInfo role : permissionMapper.getRoleListByUserId(user.getId())) {
                if (StringUtils.hasText(role.getRoleCode())) {
                    roleList.add(role.getRoleCode());
                }
            }
            user.setRoleList(roleList);

            List<String> permissionList = new ArrayList<>();
            for (MenuInfo menu : permissionMapper.getMenusByUserId(user.getId())) {
                if (StringUtils.hasText(menu.getCode())) {
                    permissionList.add(menu.getCode());
                }
            }
//            permissionList.remove("zhongjie_jigou:list");
            user.setPermissionList(permissionList);
            return new HKUserDetails(user);
        }
        else{
            throw new UsernameNotFoundException("用户账号不存在");
        }

    }
}
