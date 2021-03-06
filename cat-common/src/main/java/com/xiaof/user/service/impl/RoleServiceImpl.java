package com.xiaof.user.service.impl;

import com.xiaof.framework.CacheConstant;
import com.xiaof.repository.model.SysRole;
import com.xiaof.user.dao.RoleDao;
import com.xiaof.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色业务处理类
 *
 * @auther Chaoyun.Yip
 * @create 2018/9/3 0003 15:48
 */
@Slf4j
@Service
@CacheConfig(cacheNames = CacheConstant.HOT_HIT_CACHE_NAME)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    /**
     * 保存
     *
     * 因为必须要有返回值，才能保存到数据库中，
     * 如果保存的对象的某些字段是需要数据库生成的，那保存对象进数据库的时候，就没必要放到缓存了
     * @param sysRole
     * @return
     */
    @Override
    public SysRole insertRole(SysRole sysRole){
        int id = roleDao.insert(sysRole);
        //必须要有返回值，否则没数据放到缓存中
        return null;
    }

    /**
     * 更新
     * @param role
     * @return
     */
    @Override
    @CachePut(key="#p0.id")//@CachePut(key = "#p0.id")//#p0表示第一个参数
    public SysRole updateRole(SysRole role) {
        //updateByPrimaryKeySelective会对字段进行判断再更新(如果为Null就忽略更新)，如果你只想更新某一字段，可以用这个方法
        int result = roleDao.updateByPrimaryKeySelective(role);
        //可能只是更新某几个字段而已，所以查次数据库把数据全部拿出来全部
        role = roleDao.selectByPrimaryKey(role.getId());
        return role;
    }

    /**
     * 查询
     * @param id
     * @return
     *
     * @Cacheable 应用到读取数据的方法上，
     *  先从缓存中读取，如果没有再从DB获取数据，然后把数据添加到缓存中
     *  value 非必填。用于注定缓存数据的储存集合名称，等同于@CacheConfig(cacheNames = "roleCache")中的cacheNames
     *          当配置多个缓存时，也用于指定哪个缓存 如 Ehcache 中的指定那个 name
     *  key #p0 表示为第一个参数，或者 #id 直接指定id为 key
     *  unless 表示条件表达式成立的话不放入缓存（支持spring el表达式）
     *  eq(==), ne(!=), lt()<, le(<=), gt(>), ge(>=)，and(&&), or(||), not(!)
     */
    @Override
    @Cacheable(key = "#p0", unless = "#result eq null")
    public SysRole findById(long id){
        return roleDao.selectByPrimaryKey(id);
        //return roleDao.selectOneByCache(String.valueOf(id));
    }

    /**
     * cacheManager 指定用那个缓存器
     *      一般在配置中指定，配置多个缓存器，须@Primary
     * cacheNames/value 指定缓存组件的名字;将方法的返回结果放在哪个缓存中，是数组的方式，可以指定 多个缓存
     * @return
     */
    @Override
    //@Cacheable(cacheNames = {CacheConstant.REDIS_CACHE_NAME_USER}, cacheManager = "redisCacheManager", unless = "#result eq null")
    @Cacheable(unless = "#result eq null")
    public List<SysRole> findAll() {
        return roleDao.selectAll();
    }

    /**
     * 删除
     * @param id
     */
    @Override
    //删除缓存名称为userCache,key等于指定的id对应的缓存
    //condition 条件，当返回结果 等于 true 时才成立
    @CacheEvict(key = "#p0", condition = "#result eq true")
    public boolean deleteById(long id){
        int result = roleDao.deleteByPrimaryKey(id);
        return result > 0 ? true : false;
    }

    /**
     * 删除全部
     *
     * allEntries = true: 清空缓存 cacheNames 里的所有值，false: 默认值，此时只删除key对应的值
     * condition 条件，当返回结果 大于 0 时才执行
     */
    @Override
    @CacheEvict(allEntries = true, condition = "#result gt 0")
    public int deleteAll(){
        int result = roleDao.getMapper().deleteAll();
        return result;
    }
}
