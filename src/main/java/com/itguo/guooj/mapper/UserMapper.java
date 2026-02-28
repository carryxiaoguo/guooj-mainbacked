package com.itguo.guooj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itguo.guooj.model.entity.User;
import com.itguo.guooj.model.vo.UserActivityVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据库操作
 *
 * 
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 统计用户提交次数
     */
    @Select("SELECT COUNT(*) FROM question_submit WHERE userId = #{userId} AND isDelete = 0")
    int countUserSubmits(@Param("userId") Long userId);

    /**
     * 统计用户通过次数
     */
    @Select("SELECT COUNT(*) FROM question_submit WHERE userId = #{userId} AND isDelete = 0 AND judgeInfo LIKE '%\"message\":\"accepted\"%'")
    int countUserAccepts(@Param("userId") Long userId);

    /**
     * 获取用户最近的提交记录作为活动记录
     */
    @Select("SELECT qs.id, 'submit' as type, " +
            "CONCAT('提交了题目: ', q.title) as title, " +
            "CASE WHEN qs.judgeInfo LIKE '%\"message\":\"accepted\"%' THEN '通过' ELSE '未通过' END as description, " +
            "qs.createTime as time " +
            "FROM question_submit qs " +
            "LEFT JOIN question q ON qs.questionId = q.id " +
            "WHERE qs.userId = #{userId} AND qs.isDelete = 0 " +
            "ORDER BY qs.createTime DESC " +
            "LIMIT #{limit}")
    List<UserActivityVO> getUserRecentSubmits(@Param("userId") Long userId, @Param("limit") int limit);
}




