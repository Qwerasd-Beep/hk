package com.security.handler;

import com.entity.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.util.JsonUtil;
import com.util.RespUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("text/html;charset=UTF-8");

        R r = R.ERROR(exception.getLocalizedMessage());
        String jsonStr = JsonUtil.toJsonStr(r);
        RespUtil.write(response, jsonStr);
    }
}
