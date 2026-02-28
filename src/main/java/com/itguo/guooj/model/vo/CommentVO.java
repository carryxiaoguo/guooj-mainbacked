package com.itguo.guooj.model.vo;

import com.itguo.guooj.model.entity.QuestionComment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论视图
 */
@Data
public class CommentVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 评论用户 id
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 id
     */
    private Long parentId;

    /**
     * 回复的用户 id
     */
    private Long replyToId;

    /**
     * 点赞数
     */
    private Integer likeNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 评论用户信息
     */
    private UserVO userVO;

    /**
     * 回复的用户信息
     */
    private UserVO replyToUserVO;

    /**
     * 子评论列表
     */
    private List<CommentVO> children;

    /**
     * 对象转包装类
     */
    public static CommentVO objToVo(QuestionComment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        return commentVO;
    }

    private static final long serialVersionUID = 1L;
}
