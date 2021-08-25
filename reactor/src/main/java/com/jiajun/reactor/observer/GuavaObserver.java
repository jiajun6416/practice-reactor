package com.jiajun.reactor.observer;

import lombok.AllArgsConstructor;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * Guava中的Observer实现方案, 即EventBus
 * 1. 支持多个事件
 * 2. 支持同步和异步
 * 3. 观察者无需定义单独的接口
 *
 * @author jiajun
 */
public class GuavaObserver {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Subscribe {

    }

    /**
     * @see com.google.common.eventbus.EventBus
     * 被观察者对象
     */
    private static class EventBus {

        @AllArgsConstructor
        static class MethodInvoker {

            private Method method;

            private Object obj;

            public void invoke(Object parameter) {
                try {
                    method.invoke(obj, parameter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private Map<Class, List<MethodInvoker>> subscribeMap = new HashMap<>();

        /**
         * 实际中需要过滤掉基本类型, 数组, 集合类型
         *
         * @param object
         */
        public void register(Object object) {
            // 可使用spring中的工具类MethodIntrospector进行内部迭代过滤
            Method[] methods = object.getClass().getDeclaredMethods();
            Stream.of(methods)
                    .filter(method -> {
                        if (method.getAnnotation(Subscribe.class) == null) {
                            return false;
                        }
                        if (method.getParameterCount() != 1) {
                            return false;
                        }
                        Class<?> parameterType = method.getParameterTypes()[0];
                        // 不能是基本类型, 不能是array
                        if (parameterType.isPrimitive() || parameterType.isArray()) {
                            return false;
                        }
                        return true;
                    })
                    .forEach(method -> {
                        Class eventType = method.getParameterTypes()[0];
                        List<MethodInvoker> subscribes = subscribeMap.getOrDefault(eventType, new ArrayList<>());
                        subscribes.add(new MethodInvoker(method, object));
                        subscribeMap.put(eventType, subscribes);
                    });
        }

        public void post(Object event) {
            if (event == null) return;
            Class<?> eventType = event.getClass();
            subscribeMap.getOrDefault(eventType, Collections.emptyList())
                    .forEach(methodInvoker -> methodInvoker.invoke(event));
        }
    }

    @Test
    public void testEventBus() {
        EventBus eventBus = new EventBus();
        eventBus.register(new Subscribe1());
        eventBus.register(new Subscribe2());

        eventBus.post(new Event1());
        eventBus.post(new Event2());
    }

    public static class Event1 {

    }

    public static class Event2 {

    }

    public static class Subscribe1 {

        @Subscribe
        public void onEvent(Event1 event) {
            System.out.println("subscribe1 on event1");
        }

        @Subscribe
        public void onEvent(Event2 event) {
            System.out.println("subscribe1 on event2");
        }
    }

    public static class Subscribe2 {

        @Subscribe
        public void onEvent(Event1 event) {
            System.out.println("subscribe2 on event1");
        }

        @Subscribe
        public void onEvent(Event2 event) {
            System.out.println("subscribe2 on event2");
        }
    }
}
