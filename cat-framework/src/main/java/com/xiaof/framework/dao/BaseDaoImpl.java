package com.xiaof.framework.dao;

import com.xiaof.framework.mapper.BaseMapper;
import com.xiaof.framework.model.RedisCache;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Chaoyun.Yep on 18/8/31.
 */
public class BaseDaoImpl<T, M extends BaseMapper<T>> implements BaseDao<T, M> {
    @Autowired
    private ApplicationContext context;

    @Resource
    private RedisTemplate<String, Object> redisCache;

    private M mapper;

    private Class<T> modelClass;

    private String tableName;

    @Override
    public M getMapper() {
        return this.mapper;
    }

    @Override
    public Class<T> getModelClass() {
        return this.modelClass;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    /**
     * 获取主键值
     *
     * @param var1
     * @return
     */
    @Override
    public String getPrimaryKeyValue(T var1) {
        String rStr = "";
        Field[] fields = var1.getClass().getDeclaredFields();
        for (Field field : fields) {
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null) {
                field.setAccessible(true);
                try {
                    rStr = field.get(var1).toString();
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return rStr;
    }

    /**
     * 根据类中的第二个泛性信息, 获取mapper bean并设置
     * 不用PostConstruct注解通过实现InitializingBean接口也可以
     */
    @PostConstruct
    public void init() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        Type[] types = parameterizedType.getActualTypeArguments();
        this.modelClass = (Class<T>) types[0];
        this.mapper = context.getBean((Class<M>) types[1]);
        // 获取tableName
        Table table = this.modelClass.getAnnotation(Table.class);
        this.tableName = table.name();
    }

    @Override
    public List<T> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public T selectByPrimaryKey(Object var1) {
        return mapper.selectByPrimaryKey(var1);
    }

    @Override
    public int selectCount(T var1) {
        return mapper.selectCount(var1);
    }

    @Override
    public List<T> select(T var1) {
        return mapper.select(var1);
    }

    @Override
    public T selectOne(T var1) {
        return mapper.selectOne(var1);
    }

    @Override
    public int insert(T var1) {
        return mapper.insert(var1);
    }

    @Override
    public int insertSelective(T var1) {
        return mapper.insertSelective(var1);
    }

    @Override
    public int deleteByPrimaryKey(Object var1) {
        return mapper.deleteByPrimaryKey(var1);
    }

    @Override
    public int delete(T var1) {
        return mapper.delete(var1);
    }

    @Override
    public int updateByPrimaryKey(T var1) {
        return mapper.updateByPrimaryKey(var1);
    }

    @Override
    public int updateByPrimaryKeySelective(T var1) {
        return mapper.updateByPrimaryKeySelective(var1);
    }

    @Override
    public int deleteByExample(Object var1) {
        return mapper.deleteByExample(var1);
    }

    @Override
    public List<T> selectByExample(Object var1) {
        return mapper.selectByExample(var1);
    }

    @Override
    public int selectCountByExample(Object var1) {
        return mapper.selectCountByExample(var1);
    }

    @Override
    public int updateByExample(T var1, Object var2) {
        return mapper.updateByExample(var1, var1);
    }

    @Override
    public int updateByExampleSelective(T var1, Object var2) {
        return mapper.updateByExampleSelective(var1, var2);
    }

    @Override
    public List<T> selectByExampleAndRowBounds(Object var1, RowBounds var2) {
        return mapper.selectByExampleAndRowBounds(var1, var2);
    }

    @Override
    public List<T> selectByRowBounds(T var1, RowBounds var2) {
        return mapper.selectByRowBounds(var1, var2);
    }

    @Override
    public int insertList(List<T> var1) {
        return mapper.insertList(var1);
    }

    @Override
    public int insertUseGeneratedKeys(T var1) {
        return mapper.insertUseGeneratedKeys(var1);
    }

    @Override
    public T selectOneByCache(String key) {
        RedisCache redisCacheAnnotation = this.modelClass.getAnnotation(RedisCache.class);
        if (redisCacheAnnotation == null) {
            return null;
        }
        StringBuffer cacheKey = new StringBuffer(redisCacheAnnotation.prefix());
        cacheKey.append(key);
        Object cacheModel = redisCache.opsForValue().get(cacheKey.toString());
        if (cacheModel != null && cacheModel.getClass() == this.modelClass) {
            return (T) cacheModel;
        } else {
            return null;
        }
    }

}
