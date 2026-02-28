package com.itguo.guooj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.judge.JudgeService;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.itguo.guooj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.itguo.guooj.model.entity.Question;
import com.itguo.guooj.model.entity.QuestionSubmit;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.enums.QuestionSubmitLanguageEnum;
import com.itguo.guooj.model.enums.QuestionSubmitStatusEnum;
import com.itguo.guooj.model.vo.QuestionSubmitHistoryVO;
import com.itguo.guooj.model.vo.QuestionSubmitVO;
import com.itguo.guooj.model.vo.QuestionVO;
import com.itguo.guooj.model.vo.UserVO;
import com.itguo.guooj.service.QuestionService;
import com.itguo.guooj.service.QuestionSubmitService;
import com.itguo.guooj.mapper.QuestionSubmitMapper;
import com.itguo.guooj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
* @author xiaoguo
* @description 针对表【question_submit(题目提交表)】的数据库操作Service实现
* @createDate 2025-09-08 21:26:52
*/
@Service
@Slf4j
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;
    @Resource
    @Lazy  //懒加载
    private JudgeService judgeService;
    @Resource
    private Executor judgeExecutor;  // 判题线程池
    
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断题目是否存在，根据类别获取实体
        QuestionSubmitLanguageEnum language = QuestionSubmitLanguageEnum.getEnumByValue(questionSubmitAddRequest.getLanguage());
        if (language == null) {
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        long userId = loginUser.getId();
        String code = questionSubmitAddRequest.getCode();
        if (code == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"代码输入为空");
        }
        QuestionSubmit questionSubmit = QuestionSubmit.builder()
                .userId(userId)
                .questionId(questionId)
                .code(code)
                .language(questionSubmitAddRequest.getLanguage())
                //设置初始状态
                .status(QuestionSubmitStatusEnum.WAITING.getValue())
                .judgeInfo("{}")
                //初始值
                .build();

            boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入失败");
        }
        
        // 使用数据库原子递增操作更新题目提交数，避免并发问题
        boolean updateSuccess = questionService.lambdaUpdate()
            .eq(Question::getId, questionId)
            .setSql("submitNum = submitNum + 1")
            .update();
        
        if (!updateSuccess) {
            log.error("更新题目提交数失败: questionId={}", questionId);
        }
        
        //执行判题服务 进行异步
        Long questionSubmitId = questionSubmit.getId();
        log.info("=== 准备启动异步判题任务,提交ID: {} ===", questionSubmitId);
        
        //异步执行 - 使用自定义线程池
        CompletableFuture.runAsync(() -> {
            try {
                log.info("=== 异步判题任务已启动,提交ID: {}, 线程: {} ===", 
                    questionSubmitId, Thread.currentThread().getName());
                judgeService.doJudge(questionSubmitId);
                log.info("=== 判题完成,提交ID: {} ===", questionSubmitId);
            } catch (Exception e) {
                log.error("=== 判题异常,提交ID: {} ===", questionSubmitId, e);
                // 更新状态为失败
                QuestionSubmit failedSubmit = new QuestionSubmit();
                failedSubmit.setId(questionSubmitId);
                failedSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
                failedSubmit.setJudgeInfo("{\"message\":\"系统错误: " + e.getMessage() + "\"}");
                updateById(failedSubmit);
            }
        }, judgeExecutor);
        
        log.info("=== 异步任务已提交,返回提交ID: {} ===", questionSubmitId);
        return questionSubmitId;

    }

    /**
     * 获取查询题目包装类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        
        //查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        
        // 排序
        queryWrapper.orderBy(ObjectUtils.isNotEmpty(sortField), 
                "ascend".equals(sortOrder), 
                sortField);
        
        return queryWrapper;
    }

    /**
     * 获取题目返回信息
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 1. 关联查询用户信息
        Long userId = questionSubmit.getUserId();
        //处理脱敏
        if (userId != null && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 分页查询题目提交信息
     *
     * @param questionSubmitPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionVOPage(Page<QuestionSubmit> questionSubmitPage, HttpServletRequest request) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
            Long userId = questionSubmit.getUserId();
            User user = null;//用户脱敏
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionSubmitVO.setUserVO(userService.getUserVO(user));
            return questionSubmitVO;
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }


    /**
     * 分页获取提交历史记录
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<QuestionSubmitHistoryVO> getSubmitHistoryPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        Long questionId = questionSubmitQueryRequest.getQuestionId();

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);

        // 构建查询条件
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();

        // 如果指定了题目ID,查询该题的提交记录
        if (questionId != null && questionId > 0) {
            queryWrapper.eq("questionId", questionId);
        }

        // 非管理员只能查看自己的提交记录
        if (!isAdmin) {
            queryWrapper.eq("userId", loginUser.getId());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("createTime");

        // 分页查询
        Page<QuestionSubmit> questionSubmitPage = this.page(new Page<>(current, size), queryWrapper);

        // 转换为VO
        Page<QuestionSubmitHistoryVO> historyVOPage = new Page<>(current, size, questionSubmitPage.getTotal());
        List<QuestionSubmit> records = questionSubmitPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return historyVOPage;
        }

        // 获取用户信息(管理员需要显示用户名)
        Map<Long, User> userMap = null;
        if (isAdmin) {
            Set<Long> userIdSet = records.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
            userMap = userService.listByIds(userIdSet).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        // 转换为VO
        Map<Long, User> finalUserMap = userMap;
        List<QuestionSubmitHistoryVO> historyVOList = records.stream().map(submit -> {
            QuestionSubmitHistoryVO vo = new QuestionSubmitHistoryVO();
            vo.setId(submit.getId());
            vo.setQuestionId(submit.getQuestionId());
            vo.setLanguage(submit.getLanguage());
            vo.setCode(submit.getCode());
            vo.setCreateTime(submit.getCreateTime());
            vo.setStatus(submit.getStatus());
            vo.setJudgeInfo(submit.getJudgeInfo());

            // 管理员显示用户名
            if (isAdmin && finalUserMap != null) {
                User user = finalUserMap.get(submit.getUserId());
                if (user != null) {
                    vo.setUserName(user.getUserName());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        historyVOPage.setRecords(historyVOList);
        return historyVOPage;
    }



}




