package com.mlongbo.sunflower.commondb.aop;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

/**
 * @author malongbo
 */
public class TransactionProxyFilter implements CallbackFilter {
    private final String[] filterPrefixNames = new String[]{
            "add","create","put","insert",
            "delete","remove",
            "update","change","edit"};     //增删改操作

    @Override
    public int accept(Method method) {
        return isFilter(method.getName()) ? 0 : 1;
    }

    private boolean isFilter (String name) {

        for (String tmpName : this.filterPrefixNames) {
            if (name.startsWith(tmpName))//需要被代理
                return true;
        }
        return false;
    }

}
