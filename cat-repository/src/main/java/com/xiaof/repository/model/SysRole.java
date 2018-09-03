package com.xiaof.repository.model;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "sys_role")
public class SysRole implements Serializable {

    /**
     * 如果你修改了此类, 要修改此值。否则以前用老版本的类序列化的类恢复时会出错。
     * 为了在反序列化时，确保类版本的兼容性，最好在每个要序列化的类中加入private static final long serialVersionUID这个属性，
     * 具体数值自己定义
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色名
     */
    private String rolename;

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取备注
     *
     * @return remark - 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    /**
     * 获取角色名
     *
     * @return rolename - 角色名
     */
    public String getRolename() {
        return rolename;
    }

    /**
     * 设置角色名
     *
     * @param rolename 角色名
     */
    public void setRolename(String rolename) {
        this.rolename = rolename == null ? null : rolename.trim();
    }
}