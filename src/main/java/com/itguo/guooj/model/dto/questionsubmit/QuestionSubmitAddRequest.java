package com.itguo.guooj.model.dto.questionsubmit;

import lombok.Data;

@Data
public class QuestionSubmitAddRequest {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目 id
     */
    private Long questionId;



}
