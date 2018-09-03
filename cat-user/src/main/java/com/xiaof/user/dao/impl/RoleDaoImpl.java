package com.xiaof.user.dao.impl;

import com.xiaof.framework.dao.BaseDaoImpl;
import com.xiaof.repository.mapper.SysRoleMapper;
import com.xiaof.repository.model.SysRole;
import com.xiaof.user.dao.RoleDao;
import org.springframework.stereotype.Repository;

/**
 * @auther Chaoyun.Yip
 * @create 2018/9/3 0003 14:30
 */
@Repository
public class RoleDaoImpl extends BaseDaoImpl<SysRole, SysRoleMapper> implements RoleDao {

}
