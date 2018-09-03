package com.xiaof.user.service.impl;

import com.xiaof.framework.utils.RedisCacheUtils;
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
 *
 * @auther Chaoyun.Yip
 * @create 2018/9/3 0003 15:48
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "roleCache")
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    private RedisCacheUtils redisCacheUtils = new RedisCacheUtils("roleCache_");

    /**
     * 保存
     *
     * 因为必须要有返回值，才能保存到数据库中，
     * 如果保存的对象的某些字段是需要数据库生成的，那保存对象进数据库的时候，就没必要放到缓存了
     * @param sysRole
     * @return
     */
    @Override
    //@CachePut(key = "#p0.id")//#p0表示第一个参数
    public SysRole insertRole(SysRole sysRole){
        int id = roleDao.insert(sysRole);
        //必须要有返回值，否则没数据放到缓存中
        return null;
        //return roleDao.selectByPrimaryKey(id);
    }

    /**
     * 更新
     * @param role
     * @return
     */
    @Override
    @CachePut(key="#p0.id")
    public SysRole updateRole(SysRole role) {
        //updateByPrimaryKeySelective会对字段进行判断再更新(如果为Null就忽略更新)，如果你只想更新某一字段，可以用这个方法
        roleDao.updateByPrimaryKeySelective(role);
        //可能只是更新某几个字段而已，所以查次数据库把数据全部拿出来全部
        role = roleDao.selectByPrimaryKey(role.getId());
        //删除all缓存
        redisCacheUtils.removeObject("roleCache::ids");
        return role;
    }

    /**
     * 查询
     * @param id
     * @return
     */
    @Override
    @Cacheable(key = "#p0")//@Cacheable 会先查询缓存，如果缓存中存在，则不执行方法
    public SysRole findById(long id){
        return roleDao.selectByPrimaryKey(id);
        //return roleDao.selectOneByCache(String.valueOf(id));
    }

    @Override
    public List<SysRole> findAll() {
        List<SysRole> list = (List<SysRole>) redisCacheUtils.getObject("roleCache::ids");
        if (list == null) {
            list = roleDao.selectAll();
            redisCacheUtils.putObject("roleCache::ids", list);
        }
        return list;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    @CacheEvict(key="#p0")  //删除缓存名称为userCache,key等于指定的id对应的缓存
    public boolean deleteById(long id){
        int result = roleDao.deleteByPrimaryKey(id);
        //删除all缓存
        redisCacheUtils.removeObject("roleCache::ids");
        return result > 0 ? true : false;
    }

    /**
     * 删除全部
     *
     * allEntries = true: 清空缓存 cacheNames 里的所有值
     * allEntries = false: 默认值，此时只删除key对应的值
     */
    @Override
    @CacheEvict(allEntries = true)
    public void deleteAll(){
        roleDao.getMapper().deleteAll();
        //删除all缓存
        redisCacheUtils.removeObject("roleCache::ids");
    }
}
