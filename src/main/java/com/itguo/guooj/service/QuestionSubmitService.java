package com.itguo.guooj.service;

import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itguo.guooj.model.entity.User;

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


}
