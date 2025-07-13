package com.xw.gateway.context;

import com.xw.gateway.service.ServiceInvoker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AttributeKey<T> {

    private static final Map<String, AttributeKey<?>> namedMap = new HashMap<>();

    //	到负责均衡之前，要通过具体的服务，获取对应的服务实例列表
    public static final AttributeKey<Set<String>> MATCH_ADDRESS = create(Set.class);

    public static final AttributeKey<ServiceInvoker> HTTP_INVOKER = create(ServiceInvoker.class);

    public static final AttributeKey<ServiceInvoker> DUBBO_INVOKER = create(ServiceInvoker.class);

    static {
        namedMap.put("MATCH_ADDRESS", MATCH_ADDRESS);
    }

    public static AttributeKey<?> valueOf(String name) {
        return namedMap.get(name);
    }

    /**
     * 将对象转成对应的class类型
     * @param value
     * @return
     */
    public abstract T cast(Object value);

    /**
     * 对外暴露创建AttributeKey
     * @param valueClass
     * @return
     * @param <T>
     */
    public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
        return new SimpleAttributeKey(valueClass);
    }

    /**
     * 简单的属性Key转换类
     * @param <T>
     */
    public static class SimpleAttributeKey<T> extends AttributeKey<T> {

        private final Class<T> valueClass;

        SimpleAttributeKey(final Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public T cast(Object value) {
            // Class.cast(Object obj)方法 就是作用就是强制类型转换。将obj转化成T类型
            return valueClass.cast(value);
        }

        @Override
        public String toString() {
            if(valueClass != null) {
                StringBuilder sb = new StringBuilder(getClass().getName());
                sb.append("<");
                sb.append(valueClass.getName());
                sb.append(">");
                return sb.toString();
            }
            return super.toString();
        }
    }

}
