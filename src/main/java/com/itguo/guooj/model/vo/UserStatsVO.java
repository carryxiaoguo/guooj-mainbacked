package com.itguo.guooj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户统计信息视图对象
 */
@Data
public class UserStatsVO implements Serializable {

    /**
     * 已解决题目数
     */
    private Integer solvedCount;

    /**
     * 总提交数
     */
    private Integer submitCount;

    /**
     * 通过率（百分比）
     */
    private Double acceptRate;

    /**
     * 总通过数
     */
    private Integer acceptCount;

    private static final long serialVersionUID = 1L;
}