package com.itguo.guooj.model.dto.question;

import lombok.Data;

/**
 * 题目用例
 */
@Data
public class JudgeCase {
    //输入用例
    private String inputCase;
    //输出用例
    private String outputCase;
}
