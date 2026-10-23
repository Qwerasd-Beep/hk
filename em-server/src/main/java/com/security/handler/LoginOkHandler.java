package com.security.handler;

import com.constant.Const;
import com.entity.R;
import com.entity.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.userdetails.HKUserDetails;
import com.util.JsonUtil;
import com.util.JwtUtil;
import com.util.RedisUtil;
import com.util.RespUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class LoginOkHandler implements AuthenticationSuccessHandler {
    @Resource
    private RedisUtil redisUtil;
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HKUserDetails user = (HKUserDetails) authentication.getPrincipal();
        UserInfo user1 = user.getUser();
        String jwt = JwtUtil.generateJwt(user1);
        String redisKey = Const.REDIS_JWT_PREFIX+user.getId();
        redisUtil.set(redisKey,jwt);
        boolean rMe = Boolean.getBoolean(request.getParameter("rememberMe"));
        if(rMe==true){
            redisUtil.expire(redisKey,Const.EXPIRE_TIME, TimeUnit.SECONDS);
        } else{
            redisUtil.expire(redisKey,Const.DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
        }
        R r = R.OK(jwt);
        RespUtil.write(response,JsonUtil.toJsonStr(r));


    }

}
