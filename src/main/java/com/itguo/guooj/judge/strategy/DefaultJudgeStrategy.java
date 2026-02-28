package com.itguo.guooj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认判题策略
 */
public class DefaultJudgeStrategy implements JudgeStrategy {

    @Override
    public JudgeInfo doJudgeInfo(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(time);
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTotalCount(judgeCaseList.size());

        List<JudgeInfo.CaseResult> details = new ArrayList<>();
        int passCount = 0;

        if (outputList.size() != inputList.size()) {
            for (int i = 0; i < judgeCaseList.size(); i++) {
                String actualOutput = i < outputList.size() ? outputList.get(i) : "(无输出)";
                details.add(new JudgeInfo.CaseResult(
                    i + 1, false,
                    judgeCaseList.get(i).getInputCase(),
                    judgeCaseList.get(i).getOutputCase(),
                    actualOutput
                ));
            }
            judgeInfoResponse.setPassCount(0);
            judgeInfoResponse.setDetails(details);
            judgeInfoResponse.setMessage(QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return judgeInfoResponse;
        }

        QuestionSubmitJudgeInfoMessageEnum judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.ACCEPTED;
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            String actualOutput = outputList.get(i);
            boolean passed = judgeCase.getOutputCase().equals(actualOutput);
            if (passed) {
                passCount++;
            } else if (judgeInfoMessageEnum == QuestionSubmitJudgeInfoMessageEnum.ACCEPTED) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
            }
            details.add(new JudgeInfo.CaseResult(
                i + 1, passed,
                judgeCase.getInputCase(),
                judgeCase.getOutputCase(),
                actualOutput != null ? actualOutput : "(无输出)"
            ));
        }

        judgeInfoResponse.setPassCount(passCount);
        judgeInfoResponse.setDetails(details);

        if (judgeInfoMessageEnum == QuestionSubmitJudgeInfoMessageEnum.ACCEPTED) {
            String judgeConfigStr = question.getJudgeConfig();
            JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
            Long memoryLimit = judgeConfig.getMemoryLimit();
            Long timeLimit = judgeConfig.getTimeLimit();

            if (time != null && time > timeLimit) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            } else if (memory != null && memory > memoryLimit) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            }
        }

        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
