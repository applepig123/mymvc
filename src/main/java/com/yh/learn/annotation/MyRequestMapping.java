package com.yh.learn.annotation;

import java.lang.annotation.*;

/**
 * Created by yanghua on 2019/3/24.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    /**
     * 用来表示访问的url
     * @return
     */
    String value() default "";
}
