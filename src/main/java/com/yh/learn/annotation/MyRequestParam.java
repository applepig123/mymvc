package com.yh.learn.annotation;

import java.lang.annotation.*;

/**
 * Created by yanghua on 2019/3/24.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    /**
     * 表示参数名称
     * @return
     */
    String value() default "";
}
