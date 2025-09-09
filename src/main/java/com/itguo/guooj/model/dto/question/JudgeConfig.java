package com.itguo.guooj.model.dto.question;

import lombok.Data;

/**
 * 题目用例
 */
@Data
public class JudgeConfig {
    //内存限制(kb)
    private long memoryLimit;

    //时间限制(ms)
    private long TimeLimit;

    //堆栈限制(kb)
    private long stackLimit;

}
