package com.ssm.questionnaire.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 口令摘要工具。
 * 目前是 MD5 + 全局固定盐，强度不够，后续要换成 BCrypt 之类的慢哈希。
 */
public final class PasswordUtil {

    private static final String SALT = "questionnaire#2025";

    private PasswordUtil() {
    }

    public static String encrypt(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        String source = rawPassword + SALT;
        return DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean matches(String rawPassword, String encrypted) {
        return encrypted != null && encrypted.equalsIgnoreCase(encrypt(rawPassword));
    }
}
