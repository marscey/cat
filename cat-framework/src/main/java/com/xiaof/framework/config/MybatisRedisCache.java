package com.xiaof.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Mybatis Redis Cache 工具类
 *
 * @auther Chaoyun.Yip
 * @create 2018/9/3 0003 15:15
 */
@Slf4j
public class MybatisRedisCache implements Cache {
    //private static final Logger logger = LoggerFactory.getLogger(MybatisRedisCache.class);
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String id; // cache instance id
    private RedisTemplate<String, Object> redisTemplate;
    private static final long EXPIRE_TIME_IN_MINUTES = 30; // redis过期时间

    /**
     * 自己实现的二级缓存，必须要有一个带id的构造函数，否则会报错。
     *
     * 我们使用Spring封装的redisTemplate来操作Redis。
     * 网上所有介绍redis做二级缓存的文章都是直接用jedis库，
     *
     * 但是笔者认为这样不够Spring Style，而且，redisTemplate封装了底层的实现，
     * 未来如果我们不用jedis了，我们可以直接更换底层的库，而不用修改上层的代码。
     *
     * 更方便的是，使用redisTemplate，我们不用关心redis连接的释放问题，
     * 否则新手很容易忘记释放连接而导致应用卡死
     * @param id
     */
    public MybatisRedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }


    @Override
    public String getId() {
        return id;
    }


    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     */
    @Override
    public void putObject(Object key, Object value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, value, EXPIRE_TIME_IN_MINUTES, TimeUnit.MINUTES);
        log.debug("Put query result to redis");
    }


    /**
     * Get cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    public Object getObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        log.debug("Get cached query result from redis");
        return opsForValue.get(key);
    }


    /**
     * Remove cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    public Object removeObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
        log.debug("Remove cached query result from redis");
        return null;
    }


    /**
     * Clears this cache instance
     */
    @Override
    public void clear() {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.execute((RedisCallback) connection -> {
            connection.flushDb();
            return null;
        });
        log.debug("Clear all the cached query result from redis");
    }


    @Override
    public int getSize() {
        return 0;
    }


    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
    private RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            /**
             * 不能通过autowire的方式引用redisTemplate
             * RedisCache并不是Spring容器里的bean
             */
            redisTemplate = ApplicationContextProvider.getBean("redisTemplate", RedisTemplate.class);
        }
        return redisTemplate;
    }
}
