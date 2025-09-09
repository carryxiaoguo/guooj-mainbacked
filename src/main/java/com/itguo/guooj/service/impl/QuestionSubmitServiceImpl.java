package com.itguo.guooj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;
import com.itguo.guooj.model.enums.QuestionSubmitLanguageEnum;
import com.itguo.guooj.model.enums.QuestionSubmitStatusEnum;
import com.itguo.guooj.service.QuestionService;
import com.itguo.guooj.service.QuestionSubmitService;
import com.itguo.guooj.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author xiaoguo
* @description 针对表【question_submit(题目提交表)】的数据库操作Service实现
* @createDate 2025-09-08 21:26:52
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    @Resource
    private QuestionService questionService;

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断题目是否存在，根据类别获取实体 TODO 编程语言是否合法
        QuestionSubmitLanguageEnum language = QuestionSubmitLanguageEnum.getEnumByValue(questionSubmitAddRequest.getLanguage());
        if (language == null) {
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已题目提交
        long userId = loginUser.getId();
        // 每个用户串行题目提交
        QuestionSubmit questionSubmit = QuestionSubmit.builder()
                .userId(userId)
                .questionId(questionId)
                .code(questionSubmitAddRequest.getCode())
                .language(questionSubmitAddRequest.getLanguage())
                //设置初始状态
                .status(QuestionSubmitStatusEnum.WAITING.getValue())
                .judgeInfo("{}")
                .build();
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入失败");
        }
        return questionSubmit.getId();

    }

}




