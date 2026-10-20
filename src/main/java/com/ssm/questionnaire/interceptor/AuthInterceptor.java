package com.ssm.questionnaire.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.Result;
import com.ssm.questionnaire.common.ResultCode;
import com.ssm.questionnaire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 令牌校验拦截器，注册与登录接口在 spring-mvc.xml 里已排除。
 * 校验通过后把用户 id 放进 request attribute，控制器从这里取当前登录用户。
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(Constants.TOKEN_HEADER);
        Long userId = userService.resolveUserId(token);
        if (userId == null) {
            writeUnauthorized(response);
            return false;
        }
        request.setAttribute(Constants.CURRENT_USER_ID, userId);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        objectMapper.writeValue(response.getWriter(), result);
    }
}
