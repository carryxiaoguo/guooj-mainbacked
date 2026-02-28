package com.itguo.guooj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解表
 */
@TableName(value = "question_solution")
@Data
public class QuestionSolution implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private String title;

    private String content;

    private String language;

    private String code;

    private Integer likeNum;

    private Integer viewNum;

    private Integer isOfficial;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
