package com.itguo.guooj.judge.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;
    /**
     * 消耗的内存 (KB)
     */
    private Long memory;
    /**
     * 执行的时间 (ms)
     */
    private Long time;

    /**
     * 通过的测试用例数
     */
    private Integer passCount;

    /**
     * 总测试用例数
     */
    private Integer totalCount;

    /**
     * 每个测试用例的详细结果
     */
    private List<CaseResult> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CaseResult {
        /**
         * 用例序号（从1开始）
         */
        private Integer index;
        /**
         * 是否通过
         */
        private Boolean passed;
        /**
         * 输入
         */
        private String input;
        /**
         * 期望输出
         */
        private String expectedOutput;
        /**
         * 实际输出
         */
        private String actualOutput;
    }
}
