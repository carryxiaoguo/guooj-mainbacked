package com.itguo.guooj.service;

/**
 * 题目统计服务
 */
public interface QuestionStatsService {
    
    /**
     * 重新计算指定题目的提交数和通过数
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean recalculateQuestionStats(Long questionId);
    
    /**
     * 重新计算所有题目的提交数和通过数
     * @return 更新的题目数量
     */
    int recalculateAllQuestionStats();
}
