package com.spark.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 21:19
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate<Object, Object>
     * @param redisConnectionFactory
     * @return
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建 redisTemplate 模版
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        // 关联 redisConnectionFactory
        template.setConnectionFactory(redisConnectionFactory);
        // 创建 序列化类
        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);
        // 设置 key 和 hashKey 的序列化方式为 StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置 value 和 hashValue 的序列化方式为 genericToStringSerializer
        template.setValueSerializer(genericToStringSerializer);
        template.setHashValueSerializer(genericToStringSerializer);
        // 是否开启事务支持，默认为 false
        template.setEnableTransactionSupport(false);
        // 设置默认的序列化方式
        template.setDefaultSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
