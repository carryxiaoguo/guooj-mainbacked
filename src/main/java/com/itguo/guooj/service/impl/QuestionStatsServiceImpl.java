package com.itguo.guooj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.itguo.guooj.service.QuestionService;
import com.itguo.guooj.service.QuestionStatsService;
import com.itguo.guooj.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题目统计服务实现
 */
@Service
@Slf4j
public class QuestionStatsServiceImpl implements QuestionStatsService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    public boolean recalculateQuestionStats(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return false;
        }

        // 查询题目是否存在
        Question question = questionService.getById(questionId);
        if (question == null) {
            log.warn("题目不存在: questionId={}", questionId);
            return false;
        }

        // 统计提交数
        QueryWrapper<QuestionSubmit> submitWrapper = new QueryWrapper<>();
        submitWrapper.eq("questionId", questionId);
        submitWrapper.eq("isDelete", 0);
        long submitCount = questionSubmitService.count(submitWrapper);

        // 统计通过数 - 从judgeInfo JSON中查找包含"accepted"的记录
        QueryWrapper<QuestionSubmit> acceptWrapper = new QueryWrapper<>();
        acceptWrapper.eq("questionId", questionId);
        acceptWrapper.eq("isDelete", 0);
        acceptWrapper.like("judgeInfo", "\"message\":\"accepted\"");
        long acceptCount = questionSubmitService.count(acceptWrapper);

        // 更新题目统计信息
        Question updateQuestion = new Question();
        updateQuestion.setId(questionId);
        updateQuestion.setSubmitNum((int) submitCount);
        updateQuestion.setAcceptNum((int) acceptCount);
        
        boolean result = questionService.updateById(updateQuestion);
        
        if (result) {
            log.info("题目统计更新成功: questionId={}, submitNum={}, acceptNum={}", 
                questionId, submitCount, acceptCount);
        } else {
            log.error("题目统计更新失败: questionId={}", questionId);
        }
        
        return result;
    }

    @Override
    public int recalculateAllQuestionStats() {
        // 获取所有题目
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);
        List<Question> questionList = questionService.list(queryWrapper);

        int successCount = 0;
        for (Question question : questionList) {
            if (recalculateQuestionStats(question.getId())) {
                successCount++;
            }
        }

        log.info("批量更新题目统计完成: 总数={}, 成功={}", questionList.size(), successCount);
        return successCount;
    }
}
