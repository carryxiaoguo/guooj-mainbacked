package com.itguo.guooj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.enums.QuestionSubmitJudgeInfoMessageEnum;

import java.util.List;
import java.util.Objects;

/**
 * C语言判题策略
 */
public class CJudgeStrategy implements JudgeStrategy {

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
        
        // 检查是否已经有错误信息（编译错误、运行时错误等）
        String originalMessage = judgeInfo.getMessage();
        if (originalMessage != null && 
            (originalMessage.contains("compile error") || 
             originalMessage.contains("running error") || 
             originalMessage.contains("time limit exceeded") || 
             originalMessage.contains("memory limit exceeded") ||
             originalMessage.contains("system error"))) {
            // 直接返回原始错误信息
            judgeInfoResponse.setMessage(originalMessage);
            return judgeInfoResponse;
        }
        
        // 先给judgeInfoMessageEnum一个值
        QuestionSubmitJudgeInfoMessageEnum judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.ACCEPTED;
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        
        // 1. 先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            judgeInfoResponse.setTime(time);
            judgeInfoResponse.setMemory(memory);
            return judgeInfoResponse;
        }
        
        // 2. 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            String outputCase = judgeCaseList.get(i).getOutputCase();
            String actualOutput = outputList.get(i);
            String inputCase = judgeCaseList.get(i).getInputCase();
            
            // 检查空值
            if (judgeCaseList.get(i) == null || actualOutput == null) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                judgeInfoResponse.setTime(time);
                judgeInfoResponse.setMemory(memory);
                return judgeInfoResponse;
            }
            
            // 输出用例是否与用户运行代码所给的输出用例相等
            if (!Objects.equals(outputCase, actualOutput)) {
                judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                judgeInfoResponse.setTime(time);
                judgeInfoResponse.setMemory(memory);
                return judgeInfoResponse;
            }
        }
        
        // 3. 判断限制条件是否符合
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long memoryLimit = judgeConfig.getMemoryLimit();
        Long timeLimit = judgeConfig.getTimeLimit();
        
        // C语言特殊处理：适当放宽限制
        // 时间限制增加50%的容错空间（C语言编译后执行速度快，但启动有开销）
        Long adjustedTimeLimit = timeLimit + (timeLimit / 2);
        // 内存限制增加100KB的容错空间（C语言内存占用小，但进程启动有基础开销）
        Long adjustedMemoryLimit = memoryLimit + 102400; // 增加100MB
        
        // 检查时间限制(如果time不为null且不为0)
        if (time != null && time != 0 && time > adjustedTimeLimit) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        
        // 检查内存限制(如果memory不为null且不为0)
        // C语言通常内存占用较小，可以适当放宽限制
        if (memory != null && memory != 0 && memory > adjustedMemoryLimit) {
            judgeInfoMessageEnum = QuestionSubmitJudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        
        // 返回输出
        return judgeInfoResponse;
    }
}
