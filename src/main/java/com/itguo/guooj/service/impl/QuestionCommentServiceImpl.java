package com.itguo.guooj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.exception.ThrowUtils;
import com.itguo.guooj.mapper.QuestionCommentMapper;
import com.itguo.guooj.model.dto.comment.CommentQueryRequest;
import com.itguo.guooj.model.entity.QuestionComment;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.vo.CommentVO;
import com.itguo.guooj.model.vo.UserVO;
import com.itguo.guooj.service.QuestionCommentService;
import com.itguo.guooj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment>
        implements QuestionCommentService {

    @Resource
    private UserService userService;

    @Override
    public void validComment(QuestionComment comment, boolean add) {
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = comment.getContent();
        Long questionId = comment.getQuestionId();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(content), ErrorCode.PARAMS_ERROR, "评论内容不能为空");
            ThrowUtils.throwIf(questionId == null || questionId <= 0, ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长");
        }
    }

    @Override
    public QueryWrapper<QuestionComment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }
        Long questionId = commentQueryRequest.getQuestionId();
        Long userId = commentQueryRequest.getUserId();
        Long parentId = commentQueryRequest.getParentId();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(questionId != null, "questionId", questionId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(parentId != null, "parentId", parentId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public CommentVO getCommentVO(QuestionComment comment, HttpServletRequest request) {
        CommentVO commentVO = CommentVO.objToVo(comment);
        // 关联查询用户信息
        Long userId = comment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        commentVO.setUserVO(userVO);

        // 关联查询回复用户信息
        Long replyToId = comment.getReplyToId();
        if (replyToId != null && replyToId > 0) {
            User replyToUser = userService.getById(replyToId);
            UserVO replyToUserVO = userService.getUserVO(replyToUser);
            commentVO.setReplyToUserVO(replyToUserVO);
        }

        return commentVO;
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<QuestionComment> commentPage, HttpServletRequest request) {
        List<QuestionComment> commentList = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (commentList.isEmpty()) {
            return commentVOPage;
        }
        // 关联查询用户信息
        Set<Long> userIdSet = commentList.stream().map(QuestionComment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<CommentVO> commentVOList = commentList.stream().map(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            Long userId = comment.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentVO.setUserVO(userService.getUserVO(user));
            return commentVO;
        }).collect(Collectors.toList());
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

    @Override
    public List<CommentVO> getCommentTree(Long questionId, HttpServletRequest request) {
        // 查询所有评论
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.eq("isDelete", 0);
        queryWrapper.orderByAsc("createTime");
        List<QuestionComment> allComments = this.list(queryWrapper);

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO并关联用户信息
        Set<Long> userIdSet = allComments.stream().map(QuestionComment::getUserId).collect(Collectors.toSet());
        allComments.stream().map(QuestionComment::getReplyToId)
                .filter(id -> id != null && id > 0)
                .forEach(userIdSet::add);
        
        Map<Long, User> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<CommentVO> commentVOList = allComments.stream().map(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            User user = userMap.get(comment.getUserId());
            commentVO.setUserVO(userService.getUserVO(user));
            
            if (comment.getReplyToId() != null && comment.getReplyToId() > 0) {
                User replyToUser = userMap.get(comment.getReplyToId());
                commentVO.setReplyToUserVO(userService.getUserVO(replyToUser));
            }
            return commentVO;
        }).collect(Collectors.toList());

        // 构建树形结构
        Map<Long, List<CommentVO>> childrenMap = commentVOList.stream()
                .filter(comment -> comment.getParentId() != null && comment.getParentId() > 0)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        // 设置子评论
        commentVOList.forEach(comment -> {
            List<CommentVO> children = childrenMap.get(comment.getId());
            comment.setChildren(children != null ? children : new ArrayList<>());
        });

        // 返回顶级评论
        return commentVOList.stream()
                .filter(comment -> comment.getParentId() == null || comment.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
