package com.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataAccess {
    //传入表名或表别名
    public String tableAlias() default "";

    //传入字段名
    public String fieldName() default "";
}

