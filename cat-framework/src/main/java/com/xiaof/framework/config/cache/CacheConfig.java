package com.xiaof.framework.config.cache;

import com.alibaba.fastjson.parser.ParserConfig;
import com.google.common.collect.Lists;
import com.xiaof.framework.CacheConstant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置
 * Created by Chaoyun.Yep on 18/8/31.
 *
 * 启动缓存 @EnableCaching
 * model属性默认proxy
 *      mode属性，可以选择值proxy和aspectj。默认使用proxy。当mode为proxy时，
 *      只有缓存方法在外部被调用的时候才会生效。这也就意味着如果一个缓存方法在一个对
 *      象的内部被调用SpringCache是不会发生作用的。而mode为aspectj时，就不会有
 *      这种问题了。另外使用proxy的时候，只有public方法上的@Cacheable才会发生作用。
 *      如果想非public上的方法也可以使用那么就把mode改成aspectj
 */
@Configuration
@EnableCaching(mode = AdviceMode.PROXY)
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.database}")
    private int database;

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    @Bean
    public JedisConnectionFactory connectionFactory() {
        // 链接池配置
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        JedisConnectionFactory cf = new JedisConnectionFactory(config);
        return cf;
    }

    /**
     * 设置序列化
     */
    /*@Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(new ObjectMapper());

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        return jackson2JsonRedisSerializer;
    }*/

    /**
     * 使用 FastJson 序列化
     * @return
     */
    @Bean
    public FastJsonRedisSerializer fastJsonRedisSerializer() {
        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        /**
         * 建议使用这种方式，小范围指定白名单
         *
         * fastjson在2017年3月爆出了在1.2.24以及之前版本存在远程代码执行高危安全漏洞。
         * 所以要使用ParserConfig.getGlobalInstance().addAccept("com.xiaof.");指定序列化白名单
         */
        ParserConfig.getGlobalInstance().addAccept("com.xiaof.");
        return fastJsonRedisSerializer;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory());
        //redisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer());

        //value的序列化采用 fastJsonRedisSerializer
        redisTemplate.setValueSerializer(fastJsonRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer());

        // key的序列化采用 StringRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Redis 缓存管理器
     */
    @Bean
    //@Primary
    public RedisCacheManager redisCacheManager() {
        //初始化一个RedisCacheWriter，来实现读写Redis
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory());

        //设置CacheManager的值序列化方式为 fastJsonRedisSerializer,
        // RedisCacheConfiguration默认就是使用StringRedisSerializer序列化key，
        RedisSerializationContext.SerializationPair serializationPair = RedisSerializationContext.
                SerializationPair.fromSerializer(fastJsonRedisSerializer());

        //default 信息缓存配置
        RedisCacheConfiguration productCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializationPair).entryTtl(Duration.ofDays(30))
                .disableCachingNullValues().prefixKeysWith(CacheConstant.REDIS_KEY_PREFIX_DEFAULT);

        //user 信息缓存配置
        RedisCacheConfiguration userCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializationPair).entryTtl(Duration.ofDays(30))
                .disableCachingNullValues().prefixKeysWith(CacheConstant.REDIS_KEY_PREFIX_USER);

        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        redisCacheConfigurationMap.put(CacheConstant.REDIS_KEY_PREFIX_DEFAULT, productCacheConfiguration);
        redisCacheConfigurationMap.put(CacheConstant.REDIS_CACHE_NAME_USER, userCacheConfiguration);

        //设置默认配置，缓存过期时间为1天
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializationPair).entryTtl(Duration.ofDays(10))
                .disableCachingNullValues().prefixKeysWith(CacheConstant.REDIS_KEY_PREFIX_HOT_HIT);
                //.disableCachingNullValues();

        RedisCacheManager manager = new RedisCacheManager(redisCacheWriter, defaultCacheConfig, redisCacheConfigurationMap);

        //RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
        //        .RedisCacheManagerBuilder
        //        .fromConnectionFactory(connectionFactory())
        //        .cacheDefaults(defaultCacheConfig);
        //RedisCacheManager manager = builder.build();
        return manager;
    }

    /**
     * EhCache 操作对象
     */
    @Bean(name = "ehcache")
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheBean = new EhCacheManagerFactoryBean();
        ClassPathResource r = new ClassPathResource("config/ehcache-local.xml");
        cacheBean.setConfigLocation(r);
        //true:单例，一个cacheManager对象共享；false：多个对象独立
        cacheBean.setShared(true);
        return cacheBean;
    }

    /**
     * EhCache 缓存管理器
     * @param ehcacheManager
     * @return
     */
    @Bean("ehCacheCacheManager")
    public EhCacheCacheManager ehCacheCacheManager(@Qualifier("ehcache") net.sf.ehcache.CacheManager ehcacheManager) {
        EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager(ehcacheManager);
        return ehCacheCacheManager;
    }

    /**
     * spring cache整合(EhCache,Redis)二级缓存具体Cache
     * @return
     */
    @Bean
    public LayeringCache ehRedisCache() {
        LayeringCache ehRedisCache = new LayeringCache();
        //ehRedisCache.setEhCache(ehCacheManagerFactoryBean().getObject().getCache(CacheConstant.EHCACHE_A));
        ehRedisCache.setEhCacheManager(ehCacheManagerFactoryBean().getObject());
        ehRedisCache.setRedisCacheManager(redisCacheManager());
        //ehRedisCache.setRedisTemplate(redisTemplate());
        //ehRedisCache.setName(CacheConstant.EHCACHE_A);
        ehRedisCache.setNames(Lists.newArrayList(CacheConstant.EHCACHE_DEFAULT, CacheConstant.HOT_HIT_CACHE_NAME));
        //ehRedisCache.setLiveTime();
        return ehRedisCache;
    }

    /**
     * spring cache 统一缓存管理器
     * @return
     */
    @Bean
    @Primary
    //@Override
    public CacheManager cacheManager(){
        MyCacheManager cacheManager = new MyCacheManager();
        cacheManager.setEhRedisCache(ehRedisCache());
        return cacheManager;
    }
}
