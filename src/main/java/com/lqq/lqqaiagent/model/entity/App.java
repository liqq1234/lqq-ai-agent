package com.lqq.lqqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 应用
 * @TableName app
 */
@TableName(value ="app")
@Data
public class App {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    @TableField("appname")
    private String appName;

    /**
     * 应用封面
     */
    @TableField("cover")
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    @TableField("initprompt")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    @TableField("codegentype")
    private String codeGenType;

    /**
     * 部署标识
     */
    @TableField("deploykey")
    private String deployKey;

    /**
     * 部署时间
     */
    @TableField("deployedtime")
    private Date deployedTime;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 创建用户id
     */
    @TableField("userid")
    private Long userId;

    /**
     * 编辑时间
     */
    @TableField("edittime")
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField("createtime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updatetime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField("isdelete")
    private Integer isDelete;
}