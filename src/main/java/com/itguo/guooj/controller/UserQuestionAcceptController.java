package com.itguo.guooj.controller;

import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.common.ResultUtils;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.entity.UserQuestionAccept;
import com.itguo.guooj.service.UserQuestionAcceptService;
import com.itguo.guooj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户题目通过记录接口
 */
@RestController
@RequestMapping("/accept")
@Slf4j
public class UserQuestionAcceptController {

    @Resource
    private UserQuestionAcceptService userQuestionAcceptService;

    @Resource
    private UserService userService;

    /**
     * 获取用户通过记录
     */
    @GetMapping("/get")
    public BaseResponse<UserQuestionAccept> getAcceptRecord(
            @RequestParam("questionId") Long questionId,
            HttpServletRequest request) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        UserQuestionAccept acceptRecord = userQuestionAcceptService.getAcceptRecord(
                loginUser.getId(), questionId);
        return ResultUtils.success(acceptRecord);
    }

    /**
     * 检查用户是否通过题目
     */
    @GetMapping("/check")
    public BaseResponse<Boolean> hasAccepted(
            @RequestParam("questionId") Long questionId,
            HttpServletRequest request) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean hasAccepted = userQuestionAcceptService.hasAccepted(
                loginUser.getId(), questionId);
        return ResultUtils.success(hasAccepted);
    }
}
