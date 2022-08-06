package com.tang.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author weepppp 2022/8/6 8:31
 * 自定义Autowired注解
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
