package com.xiaof.repository.mapper;

import com.xiaof.framework.mapper.BaseMapper;
import com.xiaof.repository.model.SysRole;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Results(id = "SysRoleResult", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "remark", column = "remark"),
            @Result(property = "rolename", column = "rolename")
    })
    @Select("select * from sys_role")
    List<SysRole> findAll();

    @Delete("delete form sys_role where id=#{id}")
    int deleteById(int id);

    @Delete("delete form sys_role")
    int deleteAll();
}