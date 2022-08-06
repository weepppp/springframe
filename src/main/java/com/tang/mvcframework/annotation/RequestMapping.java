package com.tang.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author weepppp 2022/8/6 8:35
 * 自定义RequestMapping注解
 **/
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
