package com.itguo.guooj.model.dto.comment;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建评论请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 id（0表示顶级评论）
     */
    private Long parentId;

    /**
     * 回复的用户 id
     */
    private Long replyToId;

    private static final long serialVersionUID = 1L;
}
