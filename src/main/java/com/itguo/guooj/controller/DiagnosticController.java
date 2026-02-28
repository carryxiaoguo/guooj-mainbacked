package com.itguo.guooj.controller;

import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 诊断控制器
 */
@RestController
@RequestMapping("/diagnostic")
@Slf4j
public class DiagnosticController {

    @Resource(name = "judgeExecutor")
    private Executor judgeExecutor;

    @GetMapping("/test-async")
    public BaseResponse<Map<String, Object>> testAsync() {
        Map<String, Object> result = new HashMap<>();
        
        log.info("=== 开始测试异步任务 ===");
        result.put("step1", "测试开始");
        
        // 测试异步任务
        CompletableFuture.runAsync(() -> {
            log.info("=== 异步任务执行中,线程: {} ===", Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("=== 异步任务完成 ===");
        }, judgeExecutor);
        
        result.put("step2", "异步任务已提交");
        log.info("=== 测试完成,请查看日志中是否有异步任务执行的输出 ===");
        
        return ResultUtils.success(result);
    }
}
