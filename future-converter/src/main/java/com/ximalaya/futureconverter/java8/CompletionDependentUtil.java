package com.ximalaya.futureconverter.java8;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 解析CompletionFuture的依赖树
 *
 * @author jiajun
 */
public class CompletionDependentUtil {

    private static Field STACK;

    private static Field NEXT;

    private static Field DEPENDENT;

    private static Class<?> CLASS_UNI_COMPLETION;

    private static Class<?> CLASS_UNI_COMPOSE;

    static {
        try {
            STACK = CompletableFuture.class.getDeclaredField("stack");
            STACK.setAccessible(true);
            NEXT = STACK.getType().getDeclaredField("next");
            NEXT.setAccessible(true);

            CLASS_UNI_COMPLETION = Class.forName("java.util.concurrent.CompletableFuture$UniCompletion");
            CLASS_UNI_COMPOSE = Class.forName("java.util.concurrent.CompletableFuture$UniCompose");
            DEPENDENT = CLASS_UNI_COMPLETION.getDeclaredField("dep");
            Field SOURCE = CLASS_UNI_COMPLETION.getDeclaredField("src");
            DEPENDENT.setAccessible(true);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * 获取依赖树的叶子节点
     *
     * @param completableFuture
     * @return
     */
    public static List<CompletableFuture> dependentLeaf(CompletableFuture completableFuture) {
        List<CompletableFuture> list = new LinkedList<>();
        Map<CompletableFuture, CompletableFuture> map = new HashMap<>();
        try {
            Object stack = STACK.get(completableFuture);
            traversalPre(stack, list);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return list;
    }

    static void traversalPre(Object node, List<CompletableFuture> list) throws IllegalAccessException {
        if (node == null) {
            return;
        }
        Object nextNode = NEXT.get(node);
        traversalPre(nextNode, list);
        Class<?> nodeClass = node.getClass();
        if (CLASS_UNI_COMPLETION.isAssignableFrom(nodeClass)) {
            CompletableFuture depCompletion = (CompletableFuture) DEPENDENT.get(node);
            if (depCompletion == null) {
                return;
            }
            if (nodeClass != CLASS_UNI_COMPOSE) {
                list.add(depCompletion);
            }
            traversalPre(STACK.get(depCompletion), list);
        }
    }

    static void traversalPost(Object node, List<CompletableFuture> list) throws IllegalAccessException {
        if (node == null) {
            return;
        }
        Object nextNode = NEXT.get(node);
        Class<?> nodeClass = node.getClass();
        if (CLASS_UNI_COMPLETION.isAssignableFrom(nodeClass)) {
            CompletableFuture depCompletion = (CompletableFuture) DEPENDENT.get(node);
            if (depCompletion == null) {
                return;
            }
            if (nodeClass != CLASS_UNI_COMPOSE) {
                list.add(depCompletion);
            }
            traversalPost(STACK.get(depCompletion), list);
        }
        traversalPost(nextNode, list);
    }
}
