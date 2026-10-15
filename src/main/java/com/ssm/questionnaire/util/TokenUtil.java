package com.ssm.questionnaire.util;

import java.util.UUID;

/**
 * 令牌生成，随机 UUID 去掉横线
 */
public final class TokenUtil {

    private TokenUtil() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
