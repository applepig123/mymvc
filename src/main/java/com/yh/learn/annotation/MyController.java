package com.yh.learn.annotation;

import java.lang.annotation.*;

/**
 * Created by yanghua on 2019/3/24.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MyComponent
public @interface MyController {
    /**
     * 用来给controller注册别名
     * @return
     */
    String value() default "";
}
