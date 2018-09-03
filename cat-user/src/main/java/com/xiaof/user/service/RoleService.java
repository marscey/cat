package com.xiaof.user.service;

import com.xiaof.repository.model.SysRole;

import java.util.List;

public interface RoleService {

    /**
     * 保存
     */
    SysRole insertRole(SysRole sysRole);

    /**
     * 更新
     */
    SysRole updateRole(SysRole role);

    /**
     * 查询
     */
    SysRole findById(long id);

    /**
     * 查询
     */
    List<SysRole> findAll();

    /**
     * 删除
     */
    boolean deleteById(long id);

    /**
     * 删除全部
     */
    void deleteAll();
}
