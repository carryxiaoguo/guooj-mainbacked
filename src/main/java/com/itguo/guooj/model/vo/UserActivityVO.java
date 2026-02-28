package com.itguo.guooj.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户活动记录视图对象
 */
@Data
public class UserActivityVO implements Serializable {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动类型（submit, login, update等）
     */
    private String type;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动时间
     */
    private Date time;

    private static final long serialVersionUID = 1L;
}