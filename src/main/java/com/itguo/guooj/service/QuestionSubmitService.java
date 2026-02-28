package com.itguo.guooj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.vo.QuestionSubmitHistoryVO;
import com.itguo.guooj.model.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author xiaoguo
* @description 针对表【question_submit(题目提交表)】的数据库操作Service
* @createDate 2025-09-08 21:26:52
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param request
     * @return
     */
    Page<QuestionSubmitVO> getQuestionVOPage(Page<QuestionSubmit> questionSubmitPage, HttpServletRequest request);


    /**
     * 分页获取提交历史记录
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    Page<QuestionSubmitHistoryVO> getSubmitHistoryPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request);


}
