package com.tang.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author weepppp 2022/8/6 8:37
 * 自定义RequestParam注解
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}
