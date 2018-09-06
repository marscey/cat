package com.xiaof.framework.config.cache;

import com.xiaof.framework.CacheConstant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 分层缓存
 *  一级：ehcache，二级：redisCache
 *
 * @auther Chaoyun.Yip
 * @create 2018/9/5 0005 10:11
 */
@Slf4j
@Data
public class LayeringCache implements Cache {

    private String name;

    private List<String> names;

    private CacheManager ehCacheManager;

    private RedisCacheManager redisCacheManager;

    //private net.sf.ehcache.Cache ehCache;

    //private RedisTemplate<String, Object> redisTemplate;

    private long liveTime = 4 * 60 * 60; //默认4h = 4 * 60 * 60

    /**
     * Redis 缓存 集合前缀名
     */
    //private static String REDIS_KEY_COLLECTION_NAME;

    /**
     * 是否使用一级缓存
     */
    private boolean usedFirstCache = true;

    //private int activeCount = 10;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {

        Element value = getValueFrom1LevelCache(key);
        if (value != null) {
            // TODO 这样会不会更好？访问10次EhCache 强制访问一次redis 使得数据不失效
//			if (value.getHitCount() < activeCount) {
            return (value != null ? new SimpleValueWrapper(value.getObjectValue()) : null);
//			} else {
//				value.resetAccessStatistics();
//			}
        }
        //从Redis中取出
        Object objectValue = getValueFromRedis(key);
        if (objectValue == null) return null;

        //取出来之后缓存到本地
        setValueTo1LevelCache(key, objectValue);

        return new SimpleValueWrapper(objectValue);
    }


    @Override
    public <T> T get(Object key, Class<T> type) {
        if (StringUtils.isEmpty(key) || type == null) {
            return null;
        }
//		final String finalKey;
        final Class<T> finalType = type;
//		if (key instanceof String) {
//			finalKey = (String) key;
//		} else {
//			finalKey = key.toString();
//		}
//		final Object object = this.get(finalKey);
        final Object object = this.get(key);
        if (object != null && finalType.isInstance(object)) {
            return (T) object;
        }
        return null;
    }


    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        setValueTo1LevelCache(key, value);
        setValueToRedis(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        //final String finalKey;
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return null;
        }
        //if (key instanceof String) {
        //    finalKey = (String) key;
        //} else {
        //    finalKey = key.toString();
        //}
        //if (!StringUtils.isEmpty(finalKey)) {
        //    final Object finalValue = value;
            this.put(key, value);
		//}
        return new SimpleValueWrapper(value);

    }

    /**
     * 删除缓存
     * @param key
     */
    @Override
    public void evict(Object key) {
        //ehCache.remove(key);
        //先删掉二级缓存的数据，再去删掉一级缓存的数据，否则有并发问题
        removeRedisCache(key);
        remove1LevelCache(key);
        if (usedFirstCache) {
            //// 删除一级缓存需要用到redis的订阅/发布模式，否则集群中其他服服务器节点的一级缓存数据无法删除
            //Map<String, Object> message = new HashMap<>();
            //message.put("cacheName", name);
            //message.put("key", key);
            //// 创建redis发布者
            //RedisPublisher redisPublisher = new RedisPublisher(redisOperations, ChannelTopicEnum.REDIS_CACHE_DELETE_TOPIC.getChannelTopic());
            //// 发布消息
            //redisPublisher.publisher(message);
        }
    }

    @Override
    public void clear() {
        removeAllRedisCache();
        removeAll1LevelCache();
    }

    public net.sf.ehcache.Cache getEhCache(){
        //if (StringUtils.isEmpty(getName())) return this.ehCache;
        Assert.notNull(getName(), "Cache Name must not be null!");
        net.sf.ehcache.Cache ehcache = ehCacheManager.getCache(getName());
        //this.ehCache = cache;
        return ehcache;
    }

    /**
     * 将数据缓存进 1 Level Cache
     * @param key
     * @param value
     */
    private void setValueTo1LevelCache(Object key, Object value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) return;
        getEhCache().put(new Element(key, value));
    }

    /**
     * 从 1 Level Cache 中取数据
     * @param key
     * @return
     */
    private Element getValueFrom1LevelCache(Object key) {
        if (StringUtils.isEmpty(key)) return null;
        //Element value = ehCache.get(key);
        Element value = getEhCache().get(key);
        log.debug("Cache L1 (ehcache) Key : {} = {}", key, value);
        return value;
    }

    /**
     * 删除 1 Level Cache 所有缓存数据
     * @return
     */
    private void removeAll1LevelCache() {
        getEhCache().removeAll();
    }

    /**
     * 删除 1 Level Cache 缓存 key 的数据
     *
     * TODO 删除一级缓存需要用到redis的Pub/Sub（订阅发布）模式，
     *
     *      否则集群中其他服服务器节点的一级缓存数据无法删除。
     *      redis的Pub/Sub（订阅发布）模式发送消息是无状态的，
     *      如果遇到网络等原因有可能导致一些应用服务器上的一级缓存没办法删除，
     *      如果对L1和L2数据同步要求较高的话，这里可以使用MQ来做
     * @return
     */
    private void remove1LevelCache(Object key) {
        getEhCache().remove(key);
    }

    /**
     * getRedisCache
     *
     * @return
     */
    public Cache getRedisCache(){
        //if (StringUtils.isEmpty(getName())) return this.ehCache;
        Assert.notNull(getName(), "Cache Name must not be null!");
        Cache cache = redisCacheManager.getCache(getName());
        if (cache == null){
            cache = redisCacheManager.getCache(CacheConstant.REDIS_CACHE_NAME_DEFAULT);
        }
        //this.ehCache = cache;
        return cache;
    }

    /**
     * 将数据缓存进 Redis Cache
     * @param key
     * @param value
     */
    private void setValueToRedis(Object key, Object value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) return;
        final String keyStr = key.toString();
        final Object valueStr = value;
        //redisTemplate.execute(new RedisCallback<Long>() {
        //    public Long doInRedis(RedisConnection connection)
        //            throws DataAccessException {
        //        byte[] keyb = keyStr.getBytes();
        //        byte[] valueb = toByteArray(valueStr);
        //        connection.set(keyb, valueb);
        //        if (liveTime > 0) {
        //            connection.expire(keyb, liveTime);
        //        }
        //        return 1L;
        //    }
        //},true);
        getRedisCache().put(keyStr, value);
    }

    /**
     * 从 Redis Cache 取数据
     * @param key
     * @return
     */
    private Object getValueFromRedis(Object key) {
        if (StringUtils.isEmpty(key)) return null;
        final String keyStr = key.toString();
        //Object objectValue = redisTemplate.execute(new RedisCallback<Object>() {
        //    public Object doInRedis(RedisConnection connection)
        //            throws DataAccessException {
        //        byte[] key = keyStr.getBytes();
        //        byte[] value = connection.get(key);
        //        if (value == null) {
        //            return null;
        //        }
        //        //每次获得，重置缓存过期时间
        //        if (liveTime > 0) {
        //            connection.expire(key, liveTime);
        //        }
        //        return toObject(value);
        //    }
        //},true);
        ValueWrapper valueWrapper = getRedisCache().get(keyStr);

        Object objectValue = valueWrapper == null ? null : valueWrapper.get();
        log.debug("Cache L2 (redis) Key : {} = {}", getRedisKeyPrefix() + keyStr, objectValue);
        return objectValue;
    }

    /**
     * 删除 Redis Cache 中 key 的缓存数据
     * @param key
     */
    private void removeRedisCache(Object key){
        if (StringUtils.isEmpty(key)) return ;
        final String keyStr = key.toString();
        //redisTemplate.execute(new RedisCallback<Long>() {
        //    public Long doInRedis(RedisConnection connection)
        //            throws DataAccessException {
        //        return connection.del(keyStr.getBytes());
        //    }
        //},true);
        getRedisCache().evict(keyStr);
    }

    /**
     * 删除 Redis Cache 所有缓存
     */
    private void removeAllRedisCache(){
        //redisTemplate.execute(connection -> {
        //    connection.flushDb();
        //    return "clear done.";
        //},true);
        getRedisCache().clear();
    }

    private String getRedisKeyPrefix(){
        if (StringUtils.isEmpty(getName()))
            return CacheConstant.REDIS_KEY_PREFIX_DEFAULT;
        return getName() + CacheConstant.REDIS_KEY_PREFIX_SPLIT;
    }

    /**
     * 描述 : Object转byte[]. <br>
     * @param obj
     * @return
     */
    private byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 描述 :  byte[]转Object . <br>
     * @param bytes
     * @return
     */
    private Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}
