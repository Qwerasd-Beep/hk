package com.util;

import com.entity.UserInfo;
import com.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JsonUtilTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void test() {
        UserInfo user = userMapper.getUserById(1);
        String jsonStr = JsonUtil.toJsonStr(user);
        System.out.println(jsonStr);

        UserInfo u = JsonUtil.toBean(jsonStr, UserInfo.class);
        System.out.println(u);
    }

}
