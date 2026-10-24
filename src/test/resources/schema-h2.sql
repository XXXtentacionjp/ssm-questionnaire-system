-- =============================================================
-- 单元测试用建表脚本（H2 MySQL 兼容模式）
-- 与 src/main/resources/sql/schema-mysql.sql 表结构保持一致，
-- 只去掉了 MySQL 专有的 ENGINE / COMMENT / CHARSET 语法。
-- =============================================================

DROP TABLE IF EXISTS t_answer_detail;
DROP TABLE IF EXISTS t_answer;
DROP TABLE IF EXISTS t_option;
DROP TABLE IF EXISTS t_question;
DROP TABLE IF EXISTS t_questionnaire;
DROP TABLE IF EXISTS t_user_token;
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(64)  NOT NULL,
    nickname    VARCHAR(50)           DEFAULT NULL,
    email       VARCHAR(100)          DEFAULT NULL,
    phone       VARCHAR(20)           DEFAULT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username)
);

CREATE TABLE t_user_token (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    token       VARCHAR(64) NOT NULL,
    expire_time DATETIME    NOT NULL,
    create_time DATETIME    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_token UNIQUE (token),
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
);

CREATE TABLE t_questionnaire (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500)          DEFAULT NULL,
    creator_id  BIGINT       NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_questionnaire_creator FOREIGN KEY (creator_id) REFERENCES t_user (id) ON DELETE CASCADE
);

CREATE TABLE t_question (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    questionnaire_id BIGINT       NOT NULL,
    content          VARCHAR(200) NOT NULL,
    type             VARCHAR(10)  NOT NULL,
    sort_no          INT          NOT NULL DEFAULT 0,
    create_time      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES t_questionnaire (id) ON DELETE CASCADE
);

CREATE TABLE t_option (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    question_id BIGINT       NOT NULL,
    content     VARCHAR(200) NOT NULL,
    sort_no     INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES t_question (id) ON DELETE CASCADE
);

CREATE TABLE t_answer (
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    questionnaire_id BIGINT   NOT NULL,
    user_id          BIGINT   NOT NULL,
    submit_time      DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_answer_user UNIQUE (questionnaire_id, user_id),
    CONSTRAINT fk_answer_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES t_questionnaire (id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
);

CREATE TABLE t_answer_detail (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    answer_id   BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id   BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_answer_option UNIQUE (answer_id, option_id),
    CONSTRAINT fk_detail_answer FOREIGN KEY (answer_id) REFERENCES t_answer (id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_question FOREIGN KEY (question_id) REFERENCES t_question (id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_option FOREIGN KEY (option_id) REFERENCES t_option (id) ON DELETE CASCADE
);
