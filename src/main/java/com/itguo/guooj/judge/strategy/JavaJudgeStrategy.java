package com.itguo.guooj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;

import java.util.Objects;
import java.util.Optional;

import java.util.List;

/**
 * 默认判题策略
 */
public class JavaJudgeStrategy implements JudgeStrategy {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
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
        //先给judgeInfoMessageEnum一个值
        QuestionSubmitJudgeInfoMessageEnum judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.ACCEPTED;
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
//       1. 先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            // 保留time和memory
            judgeInfoResponse.setTime(time);
            judgeInfoResponse.setMemory(memory);
            return judgeInfoResponse;
        }
        //2. 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            String outputCase = judgeCaseList.get(i).getOutputCase();
            String actualOutput = outputList.get(i);
            // 检查空值
            if (judgeCaseList.get(i) == null || actualOutput == null) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                // 保留time和memory
                judgeInfoResponse.setTime(time);
                judgeInfoResponse.setMemory(memory);
                return judgeInfoResponse;
            }
            // 输出用例是否与用户运行代码所给的输出用例相等
            if (!Objects.equals(outputCase, actualOutput)) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                // 保留time和memory
                judgeInfoResponse.setTime(time);
                judgeInfoResponse.setMemory(memory);
                return judgeInfoResponse;
            }
        }
        //  3. 判断限制条件是否符合
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long memoryLimit = judgeConfig.getMemoryLimit();
        Long timeLimit = judgeConfig.getTimeLimit();
        
        // Java特殊处理：适当放宽限制
        // 时间限制增加30%的容错空间（JVM启动和类加载有开销）
        Long adjustedTimeLimit = timeLimit + (timeLimit * 3 / 10);
        // 内存限制增加200KB的容错空间（JVM基础内存开销）
        Long adjustedMemoryLimit = memoryLimit + 204800; // 增加200MB
        
        // 检查时间限制(如果time不为null且不为0)
        if (time != null && time != 0 && time > adjustedTimeLimit) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        
        // 检查内存限制(如果memory不为null且不为0)
        if (memory != null && memory != 0 && memory > adjustedMemoryLimit) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        //返回输出
        return judgeInfoResponse;
    }
}
