package com.itguo.guooj.judge.strategy;

import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;

/**
 * 判题策略
 */
public interface JudgeStrategy {
    /**
     * 执行策略
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudgeInfo(JudgeContext judgeContext);

}
