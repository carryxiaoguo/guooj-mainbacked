# 添加题解和评论功能的数据库表

use guooj;

-- 题解表
create table if not exists question_solution
(
    id          bigint auto_increment comment 'id' primary key,
    questionId  bigint                             not null comment '题目 id',
    userId      bigint                             not null comment '创建用户 id',
    title       varchar(512)                       not null comment '题解标题',
    content     text                               not null comment '题解内容',
    language    varchar(128)                       null comment '代码语言',
    code        text                               null comment '题解代码',
    likeNum     int      default 0                 not null comment '点赞数',
    viewNum     int      default 0                 not null comment '浏览数',
    isOfficial  tinyint  default 0                 not null comment '是否官方题解',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题解表' collate = utf8mb4_unicode_ci;

-- 评论表
create table if not exists question_comment
(
    id          bigint auto_increment comment 'id' primary key,
    questionId  bigint                             not null comment '题目 id',
    userId      bigint                             not null comment '评论用户 id',
    content     text                               not null comment '评论内容',
    parentId    bigint   default 0                 null comment '父评论 id（0表示顶级评论）',
    replyToId   bigint   default 0                 null comment '回复的用户 id',
    likeNum     int      default 0                 not null comment '点赞数',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId),
    index idx_parentId (parentId)
) comment '评论表' collate = utf8mb4_unicode_ci;

