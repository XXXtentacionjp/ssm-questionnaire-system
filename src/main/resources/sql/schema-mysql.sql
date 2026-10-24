-- =============================================================
-- 问卷系统初始化脚本（MySQL 5.7 / 8.0）
-- 执行方式：mysql -uroot -p < schema-mysql.sql
-- 注意：测试用的 H2 建表脚本在 src/test/resources/schema-h2.sql，
--       两边表结构改动必须同步，否则 mvn test 会失败。
-- =============================================================

DROP DATABASE IF EXISTS questionnaire_db;
CREATE DATABASE questionnaire_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE questionnaire_db;

-- 用户表
CREATE TABLE t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录名，唯一',
    password    VARCHAR(64)  NOT NULL COMMENT 'MD5(明文+固定盐)',
    nickname    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    email       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    phone       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '用户表';

-- 登录令牌表
CREATE TABLE t_user_token (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT      NOT NULL COMMENT '所属用户',
    token       VARCHAR(64) NOT NULL COMMENT '令牌',
    expire_time DATETIME    NOT NULL COMMENT '过期时间',
    create_time DATETIME    NOT NULL COMMENT '签发时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_token_user_id (user_id),
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '登录令牌表';

-- 问卷表
CREATE TABLE t_questionnaire (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    description VARCHAR(500)          DEFAULT NULL COMMENT '说明',
    creator_id  BIGINT       NOT NULL COMMENT '创建者',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 草稿 1 已发布',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_creator_id (creator_id),
    CONSTRAINT fk_questionnaire_creator FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '问卷表';

-- 题目表
CREATE TABLE t_question (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    questionnaire_id BIGINT       NOT NULL COMMENT '所属问卷',
    content          VARCHAR(200) NOT NULL COMMENT '题干',
    type             VARCHAR(10)  NOT NULL COMMENT 'SINGLE 单选 / MULTIPLE 多选',
    sort_no          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time      DATETIME     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_question_questionnaire (questionnaire_id),
    CONSTRAINT fk_question_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES t_questionnaire (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '题目表';

-- 选项表
CREATE TABLE t_option (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    question_id BIGINT      NOT NULL COMMENT '所属题目',
    content    VARCHAR(200) NOT NULL COMMENT '选项内容',
    sort_no    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (id),
    KEY idx_option_question (question_id),
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES t_question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '选项表';

-- 答卷表（表结构按基础需求设计，提交答卷的接口在中等需求里，本轮还没实现）
CREATE TABLE t_answer (
    id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    questionnaire_id BIGINT   NOT NULL COMMENT '问卷',
    user_id          BIGINT   NOT NULL COMMENT '答卷人',
    submit_time      DATETIME NOT NULL COMMENT '提交时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_user (questionnaire_id, user_id),
    CONSTRAINT fk_answer_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES t_questionnaire (id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '答卷表';

-- 答卷明细表，一行一个被选中的选项
CREATE TABLE t_answer_detail (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    answer_id   BIGINT NOT NULL COMMENT '所属答卷',
    question_id BIGINT NOT NULL COMMENT '题目',
    option_id   BIGINT NOT NULL COMMENT '选项',
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_option (answer_id, option_id),
    CONSTRAINT fk_detail_answer FOREIGN KEY (answer_id) REFERENCES t_answer (id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_question FOREIGN KEY (question_id) REFERENCES t_question (id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_option FOREIGN KEY (option_id) REFERENCES t_option (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '答卷明细表';

-- =============================================================
-- 演示数据：账号 demo / 123456
-- password = MD5('123456' + 'questionnaire#2025')
--          = 8b56356d2022ae650980ffcec7d82133
-- =============================================================
INSERT INTO t_user (id, username, password, nickname, email, role, status, create_time, update_time)
VALUES (1, 'demo', '8b56356d2022ae650980ffcec7d82133', '演示账号', 'demo@example.com', 'ROLE_USER', 1,
        '2025-04-10 09:00:00', '2025-04-10 09:00:00');

INSERT INTO t_questionnaire (id, title, description, creator_id, status, create_time, update_time)
VALUES (1001, 'Java 学习情况调查', '了解大家在学 Java 过程中的常见困难', 1, 1,
        '2025-04-10 09:30:00', '2025-04-10 09:30:00');

INSERT INTO t_question (id, questionnaire_id, content, type, sort_no, create_time)
VALUES (1, 1001, '你觉得最难的部分是什么？', 'SINGLE', 1, '2025-04-10 09:30:00'),
       (2, 1001, '你平时通过哪些方式学习？', 'MULTIPLE', 2, '2025-04-10 09:30:00');

INSERT INTO t_option (id, question_id, content, sort_no)
VALUES (1, 1, '集合框架', 1),
       (2, 1, '多线程', 2),
       (3, 1, 'SSM 框架', 3),
       (4, 2, '看视频', 1),
       (5, 2, '刷算法题', 2),
       (6, 2, '读源码', 3);
