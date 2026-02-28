package com.itguo.guooj.judge;

import com.itguo.guooj.model.entity.QuestionSubmit;

/**
 * 后期会抽象成微服务 提前隔离开 为后期做准备
 */
public interface JudgeService {

    QuestionSubmit doJudge(Long questionSubmitId);
}
