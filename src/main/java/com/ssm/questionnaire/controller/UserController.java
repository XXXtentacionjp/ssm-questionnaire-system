package com.ssm.questionnaire.controller;

import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.Result;
import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import com.ssm.questionnaire.service.UserService;
import com.ssm.questionnaire.vo.LoginVO;
import com.ssm.questionnaire.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册：POST /api/user/register
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    /**
     * 登录：POST /api/user/login，返回 token，后续请求放到 X-Token 头
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 当前登录用户信息：GET /api/user/info
     */
    @GetMapping("/info")
    public Result<UserVO> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.CURRENT_USER_ID);
        return Result.success(userService.getUserInfo(userId));
    }
}
