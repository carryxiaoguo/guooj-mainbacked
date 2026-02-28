-- 用户题目通过记录表
CREATE TABLE IF NOT EXISTS user_question_accept
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userId      BIGINT                             NOT NULL COMMENT '用户id',
    questionId  BIGINT                             NOT NULL COMMENT '题目id',
    submitId    BIGINT                             NOT NULL COMMENT '提交记录id',
    code        TEXT                               NULL COMMENT '通过的代码',
    language    VARCHAR(128)                       NULL COMMENT '编程语言',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_userId (userId),
    INDEX idx_questionId (questionId),
    UNIQUE KEY uk_user_question (userId, questionId) COMMENT '用户-题目唯一索引'
) COMMENT '用户题目通过记录表' COLLATE = utf8mb4_unicode_ci;
