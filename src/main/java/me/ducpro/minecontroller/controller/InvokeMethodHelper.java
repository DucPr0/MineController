package me.ducpro.minecontroller.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeMethodHelper {
    public static Object invokeMethod(Method method, Object obj, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }
}
