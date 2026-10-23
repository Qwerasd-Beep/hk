package com.service;

import com.entity.MenuInfo;
import com.entity.RoleInfo;

import java.util.List;

public interface PermissionService {
    //它用于前台用户呈现导航, 查询某个用户的菜单权限
    List<MenuInfo> getMenusByUserIdAndParentId(int userId, int parentId);

    List<RoleInfo> getRoleList();

    List<MenuInfo> getAllMenu();

    List<Integer> getRoleMenuIdList(Integer roleId);

    void updateRoleMenu(Integer roleId, List<Integer> checkedMenuIds);
}
