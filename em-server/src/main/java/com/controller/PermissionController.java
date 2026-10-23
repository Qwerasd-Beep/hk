package com.controller;

import com.entity.MenuInfo;
import com.entity.R;
import com.entity.RoleInfo;
import com.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("perm")
public class PermissionController {
    @Resource
    private PermissionService permissionService;

    // 查询所有角色
    @GetMapping(value = "/role-list")
    public R userPage() {
        List<RoleInfo> roleList = permissionService.getRoleList();
        return R.OK(roleList);
    }

    // 查询所有菜单,用于角色授权
    @GetMapping(value = "/all-menu_role-menus")
    public R getAllMenu(Integer roleId) {
        //查询所有菜单(含子级)
        List<MenuInfo> menuList = permissionService.getAllMenu();

        //查询某个角色下的所有 menuId
        List<Integer> roleMenuIdList = permissionService.getRoleMenuIdList(roleId);

        Map<String, Object> map = new HashMap<>();
        map.put("roleMenuIdList", roleMenuIdList);
        map.put("menuList", menuList);

        return R.OK(map);
    }

    @PostMapping("/update-rolemenu")
    public R updateRoleMenu(@RequestBody Map<String,Object> data){
        Integer roleId=Integer.parseInt(data.get("id").toString());
        String roleCode =(String)data.get("roleCode");
        String des=(String)data.get("des");
        String roleName=(String)data.get("roleName");

        System.out.println(roleId+" "+ roleName+ " "+roleCode + " "+ des );

        List<Integer> checkedMenuIds =(List<Integer>)data.get("checkedMenuIds");
        permissionService.updateRoleMenu(roleId,checkedMenuIds);
        return R.OK("角色权限更新成功");
    }


}

