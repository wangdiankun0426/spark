package com.spark.bean.base;

import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    /**
     * 获取integer类型
     *
     * @param k
     * @param defVal
     * @return
     */
    public Integer getIntegerVal(Object k, Integer defVal) {
        return MapUtils.getInteger(this, k, defVal);
    }

    /**
     * 获取boolean类型
     *
     * @param k
     * @param defVal
     * @return
     */
    public Boolean getBooleanVal(Object k, Boolean defVal) {
        return MapUtils.getBoolean(this, k, defVal);
    }

    /**
     * @param k
     * @param defVal
     * @return
     */
    public String getStringVal(Object k, String defVal) {
        return MapUtils.getString(this, k, defVal);
    }

    /**
     * 获取Long类型
     *
     * @param k
     * @param defVal
     * @return
     */
    public Long getLongVal(Object k, Long defVal) {
        return MapUtils.getLong(this, k, defVal);
    }

    public boolean putItem(Object k, Object val) {
        Object obj = this.get(k);
        if (obj != null) {
            if (!(obj instanceof List)) {
                return false;
            }
            ((List) obj).add(val);
        } else {
            obj = new ArrayList();
            ((List) obj).add(val);
        }
        return true;
    }

}
