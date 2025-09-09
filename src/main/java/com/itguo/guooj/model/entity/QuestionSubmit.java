package com.itguo.guooj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 题目提交表
 * @TableName question_submit
 */
@TableName(value ="question_submit")
@Data
@Builder
public class QuestionSubmit {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 判题状态(0待判题 1 判题中 2-成功 3-失败)
     */
    private Integer status;

    /**
     * 判题信息
     */
    private String judgeInfo;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}