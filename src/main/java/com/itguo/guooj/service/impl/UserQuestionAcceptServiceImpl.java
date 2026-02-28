package com.itguo.guooj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itguo.guooj.mapper.UserQuestionAcceptMapper;
import com.itguo.guooj.model.entity.UserQuestionAccept;
import com.itguo.guooj.service.UserQuestionAcceptService;
import org.springframework.stereotype.Service;

/**
 * 用户题目通过记录服务实现
 */
@Service
public class UserQuestionAcceptServiceImpl extends ServiceImpl<UserQuestionAcceptMapper, UserQuestionAccept>
        implements UserQuestionAcceptService {

    @Override
    public boolean saveAcceptRecord(Long userId, Long questionId, Long submitId, String code, String language) {
        // 查询是否已存在记录
        QueryWrapper<UserQuestionAccept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        UserQuestionAccept existRecord = this.getOne(queryWrapper);

        UserQuestionAccept record = new UserQuestionAccept();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setSubmitId(submitId);
        record.setCode(code);
        record.setLanguage(language);

        if (existRecord != null) {
            // 更新记录
            record.setId(existRecord.getId());
            return this.updateById(record);
        } else {
            // 新增记录
            return this.save(record);
        }
    }

    @Override
    public UserQuestionAccept getAcceptRecord(Long userId, Long questionId) {
        QueryWrapper<UserQuestionAccept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean hasAccepted(Long userId, Long questionId) {
        QueryWrapper<UserQuestionAccept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("questionId", questionId);
        return this.count(queryWrapper) > 0;
    }
}
