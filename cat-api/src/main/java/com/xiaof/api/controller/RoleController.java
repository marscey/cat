package com.xiaof.api.controller;


import com.xiaof.repository.model.SysRole;
import com.xiaof.user.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色接口
 * @auther Chaoyun.Yip
 * @create 2018/5/23
 */
@Api(description = "角色数据接口")
@RestController
@RequestMapping(path = "/xiaof/role")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ApiOperation("查询所有角色")
    @RequestMapping(value = "/lists", method = {RequestMethod.GET})
    public List<SysRole> lists() {
        //sysUserMapper
        //try {
        //    return RestResponse.ok(pushService.pushMessage(aliasIds, content));
        //} catch (Exception e) {
        //    //e.printStackTrace();
        //    return RestResponse.exception(CodeDefault.INTERNAL_SERVER_ERROR);
        //}
        //List<SysRole> list = sysRoleMapper.selectAll();
        List<SysRole> list = roleService.findAll();
        return list;
    }

    @ApiOperation("根据ID，查询角色")
    @RequestMapping(value = "/findById", method = {RequestMethod.GET})
    public SysRole listById(
            @ApiParam(value = "角色ID", required = true)
            @RequestParam int roleId) {
        SysRole sysRole = roleService.findById(roleId);
        return sysRole;
    }

    @ApiOperation("保存角色")
    @RequestMapping(value = "/save", method = {RequestMethod.POST})
    public SysRole save(
            @ApiParam(value = "角色编码，必须加前缀“ROLE_”，统一大写", required = true)
            @RequestParam String roleCode,
            @ApiParam(value = "角色名称", required = true)
            @RequestParam String roleName) {
        SysRole sysRole = new SysRole();
        sysRole.setRemark(roleName);
        sysRole.setRolename(roleCode);
        return roleService.insertRole(sysRole);
    }

    @ApiOperation("更新角色")
    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    public SysRole update(
            @ApiParam(value = "角色ID", required = true)
            @RequestParam long roleId,
            @ApiParam(value = "角色编码，必须加前缀“ROLE_”，统一大写", required = false)
            @RequestParam(required = false) String roleCode,
            @ApiParam(value = "角色名称", required = false)
            @RequestParam(required = false) String roleName) {
        SysRole sysRole = new SysRole();
        sysRole.setId(roleId);
        sysRole.setRemark(roleName);
        sysRole.setRolename(roleCode);
        return roleService.updateRole(sysRole);
    }

    @ApiOperation("删除角色")
    @RequestMapping(value = "/delete", method = {RequestMethod.POST})
    public boolean update(
            @ApiParam(value = "角色ID", required = true)
            @RequestParam long roleId) {
        return roleService.deleteById(roleId);
    }

    @ApiOperation("删除所有角色")
    @RequestMapping(value = "/deleteAll", method = {RequestMethod.POST})
    public boolean deleteAll() {
        try {
            roleService.deleteAll();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
