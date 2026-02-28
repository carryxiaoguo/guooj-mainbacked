package com.itguo.guooj.judge.codesandbox;

import com.itguo.guooj.judge.codesandbox.impl.ExampleCodeSandBox;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeResponse;
import com.itguo.guooj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@SpringBootTest
class CodeSandBoxTest {
    @Value("${codesandbox.type:remote}")
    private String type;

    @Test
    void executeCode() {
        //一个个写太麻烦 现在就需要工厂模式
        CodeSandBox examplecodeSandBox = new ExampleCodeSandBox();//示例
        String code = "print(hello world)";
        String language = QuestionSubmitLanguageEnum.PYTHON.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        ExecuteCodeResponse exampleCodeResponse = examplecodeSandBox.executeCode(build);
        Assertions.assertNotNull(exampleCodeResponse);//判断是否为空   --- 当前为空

    }

    @Test
    void executeCodeByValue() {
        CodeSandBox examplecodeSandBox = CodeSendBoxFactory.newInstance(type);//示例
        String code = "print(hello world)";
        String language = QuestionSubmitLanguageEnum.PYTHON.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        ExecuteCodeResponse exampleCodeResponse = examplecodeSandBox.executeCode(build);
        Assertions.assertNotNull(exampleCodeResponse);//判断是否为空   --- 当前为空

    }

    @Test
    void executeCodeProxy() {
        CodeSandBox codeSandBox = CodeSendBoxFactory.newInstance(type); //远程
        codeSandBox = new CodeSanBoxProxy(codeSandBox); //获取传入参数examplecodeSandBox的日志
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果: \"+(a+b));\n" +
                "    }\n" +
                "}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        ExecuteCodeResponse codeResponse = codeSandBox.executeCode(build);
        Assertions.assertNotNull(codeResponse);//判断是否为空   --- 当前为空

    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String type = scanner.next();
            CodeSandBox codeSandBox = CodeSendBoxFactory.newInstance(type);
            String code = "print(hello world)";
            String language = QuestionSubmitLanguageEnum.PYTHON.getValue();
            List<String> inputList = Arrays.asList("1 2", "3 4");
            ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                    .code(code)
                    .inputList(inputList)
                    .language(language)
                    .build();
            ExecuteCodeResponse exampleCodeResponse = codeSandBox.executeCode(build);
        }
    }
}