package com.yh.learn.annotation;

import java.lang.annotation.*;

/**
 * Created by yanghua on 2019/3/24.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyComponent {
}
