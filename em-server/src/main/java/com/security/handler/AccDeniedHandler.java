package com.security.handler;

import com.entity.R;
import com.util.JsonUtil;
import com.util.RespUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        R r = R.ERROR("没有权限访问，访问被拒绝，访问路径："+request.getRequestURI());
        RespUtil.write(response, JsonUtil.toJsonStr(r));
    }
}
