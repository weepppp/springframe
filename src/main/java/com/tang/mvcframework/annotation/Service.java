package com.tang.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author weepppp 2022/8/6 8:27
 * 自定义Service注解
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
}
