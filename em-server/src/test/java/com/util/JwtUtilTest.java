package com.util;

import com.entity.UserInfo;
import com.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JwtUtilTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void test1() {
        UserInfo user = userMapper.getUserById(1);
        String jwt = JwtUtil.generateJwt(user);
        System.out.println(jwt);
        UserInfo u = JwtUtil.parseUserFromJwt(jwt);
        System.out.println(u);
    }
}
