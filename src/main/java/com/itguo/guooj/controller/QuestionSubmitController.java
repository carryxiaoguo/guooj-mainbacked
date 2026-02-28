package com.itguo.guooj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.common.ResultUtils;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.service.QuestionSubmitService;
import com.itguo.guooj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 */
@RestController
//@RequestMapping("/question_Submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

   /* @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    *//**
     * 题目提交id
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return
     *//*
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能提交
        final User loginUser = userService.getLoginUser(request);
        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    *//**
     * 分页查询题目提交了列表  (除Admin以外,RegulaUser只能看到非答案,提交代码等公开信息)
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     *//*
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmit>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        Page<QuestionSubmit> questionSubmitPagePage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitPagePage);
    }*/

}
