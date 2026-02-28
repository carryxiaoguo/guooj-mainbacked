package com.itguo.guooj.model.vo;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.itguo.guooj.model.dto.question.JudgeCase;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.model.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 题目
 *
 * @TableName question
 */
@TableName(value = "question")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class QuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 创建用户 id
     */
    private Long userId;
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 提交数
     */
    private Integer submitNum;

    /**
     * 通过数
     */
    private Integer acceptNum;

    /**
     * 判题用例
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 创建用户人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        try {
            Question question = new Question();
            BeanUtils.copyProperties(questionVO, question);

            // 安全处理 tags
            List<String> tagList = questionVO.getTags();
            if (CollectionUtils.isNotEmpty(tagList)) {
                question.setTags(JSONUtil.toJsonStr(tagList));
            } else {
                question.setTags(null);
            }

            // 安全处理 judgeConfig
            JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
            if (voJudgeConfig != null) {
                question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
            } else {
                question.setJudgeConfig(null);
            }

            // 安全处理 judgeCase
            List<JudgeCase> voJudgeCase = questionVO.getJudgeCase();
            if (CollectionUtils.isNotEmpty(voJudgeCase)) {
                question.setJudgeCase(JSONUtil.toJsonStr(voJudgeCase));
            } else {
                question.setJudgeCase(null);
            }

            return question;
        } catch (Exception e) {
            log.error("QuestionVO to Question conversion failed", e);
            return null;
        }
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        try {
            QuestionVO questionVO = new QuestionVO();
            BeanUtils.copyProperties(question, questionVO);

            // 安全处理 tags
            if (StringUtils.isNotBlank(question.getTags())) {
                try {
                    questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
                } catch (Exception e) {
                    log.error("解析Tags失败: {}", question.getTags(), e);
                    questionVO.setTags(new ArrayList<>());
                }
            } else {
                questionVO.setTags(new ArrayList<>());
            }

            // 安全处理 judgeConfig
            if (StringUtils.isNotBlank(question.getJudgeConfig())) {
                try {
                   questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(),JudgeConfig.class));
                } catch (Exception e) {
                    log.error("解析JudgeConfig JSON失败: {}", question.getJudgeConfig(), e);
                    questionVO.setJudgeConfig(new JudgeConfig());
                }
            } else {
                questionVO.setJudgeConfig(new JudgeConfig());
            }

            // 安全处理 judgeCase
            if (StringUtils.isNotBlank(question.getJudgeCase())) {
                try {
                    questionVO.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
                } catch (Exception e) {
                    log.error("解析JudgeCase JSON失败: {}", question.getJudgeCase(), e);
                    questionVO.setJudgeCase(new ArrayList<>());
                }
            } else {
                questionVO.setJudgeCase(new ArrayList<>());
            }

            return questionVO;
        } catch (Exception e) {
            log.error("Question 对象转换为 QuestionVO 失败", e);
            return null;
        }
    }
}