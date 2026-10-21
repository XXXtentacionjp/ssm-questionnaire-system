package com.ssm.questionnaire.service.impl;

import com.ssm.questionnaire.common.BizException;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.ResultCode;
import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import com.ssm.questionnaire.entity.User;
import com.ssm.questionnaire.entity.UserToken;
import com.ssm.questionnaire.mapper.UserMapper;
import com.ssm.questionnaire.mapper.UserTokenMapper;
import com.ssm.questionnaire.service.UserService;
import com.ssm.questionnaire.util.PasswordUtil;
import com.ssm.questionnaire.util.TokenUtil;
import com.ssm.questionnaire.vo.LoginVO;
import com.ssm.questionnaire.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        if (dto == null) {
            throw new BizException("请求体不能为空");
        }
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        String password = dto.getPassword();
        if (!StringUtils.hasText(username)) {
            throw new BizException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 20) {
            throw new BizException("用户名长度需要在 3 到 20 之间");
        }
        if (!username.matches("[A-Za-z0-9_]+")) {
            throw new BizException("用户名只能包含字母、数字和下划线");
        }
        if (!StringUtils.hasText(password)) {
            throw new BizException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new BizException("密码长度需要在 6 到 32 之间");
        }
        if (userMapper.selectByUsername(username) != null) {
            throw new BizException("用户名已存在");
        }

        Date now = new Date();
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : username);
        user.setEmail(trimToNull(dto.getEmail()));
        user.setPhone(trimToNull(dto.getPhone()));
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        log.info("用户注册成功, userId={}, username={}", user.getId(), username);
        return UserVO.from(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BizException(ResultCode.PARAM_ERROR, "用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(dto.getUsername().trim());
        if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != Constants.USER_STATUS_NORMAL) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        Date now = new Date();
        Date expireTime = new Date(now.getTime() + Constants.TOKEN_EXPIRE_MINUTES * 60L * 1000L);
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setToken(TokenUtil.generate());
        userToken.setExpireTime(expireTime);
        userToken.setCreateTime(now);
        userTokenMapper.insert(userToken);

        long expiresIn = (expireTime.getTime() - now.getTime()) / 1000L;
        log.info("用户登录成功, userId={}", user.getId());
        return new LoginVO(userToken.getToken(), expiresIn, UserVO.from(user));
    }

    @Override
    public Long resolveUserId(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        UserToken userToken = userTokenMapper.selectByToken(token.trim());
        if (userToken == null) {
            return null;
        }
        if (userToken.getExpireTime() == null || userToken.getExpireTime().before(new Date())) {
            return null;
        }
        return userToken.getUserId();
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return UserVO.from(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
