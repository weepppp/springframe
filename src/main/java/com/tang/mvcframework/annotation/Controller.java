package com.tang.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author weepppp 2022/8/6 8:34
 * 自定义Controller注解
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
