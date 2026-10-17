package com.ssm.questionnaire.common;

/**
 * 全局常量
 */
public final class Constants {

    /** 前端登录后需在请求头携带该字段 */
    public static final String TOKEN_HEADER = "X-Token";

    /** 拦截器解析出的当前用户 id 存放位置 */
    public static final String CURRENT_USER_ID = "currentUserId";

    /** 令牌有效期（分钟） */
    public static final int TOKEN_EXPIRE_MINUTES = 120;

    public static final String ROLE_USER = "ROLE_USER";

    /** 问卷状态 */
    public static final int QUESTIONNAIRE_DRAFT = 0;
    public static final int QUESTIONNAIRE_PUBLISHED = 1;

    /** 题目类型 */
    public static final String QUESTION_SINGLE = "SINGLE";
    public static final String QUESTION_MULTIPLE = "MULTIPLE";

    public static final int USER_STATUS_NORMAL = 1;

    private Constants() {
    }
}
