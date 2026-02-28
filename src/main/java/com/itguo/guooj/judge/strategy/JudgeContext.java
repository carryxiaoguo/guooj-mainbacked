package com.itguo.guooj.judge.strategy;


import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上下文(用于定义在策略中传递的参数)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeContext {


    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private QuestionSubmit questionSubmit;

    private Question question;
}
