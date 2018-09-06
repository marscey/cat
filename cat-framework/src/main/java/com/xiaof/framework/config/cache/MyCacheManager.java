package com.xiaof.framework.config.cache;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @auther Chaoyun.Yip
 * @create 2018/9/5 0005 16:34
 */
@Data
@Slf4j
public class MyCacheManager implements CacheManager {

    private LayeringCache ehRedisCache;

    //private MyRedisCache myRedisCache;


    @Override
    public Cache getCache(String name) {
        if (StringUtils.isEmpty(name)) return null;

        //多级缓存实现
        //if(name.equals(ehRedisCache.getName())){
        if(isContainElement(ehRedisCache.getNames(), name)){
            ehRedisCache.setName(name);
            return ehRedisCache;
        }
        return null;
    }

    private static <T> boolean isContainElement(List<T> elements, T t) {
        //boolean flag = false;
        if (elements == null || elements.size() <= 0 || t == null) return false;
        //long s = new Date().getTime();
        for (T element : elements){
            if (element.equals(t)){
                return true;
            }
        }
        //long e = new Date().getTime();
        //log.info("传统方式：{}，耗时：{}", flag, e-s);
        return false;
    }

    private static  <T> boolean isContainElementStream(List<T> elements, T t) {
        if (elements == null || elements.size() <= 0) return false;
        long s = new Date().getTime();
        long r = elements.stream().filter(f -> f.equals(t)).count();
        long e = new Date().getTime();
        boolean flag = r > 0 ? true : false;
        log.info("Stream方式：{}，耗时：{}", flag, e-s);
        return flag;
    }

    public static void main(String[] args){
        Object o = "222";
        List<Object> list = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            list.add(String.valueOf(i));
        }
        isContainElement(list,o);
        isContainElementStream(list,o);
    }

    @Override
    public Collection<String> getCacheNames() {

        return null;
    }
}
