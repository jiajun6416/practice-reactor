package com.jiajun.reactor.observer;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * apache common中通过代理实现的观察者模式, 使用上很简单
 * 1. 使用上简单
 * 2. 只支持单一事件的广播
 *
 * @author jiajun
 */
public class ApacheCommonObserver {

    /**
     * @param <T>
     * @see org.apache.commons.lang3.event.EventListenerSupport
     * 单一的事件广播器
     */
    static class EventListenerSupport<T> {

        public List<T> subscribers = new ArrayList<>();

        T broadCast;

        public EventListenerSupport(Class<T> eventListener) {
            if (!eventListener.isInterface()) {
                throw new RuntimeException("must interface");
            }
            broadCast = (T) Proxy.newProxyInstance(eventListener.getClassLoader(), new Class[]{eventListener}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    for (T subscriber : subscribers) {
                        method.invoke(subscriber, args);
                    }
                    // 不能又返回值
                    return null;
                }
            });
        }

        public void register(T t) {
            subscribers.add(t);
        }

        public T fire() {
            return broadCast;
        }
    }


    interface EventListener {

        void onChange(Object obj);
    }

    @Test
    public void eventListenerTest() {
        EventListenerSupport<EventListener> eventListenerSupport = new EventListenerSupport<>(EventListener.class);
        eventListenerSupport.register(System.out::println);
        eventListenerSupport.register(System.out::println);
        eventListenerSupport.fire().onChange("aaaa");
    }

}
