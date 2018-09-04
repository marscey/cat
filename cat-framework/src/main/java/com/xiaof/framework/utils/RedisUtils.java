package com.xiaof.framework.utils;

import com.xiaof.framework.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * @auther Chaoyun.Yip
 * @create 2018/9/3 0003 15:15
 */
@Component
public class RedisUtils {

	//@Resource
    private static RedisTemplate<String, Object> redisTemplate;

	//默认一天失效
	private static final long DEFAULT_EXPIRE = 24 * 60 * 60;

    //默认不开启缓存
    private boolean isCache = false;

    @Autowired
    RedisUtils(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        //是否开启缓存
        EnableCaching enableCaching = RedisConfig.class.getAnnotation(EnableCaching.class);
        if(enableCaching != null){
            this.isCache = true;
        }
    }


    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     */
    public void set(final String key, final Object value) {
		set(key, value, DEFAULT_EXPIRE);
    }

    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     * @param expire    失效时间，单位 秒
     */
	public void set(final String key, final Object value, final Long expire) {
        if (redisTemplate == null) return;
		ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        vo.set(key, value);
        //设置超时时间
        expire(key, expire);
	}

    /**
     * Get cached query result from redis
     *
     * @param key
     * @return
     */
    public Object get(final String key){
        if (!isCache)return null;
    	ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        return vo.get(key);
    }

    /**
     * Remove cached query result from redis
     *
     * @param key
     */
	public void remove(String key) {
        if (!isCache)return;
		redisTemplate.delete(key);
	}

    /**
     * 设置超时时间
     * @param key
     * @param expire
     */
    public void expire(final String key, long expire) {
        if (!isCache)return;
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * 左入栈
     * @param key
     * @param value
     */
    public void lpush(final String key, String value) {
        if (!isCache)return;
        redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                long count = connection.lPush(serializer.serialize(key), serializer.serialize(value));
                return count;
            }
        });
    }


    /**
     * 先进后出
     * @param key
     * @return
     */
    public Object lpop(final String key) {
        if (!isCache)return null;
        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] res =  connection.lPop(serializer.serialize(key));
                return serializer.deserialize(res);
            }
        });
        return result;
    }


    /**
     * 先进先出
     * @param key
     * @return
     */
    public Object rpop(String key) {
        if (!isCache)return null;
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] res =  connection.rPop(serializer.serialize(key));
            return serializer.deserialize(res);
        });
        return result;
    }


    /**
     * 堆栈当前数量
     * @param key
     * @return
     */
    public Long llen(String key) {
        if (!isCache)return null;
        Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            Long res =  connection.lLen(serializer.serialize(key));
            return res;
        });
        return result;
    }
}
