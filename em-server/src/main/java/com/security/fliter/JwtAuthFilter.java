package com.security.fliter;

import com.constant.Const;
import com.entity.R;
import com.entity.UserInfo;
import com.enums.RCodeEnum;
import com.util.JsonUtil;
import com.util.JwtUtil;
import com.util.RedisUtil;
import com.util.RespUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private ThreadPoolTaskExecutor pool;
    @Autowired
    private RedisUtil redisUtil;

    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String uri = req.getRequestURI();
        if(uri.contains("favicon.ico") ||uri.contains("swagger")||uri.contains("api-docs")
                ||uri.contains("doc.html") ||uri.contains("webjars") ||uri.contains("ai")
                ||uri.contains("tool")
                || uri.startsWith("/sse")) { // MCP Server 的 SSE 端点（演示环境放行，生产应加鉴权）
            chain.doFilter(req, resp);
            return;
        }
        if(uri.equals(Const.LOGIN_URI)){
            chain.doFilter(req, resp);
        } else{
            String token = req.getHeader(Const.HK_TOKEN);
            if(!StringUtils.hasText(token)){
                R r = R.ERROR(RCodeEnum.TOKEN_IS_EMPTY);
                RespUtil.write(resp, JsonUtil.toJsonStr(r));
                return;
            }
            if(!JwtUtil.verifyJwt(token)){
                R r = R.ERROR(RCodeEnum.TOKEN_IS_ERROR);
                RespUtil.write(resp, JsonUtil.toJsonStr(r));
                return;
            }
            UserInfo user = JwtUtil.parseUserFromJwt(token);
            String redisKey = Const.REDIS_JWT_PREFIX + user.getId();
            String redisValue = redisUtil.get(redisKey);
            if(!StringUtils.hasText(redisValue)){
                R r = R.ERROR(RCodeEnum.TOKEN_IS_EMPTY);
                RespUtil.write(resp, JsonUtil.toJsonStr(r));
                return;
            }
            if(!redisValue.equals(token)){
                R r = R.ERROR(RCodeEnum.TOKEN_IS_NONE_MATCH );
                RespUtil.write(resp, JsonUtil.toJsonStr(r));
                return;
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.authorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            pool.execute(() -> {
                boolean rMe = Boolean.parseBoolean(req.getHeader("rememberMe"));
                if(rMe){
                    redisUtil.expire(redisKey,Const.EXPIRE_TIME, TimeUnit.SECONDS);
                }
                else{
                    redisUtil.expire(redisKey,Const.DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
                }
            });
            chain.doFilter(req, resp);

        }
    }



}
