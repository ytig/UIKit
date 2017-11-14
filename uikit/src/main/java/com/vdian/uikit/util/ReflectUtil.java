package com.vdian.uikit.util;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by zhangliang on 16/11/11.
 */
public class ReflectUtil {
    private static HashMap<String, Field> cache = new HashMap<>();

    public static Field getDeclaredField(String className, String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        String key = className + " " + fieldName;
        Field value = cache.get(key);
        if (value == null) {
            value = Class.forName(className).getDeclaredField(fieldName);
            if (value != null) {
                value.setAccessible(true);
                cache.put(key, value);
            }
        }
        return value;
    }
}
