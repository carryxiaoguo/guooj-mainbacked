package com.itguo.guooj.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.judge.codesandbox.CodeSandBox;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * 远程代码沙箱 实际调用接口
 */
@Slf4j
public class RemoteCodeSandBox implements CodeSandBox {
    //添加鉴权 防止代码沙箱模块在公网裸奔 设置请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";

    private String authSecret;
    private String sandboxUrl;

    public RemoteCodeSandBox(String sandboxUrl, String authSecret) {
        this.sandboxUrl = sandboxUrl;
        this.authSecret = authSecret;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("远程代码沙箱");
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(sandboxUrl)
                .body(json)
                .header(AUTH_REQUEST_HEADER, authSecret)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR,"executeCode remoteSandbox error ,message="+responseStr);
        }
        return JSONUtil.toBean(responseStr,ExecuteCodeResponse.class);
    }
}
