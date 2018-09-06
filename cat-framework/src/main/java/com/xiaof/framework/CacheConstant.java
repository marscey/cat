package com.xiaof.framework;

/**
 *
 *
 * Created by Chaoyun.Yep on 18/8/31.
 */
public class CacheConstant {

    /**
     * Redis Key 分隔符
     */
    public static final String REDIS_KEY_PREFIX_SPLIT = "::";

    public static final String REDIS_CACHE_NAME_DEFAULT = "cache_default";

    public static final String REDIS_KEY_PREFIX_DEFAULT = REDIS_CACHE_NAME_DEFAULT + REDIS_KEY_PREFIX_SPLIT;

    public static final String REDIS_CACHE_NAME_USER = "cache_user";

    public static final String REDIS_KEY_PREFIX_USER = REDIS_CACHE_NAME_USER + REDIS_KEY_PREFIX_SPLIT;


    public static final String HOT_HIT_CACHE_NAME = "hot_hit";

    public static final String REDIS_KEY_PREFIX_HOT_HIT = HOT_HIT_CACHE_NAME + REDIS_KEY_PREFIX_SPLIT;

    public static final String EHCACHE_DEFAULT = "local_default";

}
