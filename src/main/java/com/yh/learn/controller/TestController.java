package com.yh.learn.controller;

import com.yh.learn.annotation.MyController;
import com.yh.learn.annotation.MyRequestMapping;

/**
 * Created by yanghua on 2019/3/24.
 */
@MyController
public class TestController {

    @MyRequestMapping(value = "test")
    public String test() {
        return "hello";
    }
}
