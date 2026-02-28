package com.itguo.guooj.judge.codesandbox;

import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码沙箱的代理
 */
@Slf4j
@AllArgsConstructor
public class CodeSanBoxProxy implements CodeSandBox {
    //创建一个实例
    private CodeSandBox codeSandBox;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        if (executeCodeRequest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"请求数据不存在");
        }
        log.info("代码沙箱请求信息:{}", executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        if (executeCodeResponse == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"返回数据不存在");
        }
        executeCodeResponse.setMessage("代码沙箱引用成功");
        log.info("代码沙箱返回信息:{}", executeCodeResponse);
        return executeCodeResponse;
    }
}
