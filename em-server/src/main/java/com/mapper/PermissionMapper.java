package com.mapper;

import com.entity.MenuInfo;
import com.entity.RoleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionMapper {
    List<MenuInfo> getMenusByUserIdAndParentId(@Param("userId") int userId, @Param("parentId") int parentId);
    // 查询某个用户,有哪些角色
    List<RoleInfo> getRoleListByUserId(int id);

    // 查询用户的ui权限
    List<MenuInfo> getMenusByUserId(int userId);

    List<RoleInfo> getRoleList();

    List<MenuInfo> getMenuByParent(int i);

    List<Integer> getRoleMenuIdList(Integer roleId);

    void deleteRoleMenu(Integer roleId);

    void addRoleMenu(Integer roleId, Integer menuId);
}
