package com.itguo.guooj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itguo.guooj.model.dto.comment.CommentQueryRequest;
import com.itguo.guooj.model.entity.QuestionComment;
import com.itguo.guooj.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评论服务
 */
public interface QuestionCommentService extends IService<QuestionComment> {

    /**
     * 校验评论
     */
    void validComment(QuestionComment comment, boolean add);

    /**
     * 获取查询条件
     */
    QueryWrapper<QuestionComment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    /**
     * 获取评论封装
     */
    CommentVO getCommentVO(QuestionComment comment, HttpServletRequest request);

    /**
     * 分页获取评论封装
     */
    Page<CommentVO> getCommentVOPage(Page<QuestionComment> commentPage, HttpServletRequest request);

    /**
     * 获取评论树（包含子评论）
     */
    List<CommentVO> getCommentTree(Long questionId, HttpServletRequest request);
}
