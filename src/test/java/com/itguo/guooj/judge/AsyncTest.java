package com.itguo.guooj.judge;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class AsyncTest {

    @Test
    public void testAsync() throws InterruptedException {
        log.info("主线程开始");
        
        CompletableFuture.runAsync(() -> {
            log.info("异步任务开始执行");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("异步任务执行完成");
        });
        
        log.info("主线程继续执行");
        
        // 等待异步任务完成
        Thread.sleep(3000);
        log.info("测试结束");
    }
}
