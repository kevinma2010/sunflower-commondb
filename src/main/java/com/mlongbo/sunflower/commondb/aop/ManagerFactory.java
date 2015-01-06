package com.mlongbo.sunflower.commondb.aop;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malongbo
 */
final public class ManagerFactory {
    private static final Map<Class, Object> managers = new HashMap<Class, Object>();

    public static <T> T getManager(Class<T> target) {
        if (!managers.containsKey(target)) {
            loadManager(target);
        }
        return (T) managers.get(target);
    }

    public static <T> void loadManager(Class<T>... targets) {
        for (Class<T> target : targets) {
            if (managers.containsKey(target))
                continue;

            T proxy = proxy(target);

            managers.put(target, proxy);
        }
    }

    public static <T> T create(Class<T> target) {
        return proxy(target);
    }

    private static <T> T proxy(Class<T> target) {
        TransactionProxy proxy = new TransactionProxy();
        Callback[] callbacks = new Callback[] {
                proxy, NoOp.INSTANCE
        };
        Enhancer en = new Enhancer();
        en.setSuperclass(target);
        en.setCallbacks(callbacks);
        en.setCallbackFilter(new TransactionProxyFilter());

        return (T) en.create();
    }
}
