package com.itguo.guooj.judge.codesandbox.model;


import lombok.Data;


import java.util.List;


@Data
public class ExecuteCodeResponse {
    /**
     * 输出信息
     */
    private List<String> outputList;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
}
