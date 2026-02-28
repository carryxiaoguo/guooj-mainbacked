package com.itguo.guooj.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itguo.guooj.common.ErrorCode;
import com.itguo.guooj.exception.BusinessException;
import com.itguo.guooj.model.dto.question.JudgeConfig;
import com.itguo.guooj.judge.codesandbox.model.JudgeInfo;
import com.itguo.guooj.model.entity.QuestionSubmit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 题目
 *
 * @TableName question
 */
@TableName(value = "questionSubmit")
@Data
@Slf4j
public class QuestionSubmitVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 代码
     */
    private String code;
    /**
     * 创建用户人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO) {
        if (questionSubmitVO == null) {
            return null;
        }
        try {
            QuestionSubmit questionSubmit = new QuestionSubmit();
            BeanUtils.copyProperties(questionSubmitVO, questionSubmit);

            // 安全处理 judgeInfo
            JudgeConfig judgeConfig = questionSubmitVO.getJudgeConfig();
            if (judgeConfig != null) {
                try {
                    questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeConfig));
                } catch (Exception e) {
                    log.error("Failed to convert JudgeConfig to JSON", e);
                    questionSubmit.setJudgeInfo("{}"); // 设置空JSON对象作为默认值
                }
            } else {
                questionSubmit.setJudgeInfo("{}"); // 处理null情况
            }

            return questionSubmit;
        } catch (Exception e) {
            log.error("QuestionSubmitVO to QuestionSubmit conversion failed", e);
            return null;
        }
    }

    /**
     * 对象转包装类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVo(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        String judgeInfoStr = questionSubmit.getJudgeInfo();
        if (judgeInfoStr.isEmpty()) {
           throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"数据不存在");
        }
        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(judgeInfoStr, JudgeInfo.class));
       return questionSubmitVO;
        }
    }
