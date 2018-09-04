package com.xiaof.api.controller;


import com.xiaof.repository.mapper.SysUserMapper;
import com.xiaof.repository.model.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户接口
 * @auther Chaoyun.Yip
 * @create 2018/5/23
 */
@Api(description = "用户数据接口")
@RestController
@RequestMapping(path = "/xiaof/user")
@Slf4j
public class UserController {

    @Resource
    private SysUserMapper sysUserMapper;


    @ApiOperation("用户")
    @RequestMapping(value = "/list", method = {RequestMethod.POST})
    public SysUser userList() {
        //sysUserMapper
        //try {
        //    return RestResponse.ok(pushService.pushMessage(aliasIds, content));
        //} catch (Exception e) {
        //    //e.printStackTrace();
        //    return RestResponse.exception(CodeDefault.INTERNAL_SERVER_ERROR);
        //}
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(2l);
        //List<SysUser> list = sysRoleMapper.selectAll();
        return sysUser;
    }
}
