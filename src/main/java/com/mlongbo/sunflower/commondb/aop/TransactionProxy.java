package com.mlongbo.sunflower.commondb.aop;

import com.mlongbo.sunflower.commondb.DbConnectionManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author malongbo
 */
public class TransactionProxy implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        /*String name = method.getDeclaringClass().getName();
        name += "." + method.getName();

        for (Class clazz : method.getParameterTypes()) {
            name += "."+clazz.getName();
        }

        System.out.println(method.toString());
        System.out.println(method.hashCode());*/
        String name = new StringBuffer().append(method.hashCode()).toString();
        boolean isException = false;
        boolean firstTransact = DbConnectionManager.getInstance().isFirstTransact(name);
        if (firstTransact)
            DbConnectionManager.getInstance().openTransactionConnection();

        try {
            Object invokeResult = methodProxy.invokeSuper(o, objects);

            if (firstTransact)
                DbConnectionManager.getInstance().getTransactionConnection().commit();

            return invokeResult;
        } catch (Exception e) {
//            if (firstTransact)
                DbConnectionManager.getInstance().getTransactionConnection().rollback();
            DbConnectionManager.getInstance().closeThreadTransactionConnection(true);
            isException = true;
            throw new Exception(e);
        } finally {
            if (!isException) {
                if (firstTransact)
                    DbConnectionManager.getInstance().closeThreadTransactionConnection(false);
            }
        }
    }
}
