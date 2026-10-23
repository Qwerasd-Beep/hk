package com.aop;

import com.constant.Const;
import com.entity.BaseEntity;
import com.entity.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.util.JwtUtil;

import java.util.List;
import java.util.Objects;

@Component
@Aspect
public class DataAccessAspect {
    @Pointcut("@annotation(com.aop.DataAccess)")
    public void anyMeethod() {}

    @Around("anyMeethod()")
    public Object process(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest();

        String token=request.getHeader(Const.HK_TOKEN);
        UserInfo user=JwtUtil.parseUserFromJwt(token);
        List<String> roleList = user.getRoleList();

        //如果用户不是超级管理员,要进行数据权限限定
        if(!roleList.contains("ROLE_SYS_ADMIN")) {
            //得到拦到的方法的第一个参数,可能是类似 UserInfo, ApprovalProcessInfo 这样的对象, 继承自 BaseEntity ,里面含有 filterAccSQL
            Object firstParam=	point.getArgs()[0];
            if(Objects.nonNull(firstParam)&& firstParam instanceof BaseEntity) {
                MethodSignature sig = (MethodSignature)point.getSignature();
                DataAccess dataAcc= sig.getMethod().getAnnotation(DataAccess.class);

                //得到注解上传的参数
                String tableAlias=dataAcc.tableAlias();   //比如  a
                String fieldName=dataAcc.fieldName();   //比如 ownerId

                String sql="  AND "+tableAlias +"." +fieldName+"="+user.getId();
                ((BaseEntity)firstParam).setFilterAccSQL(sql);
            }
        }

        //放
        return point.proceed();
    }
}


