package com.itguo.guooj.judge.codesandbox;


import com.itguo.guooj.judge.codesandbox.impl.ExampleCodeSandBox;
import com.itguo.guooj.judge.codesandbox.impl.RemoteCodeSandBox;
import com.itguo.guooj.judge.codesandbox.impl.ThirdPartyCodeSandBox;

/**
 * 创建代码沙箱 工厂模式
 * 根据字符串参数穿件指定的代码沙箱实例
 */
public class CodeSendBoxFactory {
    /**
     * 此处传入的代码沙箱的类型
     *
     * @param type
     * @param sandboxUrl 代码沙箱URL
     * @param authSecret 认证密钥
     * @return
     */
    public static CodeSandBox newInstance(String type, String sandboxUrl, String authSecret) {
        switch (type) {
            case "example":
                return new ExampleCodeSandBox();
            case "remote":
                return new RemoteCodeSandBox(sandboxUrl, authSecret);
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            default:
                return new ExampleCodeSandBox();//返回默认
        }
    }
}
