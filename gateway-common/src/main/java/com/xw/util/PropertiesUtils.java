package com.xw.util;

import java.lang.reflect.Method;
import java.util.Properties;

public class PropertiesUtils {

    public static void properties2Object(Properties properties, Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                try {
                    String first = methodName.substring(3, 4);
                    String extra = methodName.substring(4);
                    String key = first.toLowerCase() + extra;
                    String property = properties.getProperty(key);
                    if (property != null) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes!=null && parameterTypes.length==1) {
                            String simpleName = parameterTypes[0].getSimpleName();
                            Object arg = null;
                            if (simpleName.equals("int") || simpleName.equals("Integer")) {
                                arg = Integer.parseInt(property);
                            } else if (simpleName.equals("long") || simpleName.equals("Long")) {
                                arg = Long.parseLong(property);
                            } else if (simpleName.equals("double") || simpleName.equals("Double")) {
                                arg = Double.parseDouble(property);
                            } else if (simpleName.equals("boolean") || simpleName.equals("Boolean")) {
                                arg = Boolean.parseBoolean(property);
                            } else if (simpleName.equals("float") || simpleName.equals("Float")) {
                                arg = Float.parseFloat(property);
                            } else if (simpleName.equals("String")) {
                                arg = property;
                            } else {
                                continue;
                            }
                            method.invoke(object, arg);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

}
