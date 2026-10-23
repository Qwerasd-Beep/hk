package com.aop;

import com.entity.R;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public R handlerException(Exception e) throws Exception {
        e.printStackTrace();
        if(e instanceof AuthorizationDeniedException){
            throw e;
        }
        return R.ERROR(e.getMessage());
    }
}
