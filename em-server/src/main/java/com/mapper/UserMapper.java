package com.mapper;

import com.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    UserInfo loginByName(String name);
    UserInfo getUserById(int id);

    List<UserInfo> getUserList(UserInfo user);

    void addUser(UserInfo user);

    void updateUser(UserInfo user);

    void deleteUser(Integer id);

    void batchDelete(@Param("ids") Integer[] ids);

    /** AI 工具用：只读用户列表（显式列、不含密码；绕过 @DataAccess 数据权限片段），可按姓名模糊筛选 */
    List<UserInfo> selectAllUsers(@Param("keyword") String keyword);
}
