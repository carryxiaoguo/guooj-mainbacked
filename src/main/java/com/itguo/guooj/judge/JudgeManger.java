package com.itguo.guooj.judge;

import com.itguo.guooj.judge.strategy.CJudgeStrategy;
import com.itguo.guooj.judge.strategy.DefaultJudgeStrategy;
import com.itguo.guooj.judge.strategy.JavaJudgeStrategy;
import com.itguo.guooj.judge.strategy.JudgeContext;
import com.itguo.guooj.judge.strategy.JudgeStrategy;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理 简化调用
 */
@Service
public class JudgeManger {
    /**
     * 执行策略
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudgeInfo(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        
        // 根据语言选择判题策略
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategy();
        } else if ("c".equals(language) || "c语言".equals(language)) {
            judgeStrategy = new CJudgeStrategy();
        }
        
        return judgeStrategy.doJudgeInfo(judgeContext);
    }
}
