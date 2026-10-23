package com.util;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisUtilTest {
    @Resource
    private RedisUtil redisUtil;
    @Test
    public void test() throws InterruptedException {
        redisUtil.set("str","china");
        redisUtil.expire("str",4, TimeUnit.SECONDS);
        System.out.println(redisUtil.get("str"));
        Thread.sleep(5000);
        System.out.println(redisUtil.get("str"));
    }

}
