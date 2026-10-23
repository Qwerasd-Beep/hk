package com.service;

import com.entity.UserInfo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {
    UserInfo getUserById(int id);

    PageInfo<UserInfo> getUserList(UserInfo user ,Integer pageIndex);

    void addUser(UserInfo user);

    void updateUser(UserInfo user);

    void deleteUser(Integer id);

    void batchDelete(Integer[] ids);
}
