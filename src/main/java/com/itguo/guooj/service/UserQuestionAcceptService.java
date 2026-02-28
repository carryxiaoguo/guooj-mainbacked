package com.itguo.guooj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itguo.guooj.model.entity.UserQuestionAccept;

/**
 * 用户题目通过记录服务
 */
public interface UserQuestionAcceptService extends IService<UserQuestionAccept> {

    /**
     * 保存用户通过记录
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param submitId 提交记录ID
     * @param code 通过的代码
     * @param language 编程语言
     * @return 是否保存成功
     */
    boolean saveAcceptRecord(Long userId, Long questionId, Long submitId, String code, String language);

    /**
     * 获取用户通过记录
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 通过记录
     */
    UserQuestionAccept getAcceptRecord(Long userId, Long questionId);

    /**
     * 检查用户是否通过题目
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否通过
     */
    boolean hasAccepted(Long userId, Long questionId);
}
