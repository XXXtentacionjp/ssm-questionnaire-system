package com.ssm.questionnaire.vo;

/**
 * 登录成功返回的令牌与用户信息
 */
public class LoginVO {

    private String token;
    /** 令牌剩余有效期，秒 */
    private Long expiresIn;
    private UserVO user;

    public LoginVO() {
    }

    public LoginVO(String token, Long expiresIn, UserVO user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
