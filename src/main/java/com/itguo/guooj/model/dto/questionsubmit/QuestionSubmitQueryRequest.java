package com.itguo.guooj.model.dto.questionsubmit;

import com.itguo.guooj.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private Integer status;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 用户 id
     */
    private Long userId;


}
