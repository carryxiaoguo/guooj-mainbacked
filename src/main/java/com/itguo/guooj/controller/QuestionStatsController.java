package com.itguo.guooj.controller;

import com.itguo.guooj.annotation.AuthCheck;
import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.common.ResultUtils;
import com.itguo.guooj.constant.UserConstant;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.service.QuestionStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 题目统计接口（仅管理员）
 */
@RestController
@RequestMapping("/question/stats")
@Slf4j
public class QuestionStatsController {

    @Resource
    private QuestionStatsService questionStatsService;

    /**
     * 重新计算指定题目的统计信息
     */
    @PostMapping("/recalculate/{questionId}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> recalculateQuestionStats(@PathVariable Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        log.info("管理员触发重新计算题目统计: questionId={}", questionId);
        boolean result = questionStatsService.recalculateQuestionStats(questionId);
        return ResultUtils.success(result);
    }

    /**
     * 重新计算所有题目的统计信息
     */
    @PostMapping("/recalculate/all")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> recalculateAllQuestionStats() {
        log.info("管理员触发重新计算所有题目统计");
        int count = questionStatsService.recalculateAllQuestionStats();
        return ResultUtils.success(count);
    }
}
