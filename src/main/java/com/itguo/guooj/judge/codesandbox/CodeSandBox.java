package com.itguo.guooj.judge.codesandbox;

import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeResponse;


public interface CodeSandBox {
    //定义代码沙箱接口 输入ExecuteCodeRequest(请求) 输出ExecuteCodeResponse(响应)
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
