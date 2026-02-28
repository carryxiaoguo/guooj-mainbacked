package com.itguo.guooj.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * 
 */
@Getter
public enum QuestionSubmitJudgeInfoMessageEnum {

    ACCEPTED("成功", "accepted"),
    WRONG_ANSWER("答案错误","wrong Answer"),
    COMPILE_ERROR("compile error","编译错误"),
    MEMORY_LIMIT_EXCEEDED("memory limit ","内存溢出"),
    TIME_LIMIT_EXCEEDED("time limit exceeded","时间超时"),
    PRESENTATION_ERROR("presentation error", "展示错误"),
    WAITING("Waiting", "等待"),
    OUTPUT_LIMIT_EXCEEDED("output limit exceeded", "输出溢出"),
    DANGEROUS_OPERATION("dangerous operation", "危险操作"),
    RUNNING_ERROR("running error", "运行错误"),
    SYSTEM_ERROR("system error", "系统错误");


    private final String text;

    private final String value;

    QuestionSubmitJudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static QuestionSubmitJudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (QuestionSubmitJudgeInfoMessageEnum anEnum : QuestionSubmitJudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
