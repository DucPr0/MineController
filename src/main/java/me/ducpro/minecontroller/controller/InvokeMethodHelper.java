package me.ducpro.minecontroller.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeMethodHelper {
    /**
     * This function is necessary since in Kotlin, there is no way to call method.invoke() with an array of arguments.
     */
    public static Object invokeMethod(Method method, Object obj, Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }
}
