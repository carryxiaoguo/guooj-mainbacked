package com.itguo.guooj.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交历史记录VO
 */
@Data
public class QuestionSubmitHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交ID
     */
    private Long id;

    /**
     * 题目ID
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
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 用户名(仅管理员可见)
     */
    private String userName;

    /**
     * 判题状态
     */
    private Integer status;

    /**
     * 判题信息
     */
    private String judgeInfo;
}
