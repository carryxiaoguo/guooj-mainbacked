package com.itguo.guooj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itguo.guooj.annotation.AuthCheck;
import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.DeleteRequest;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.common.ResultUtils;
import com.itguo.guooj.constant.UserConstant;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.exception.ThrowUtils;
import com.itguo.guooj.model.dto.comment.CommentAddRequest;
import com.itguo.guooj.model.dto.comment.CommentQueryRequest;
import com.itguo.guooj.model.entity.QuestionComment;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.vo.CommentVO;
import com.itguo.guooj.service.QuestionCommentService;
import com.itguo.guooj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/question_comment")
@Slf4j
public class QuestionCommentController {

    @Resource
    private QuestionCommentService questionCommentService;

    @Resource
    private UserService userService;

    /**
     * 创建评论
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        log.info("收到评论请求: {}", commentAddRequest);
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionComment comment = new QuestionComment();
        BeanUtils.copyProperties(commentAddRequest, comment);
        
        log.info("复制后的comment: questionId={}, content={}, parentId={}, replyToId={}", 
                comment.getQuestionId(), comment.getContent(), comment.getParentId(), comment.getReplyToId());
        
        // 设置默认值
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getReplyToId() == null) {
            comment.setReplyToId(0L);
        }
        comment.setLikeNum(0);
        
        questionCommentService.validComment(comment, true);
        User loginUser = userService.getLoginUser(request);
        comment.setUserId(loginUser.getId());
        boolean result = questionCommentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除评论
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionComment oldComment = questionCommentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionCommentService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取评论
     */
    @GetMapping("/get/vo")
    public BaseResponse<CommentVO> getCommentVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionComment comment = questionCommentService.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionCommentService.getCommentVO(comment, request));
    }

    /**
     * 分页获取评论列表（仅管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionComment>> listCommentByPage(@RequestBody CommentQueryRequest commentQueryRequest) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        Page<QuestionComment> commentPage = questionCommentService.page(new Page<>(current, size),
                questionCommentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(commentPage);
    }

    /**
     * 分页获取评论列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
            HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionComment> commentPage = questionCommentService.page(new Page<>(current, size),
                questionCommentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(questionCommentService.getCommentVOPage(commentPage, request));
    }

    /**
     * 获取评论树（包含子评论）
     */
    @GetMapping("/tree")
    public BaseResponse<List<CommentVO>> getCommentTree(@RequestParam Long questionId, HttpServletRequest request) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<CommentVO> commentTree = questionCommentService.getCommentTree(questionId, request);
        return ResultUtils.success(commentTree);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likeComment(@RequestParam Long commentId, HttpServletRequest request) {
        if (commentId == null || commentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QuestionComment comment = questionCommentService.getById(commentId);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 简单实现：直接增加点赞数（实际应该记录用户点赞关系）
        comment.setLikeNum(comment.getLikeNum() + 1);
        boolean result = questionCommentService.updateById(comment);
        return ResultUtils.success(result);
    }
}
