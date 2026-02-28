package com.itguo.guooj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * C语言判题策略
 */
public class CJudgeStrategy implements JudgeStrategy {

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

        // 检查是否已经有错误信息（编译错误、运行时错误等）
        String originalMessage = judgeInfo.getMessage();
        if (originalMessage != null &&
            (originalMessage.contains("compile error") ||
             originalMessage.contains("running error") ||
             originalMessage.contains("time limit exceeded") ||
             originalMessage.contains("memory limit exceeded") ||
             originalMessage.contains("system error"))) {
            judgeInfoResponse.setMessage(originalMessage);
            judgeInfoResponse.setPassCount(0);
            // 所有用例标记为失败
            List<JudgeInfo.CaseResult> details = new ArrayList<>();
            for (int i = 0; i < judgeCaseList.size(); i++) {
                details.add(new JudgeInfo.CaseResult(
                    i + 1, false,
                    judgeCaseList.get(i).getInputCase(),
                    judgeCaseList.get(i).getOutputCase(),
                    originalMessage
                ));
            }
            judgeInfoResponse.setDetails(details);
            return judgeInfoResponse;
        }

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
            String expectedOutput = judgeCaseList.get(i).getOutputCase();
            String actualOutput = outputList.get(i);
            boolean passed = actualOutput != null && Objects.equals(expectedOutput.trim(), actualOutput.trim());
            if (passed) {
                passCount++;
            } else if (judgeInfoMessageEnum == QuestionSubmitJudgeInfoMessageEnum.ACCEPTED) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
            }
            details.add(new JudgeInfo.CaseResult(
                i + 1, passed,
                judgeCaseList.get(i).getInputCase(),
                expectedOutput,
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

            Long adjustedTimeLimit = timeLimit + (timeLimit / 2);
            Long adjustedMemoryLimit = memoryLimit + 102400;

            if (time != null && time != 0 && time > adjustedTimeLimit) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            } else if (memory != null && memory != 0 && memory > adjustedMemoryLimit) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            }
        }

        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
