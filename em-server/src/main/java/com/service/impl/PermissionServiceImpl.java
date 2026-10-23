package com.service.impl;

import com.entity.MenuInfo;
import com.entity.RoleInfo;
import com.mapper.PermissionMapper;
import com.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionMapper mapper;

    @Override
    public List<MenuInfo> getMenusByUserIdAndParentId(int userId, int parentId) {
        List<MenuInfo> list = mapper.getMenusByUserIdAndParentId(userId,parentId);
        for(MenuInfo menuInfo:list){
            if(menuInfo.getParentId()==0){
                menuInfo.setSubMenuList(getMenusByUserIdAndParentId(userId,menuInfo.getId()));
            }
        }
        return list;
    }

    @Override
    public List<RoleInfo> getRoleList() {
        return mapper.getRoleList();
    }

    public List<MenuInfo> getAllMenu() {
        List<MenuInfo> rootList=mapper.getMenuByParent(0);
        for(MenuInfo m:rootList) {
            List<MenuInfo> secondMenuList=mapper.getMenuByParent(m.getId());
            for(MenuInfo mSecond:secondMenuList) {
                List<MenuInfo> treeMenuList=mapper.getMenuByParent(mSecond.getId());
                mSecond.setSubMenuList(treeMenuList);
            }
            m.setSubMenuList(secondMenuList);
        }
        return rootList;
    }
    public List<Integer> getRoleMenuIdList(Integer roleId) {
        return mapper.getRoleMenuIdList( roleId);
    }

    public void updateRoleMenu(Integer roleId, List<Integer> checkedMenuIds) {
        //先删除旧的,再添加新的
        mapper.deleteRoleMenu(roleId);

        //再添加
        for(Integer menuId:checkedMenuIds ) {
            mapper.addRoleMenu(roleId,menuId);
        }
    }


}
