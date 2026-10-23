package com.controller;

import com.entity.MenuInfo;
import com.entity.R;
import com.entity.UserInfo;
import com.github.pagehelper.PageInfo;
import com.security.userdetails.HKUserDetails;
import com.service.PermissionService;
import com.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private PermissionService permissionService;

    @RequestMapping("/test")
    public R test(int id) {
        UserInfo user = userService.getUserById(id);
        return R.OK(user);
    }

    @GetMapping("/login-info")
    public R loginInfo(Authentication authentication) {
        UserInfo user = (UserInfo) authentication.getPrincipal();
        List<MenuInfo> menuList = permissionService.getMenusByUserIdAndParentId(user.getId(), 0);
        user.setMenuList(menuList);
        return R.OK(user);
    }

    @GetMapping("/login-auto")
    public R loginAuto(){
        return R.OK();
    }

    @GetMapping("/users")
    public R getUserList(@RequestParam(required = false, defaultValue = "1") Integer pageIndex){
        UserInfo user = new UserInfo();
        PageInfo<UserInfo> page = userService.getUserList(user,pageIndex);
        return R.OK(page);
    }

    @GetMapping("/user/{id}")
    public R getUserById(@PathVariable int id){
        UserInfo user = userService.getUserById(id);
        return R.OK(user);
    }

    @PostMapping("/user-add")
    public R addUser(@RequestBody UserInfo user){
        userService.addUser(user);
        return R.OK();
    }

    @PostMapping("/user-update")
    public R updateUser(@RequestBody UserInfo user){
        userService.updateUser(user);
        return R.OK();
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/user-delete/{id}")
    public R deleteUser(@PathVariable(value = "id")Integer id){
        userService.deleteUser(id);
        return R.OK();
    }

    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/user-batch-delete")
    public R deleteUserBatch(@RequestParam("ids") Integer[] ids){
        userService.batchDelete(ids);
        return R.OK();
    }
}
