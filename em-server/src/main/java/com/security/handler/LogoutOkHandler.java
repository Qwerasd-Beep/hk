package com.security.handler;

import com.constant.Const;
import com.entity.R;
import com.entity.UserInfo;
import com.util.JsonUtil;
import com.util.RedisUtil;
import com.util.RespUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutOkHandler implements LogoutSuccessHandler {
    @Resource
    private RedisUtil redisUtil;
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserInfo user = (UserInfo) authentication.getPrincipal();
        String redisKey = Const.REDIS_JWT_PREFIX+user.getId();
        redisUtil.remove(redisKey);
        RespUtil.write(response, JsonUtil.toJsonStr(R.OK()));

    }
}
