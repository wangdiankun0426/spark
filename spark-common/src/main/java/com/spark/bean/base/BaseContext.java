package com.spark.bean.base;

import java.util.HashMap;

/**
 *
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 * 上下文对象
 */
public class BaseContext extends HashMap {

    /**
     * 根据 对象 class获取上下文对象
     *
     * @param c
     * @param <D>
     * @return
     */
    public <D> D getVal(Class<D> c) {
        if (c == null) {
            return null;
        }
        Object obj = super.get(c);
        if (obj == null) {
            return null;
        }
        try {
            return (D) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储上下文对象，key为对象class，值为对象本身
     *
     * @param k
     * @param v
     * @param <K>
     */
    public <K extends Class> void putVal(K k, Object v) {
        super.put(k, v);
    }
}
