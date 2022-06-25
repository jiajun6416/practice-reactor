package com.jiajun.reactor.observer;

import lombok.Getter;
import org.apache.commons.lang3.event.EventListenerSupport;
import org.junit.Test;

import java.util.Observable;

/**
 * 常用的发布订阅模型
 *
 * @author jiajun
 */
public class Observer {

    /**
     * JDK中Observable的子类
     *
     * @param <T>
     */
    public static class ObservableSubject<T> extends Observable {

        @Getter
        private T subject;

        public void setSubject(T subject) {
            this.subject = subject;
            super.setChanged();
            super.notifyObservers();
        }
    }

    @Test
    public void jdkObservableTest() {
        ObservableSubject<String> observable = new ObservableSubject<>();
        observable.addObserver(new java.util.Observer() {
            @Override
            public void update(Observable o, Object arg) {
                ObservableSubject subject = ObservableSubject.class.cast(o);
                System.out.println("update1: " + subject.getSubject());
            }
        });
        observable.addObserver(new java.util.Observer() {
            @Override
            public void update(Observable o, Object arg) {
                ObservableSubject subject = ObservableSubject.class.cast(o);
                System.out.println("update2: " + subject.getSubject());
            }
        });
        observable.setSubject("a");
        observable.setSubject("b");
    }

    /**
     * 事件处理者
     *
     * @param <T>
     */
    public interface EventListener extends java.util.EventListener {

        // listener触发onEvent
        void onEvent(String event);

        // 生产者调用fireEvent
        default void fireEvent(String event) {
            onEvent(event);
        }
    }

    /**
     * Apache-Common中{@link EventListenerSupport}: 将被代理接口的方法广播到所有listener中执行, listener也是代理接口的实现.
     */
    @Test
    public void apacheEventListenerSupportTest() {
        EventListenerSupport<EventListener> eventListenerSupport = EventListenerSupport.create(EventListener.class);

        eventListenerSupport.addListener(event -> System.out.println("listener1 onEvent: " + event));
        eventListenerSupport.addListener(event -> System.out.println("listener2 onEvent: " + event));

        eventListenerSupport.fire().fireEvent("a"); // 直接触发监听器的事件
        eventListenerSupport.fire().fireEvent("b");
    }
}
