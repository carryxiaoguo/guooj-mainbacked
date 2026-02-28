package com.itguo.guooj.judge;

import cn.hutool.json.JSONUtil;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.judge.codesandbox.CodeSanBoxProxy;
import com.itguo.guooj.judge.codesandbox.CodeSandBox;
import com.itguo.guooj.judge.codesandbox.CodeSendBoxFactory;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeRequest;
import com.itguo.guooj.judge.codesandbox.model.ExecuteCodeResponse;
import com.itguo.guooj.judge.strategy.JudgeContext;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.itguo.guooj.model.enums.QuestionSubmitStatusEnum;
import com.itguo.guooj.service.QuestionService;
import com.itguo.guooj.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例代码沙箱判题服务
 */
@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private com.itguo.guooj.service.UserQuestionAcceptService userQuestionAcceptService;
    @Value("${codesandbox.type:example}")
    private String type;
    @Resource
    private JudgeManger judgeManger;

    @Override
    public QuestionSubmit doJudge(Long questionSubmitId) {
      /*  判题服务业务的流程
        1. 传入题目的id获取相应的题目信息
        2. 调用沙箱,获取执行结果
        3. 根据沙箱的执行结果,设置题目的判题状态和信息.*/

        //根据提交题目id获取题目信息
        QuestionSubmit submitInfo = questionSubmitService.getById(questionSubmitId);
        if (submitInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        //提交id
        Long id = submitInfo.getQuestionId();
        //题库是否存在此题
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        //如果题目状态是等待中
        if (!submitInfo.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题等待中");
        }
        
        // 更新状态为判题中
        boolean updateToRunning = questionSubmitService.lambdaUpdate()
            .eq(QuestionSubmit::getId, questionSubmitId)
            .set(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.RUNNING.getValue())
            .update();
            
        if (!updateToRunning) {
            log.error("更新判题状态为运行中失败: submitId={}", questionSubmitId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新判题状态失败");
        }
        
        //调用代码沙箱
        CodeSandBox codeSandBox = CodeSendBoxFactory.newInstance(type);//调用代码沙箱
        codeSandBox = new CodeSanBoxProxy(codeSandBox);  //获取传入参数exampleCodeSandBox的日志
        String language = submitInfo.getLanguage();
        String code = submitInfo.getCode();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInputCase).collect(Collectors.toList());
        ExecuteCodeRequest build = ExecuteCodeRequest.builder()
                .code(code)
                .inputList(inputList)
                .language(language)
                .build();
        //开始调用代码沙箱  此处用的是远程代码沙箱
        ExecuteCodeResponse exampleCodeResponse = codeSandBox.executeCode(build);
        List<String> outputList = exampleCodeResponse.getOutputList();
        //根据沙箱的执行结果,设置题目的判题状态和信息. (开始判题)
        JudgeContext judgeContext = new JudgeContext();//设置一个判题上下文
        judgeContext.setJudgeInfo(exampleCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(submitInfo);
        //判断额外语言 进行判题
        JudgeInfo judgeInfo = judgeManger.doJudgeInfo(judgeContext);
        log.info("判题完成,结果: message={}, time={}ms, memory={}KB", 
            judgeInfo.getMessage(), judgeInfo.getTime(), judgeInfo.getMemory());
        
        //再次更新数据库 修改数据库判题结果
        QuestionSubmit originalSubmitInfo = submitInfo;
        
        log.info("准备更新判题信息到数据库: submitId={}, status={}, judgeInfo={}", 
            questionSubmitId, QuestionSubmitStatusEnum.SUCCEED.getValue(), JSONUtil.toJsonStr(judgeInfo));
        
        // 验证记录是否存在且未被删除
        QuestionSubmit checkSubmit = questionSubmitService.getById(questionSubmitId);
        if (checkSubmit == null) {
            log.error("更新失败: 提交记录不存在, submitId={}", questionSubmitId);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        if (checkSubmit.getIsDelete() != null && checkSubmit.getIsDelete() == 1) {
            log.error("更新失败: 提交记录已被删除, submitId={}", questionSubmitId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交记录已被删除");
        }
        
        // 根据判题结果设置状态
        Integer finalStatus;
        if ("accepted".equals(judgeInfo.getMessage())) {
            finalStatus = QuestionSubmitStatusEnum.SUCCEED.getValue(); // 2 - 成功
        } else {
            finalStatus = QuestionSubmitStatusEnum.FAILED.getValue(); // 3 - 失败
        }
        
        // 使用 LambdaUpdateWrapper 确保更新成功
        boolean update = questionSubmitService.lambdaUpdate()
            .eq(QuestionSubmit::getId, questionSubmitId)
            .set(QuestionSubmit::getStatus, finalStatus)
            .set(QuestionSubmit::getJudgeInfo, JSONUtil.toJsonStr(judgeInfo))
            .update();
            
        log.info("更新数据库结果: {}, 判题状态: {}", update ? "成功" : "失败", finalStatus);
        
        if (!update) {
            log.error("更新题目提交失败: submitId={}, 可能原因: 1.并发更新冲突 2.数据库连接问题", questionSubmitId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目提交更新失败");
        }
        
        // 判断是否通过，如果通过则保存通过记录
        if ("accepted".equals(judgeInfo.getMessage())) {
            log.info("用户 {} 通过题目 {}", originalSubmitInfo.getUserId(), id);
            userQuestionAcceptService.saveAcceptRecord(
                originalSubmitInfo.getUserId(),
                id,
                questionSubmitId,
                code,
                language
            );
            
            // 使用数据库原子递增操作更新题目通过数，避免并发问题
            boolean updateSuccess = questionService.lambdaUpdate()
                .eq(Question::getId, id)
                .setSql("acceptNum = acceptNum + 1")
                .update();
            
            if (!updateSuccess) {
                log.error("更新题目通过数失败: questionId={}", id);
            }
        }
        
        return questionSubmitService.getById(questionSubmitId);
    }
}
