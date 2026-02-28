package com.itguo.guooj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Java判题策略
 */
public class JavaJudgeStrategy implements JudgeStrategy {

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

        // 构建每个测试用例的详细结果
        List<JudgeInfo.CaseResult> details = new ArrayList<>();
        int passCount = 0;

        // 如果输出数量不匹配，标记所有用例
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

        // 逐个比较测试用例
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

        // 如果所有用例都通过，再检查资源限制
        if (judgeInfoMessageEnum == QuestionSubmitJudgeInfoMessageEnum.ACCEPTED) {
            String judgeConfigStr = question.getJudgeConfig();
            JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
            Long memoryLimit = judgeConfig.getMemoryLimit();
            Long timeLimit = judgeConfig.getTimeLimit();

            // Java特殊处理：适当放宽限制
            Long adjustedTimeLimit = timeLimit + (timeLimit * 3 / 10);
            Long adjustedMemoryLimit = memoryLimit + 204800;

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
