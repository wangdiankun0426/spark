package com.spark.config.redis;

import com.spark.utils.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 19:03
 */
@Service
public class RedisService {
    @Resource(name = "redisTemplate")
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 判断key是否存在
     * @param key 键
     * @return true存在 false不存在
     */
    public boolean hasKey(String key) {
        if (StringUtil.isBlank(key)) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public String getValue(String key) {
        if (StringUtil.isBlank(key)) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if(value == null){
            return null;
        }
        return value.toString();
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean setObj(String key,Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean setStr(String key,String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setStr(String key,String value,long time) {
        try {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间（指定时间单位）
     * @param key 键
     * @param value 值
     * @param time 时间
     * @param timeUnit 时间单位
     * @return true成功 false 失败
     */
    public boolean setStr(String key, String value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    public boolean del(String ... key) {
        try {
            if (key!=null && key.length>0) {
                if (key.length==1) {
                    redisTemplate.delete(key[0]);
                } else {
                    redisTemplate.delete(key);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key,long time){
        try {
            if(time>0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向Redis的List中追加元素（从右侧推入）
     * @param key 键
     * @param values 一个或多个值
     * @return 返回插入后List的长度
     */
    public long pushList(String key, Object... values) {
        try {
            if (StringUtil.isBlank(key) || values == null || values.length == 0) {
                return 0;
            }
            Long count = redisTemplate.opsForList().rightPushAll(key, List.of(values));
            return count == null ? 0 : count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取Redis中List的指定范围元素
     * @param key 键
     * @param start 起始索引（0开始），-1表示最后一个
     * @param end 结束索引，-1表示最后一个
     * @return 列表中的元素（字符串形式）
     */
    public List<Object> getList(String key, long start, long end) {
        try {
            if (StringUtil.isBlank(key)) {
                return new ArrayList<>();
            }
            List<Object> objects = redisTemplate.opsForList().range(key, start, end);
            if (objects == null) {
                return new ArrayList<>();
            }
            return objects;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
