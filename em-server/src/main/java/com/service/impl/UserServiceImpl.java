package com.service.impl;

import com.aop.DataAccess;
import com.constant.Const;
import com.entity.UserInfo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mapper.UserMapper;
import com.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserMapper userMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Override
    public UserInfo getUserById(int id) {
        return userMapper.getUserById(id);
    }

    @Override
    @DataAccess(tableAlias = "userInfo",fieldName = "id")
    public PageInfo<UserInfo> getUserList(UserInfo user ,Integer pageIndex) {
        PageHelper.startPage(pageIndex, Const.PAGE_SIZE);
        List<UserInfo> userList = userMapper.getUserList(user);
        return new PageInfo<>(userList);
    }

    @Override
    public void addUser(UserInfo user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.addUser(user);
    }

    @Override
    public void updateUser(UserInfo user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setEditTime(new Date());
        userMapper.updateUser(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteUser(id);
    }

    @Override
    public void batchDelete(Integer[] ids) {
        userMapper.batchDelete(ids);
    }
}
