package com.itguo.guooj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统统计信息视图
 */
@Data
public class SystemStatsVO implements Serializable {

    /**
     * 题目总数
     */
    private Long questionCount;

    /**
     * 用户总数
     */
    private Long userCount;

    /**
     * 提交总数
     */
    private Long submitCount;

    private static final long serialVersionUID = 1L;
}
