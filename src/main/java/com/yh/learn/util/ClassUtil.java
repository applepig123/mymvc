package com.yh.learn.util;

/**
 * Created by yanghua on 2019/3/24.
 */
public class ClassUtil {

    public static String toLoweFirstWord(String str) {
        char[] charArray = str.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

    public static String getRootPath() {
        return ClassUtil.class.getResource("/").getPath().substring(1);
    }
}
