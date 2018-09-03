package com.xiaof.framework.dao;

import com.xiaof.framework.mapper.BaseMapper;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * DAO基类
 * 泛型: T-Model M-Mapper
 * Created by Chaoyun.Yep on 18/8/31.
 */
public interface BaseDao<T, M extends BaseMapper<T>> {

    M getMapper();

    Class<T> getModelClass();

    String getTableName();

    String getPrimaryKeyValue(T var1);

    List<T> selectAll();

    T selectByPrimaryKey(Object var1);

    int selectCount(T var1);

    List<T> select(T var1);

    T selectOne(T var1);

    int insert(T var1);

    int insertSelective(T var1);

    int deleteByPrimaryKey(Object var1);

    int delete(T var1);

    int updateByPrimaryKey(T var1);

    int updateByPrimaryKeySelective(T var1);

    int deleteByExample(Object var1);

    List<T> selectByExample(Object var1);

    int selectCountByExample(Object var1);

    int updateByExample(T var1, Object var2);

    int updateByExampleSelective(T var1, Object var2);

    List<T> selectByExampleAndRowBounds(Object var1, RowBounds var2);

    List<T> selectByRowBounds(T var1, RowBounds var2);

    int insertList(List<T> var1);

    int insertUseGeneratedKeys(T var1);

    T selectOneByCache(String key);

}
