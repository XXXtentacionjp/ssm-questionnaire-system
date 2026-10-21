package com.ssm.questionnaire.service;

import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import com.ssm.questionnaire.vo.LoginVO;
import com.ssm.questionnaire.vo.UserVO;

public interface UserService {

    /**
     * 注册，用户名重复时抛业务异常
     */
    UserVO register(RegisterDTO dto);

    /**
     * 登录成功签发令牌
     */
    LoginVO login(LoginDTO dto);

    /**
     * 校验令牌，通过返回用户 id，否则返回 null
     */
    Long resolveUserId(String token);

    /**
     * 取用户信息，用户不存在时抛业务异常
     */
    UserVO getUserInfo(Long userId);
}
