package com.ssm.questionnaire.service;

import com.ssm.questionnaire.BaseIntegrationTest;
import com.ssm.questionnaire.common.BizException;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.ResultCode;
import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import com.ssm.questionnaire.vo.LoginVO;
import com.ssm.questionnaire.vo.UserVO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class UserServiceTest extends BaseIntegrationTest {

    private static final String RAW_PASSWORD = "123456";

    @Autowired
    private UserService userService;

    @Test
    public void registerShouldPersistUserAndReturnVoWithoutPassword() {
        UserVO vo = userService.register(registerDTO("alice", RAW_PASSWORD));
        assertNotNull(vo.getId());
        assertEquals("alice", vo.getUsername());
        assertEquals("alice", vo.getNickname());
        assertEquals(Constants.ROLE_USER, vo.getRole());
    }

    @Test
    public void registerShouldRejectDuplicatedUsername() {
        userService.register(registerDTO("bob", RAW_PASSWORD));
        try {
            userService.register(registerDTO("bob", "654321"));
            fail("重复用户名应当被拒绝");
        } catch (BizException e) {
            assertEquals(ResultCode.PARAM_ERROR, e.getCode());
            assertEquals("用户名已存在", e.getMessage());
        }
    }

    @Test
    public void registerShouldRejectShortPassword() {
        try {
            userService.register(registerDTO("carol", "123"));
            fail("弱口令应当被拒绝");
        } catch (BizException e) {
            assertEquals(ResultCode.PARAM_ERROR, e.getCode());
        }
    }

    @Test
    public void registerShouldRejectIllegalUsernameCharacters() {
        try {
            userService.register(registerDTO("bad name!", RAW_PASSWORD));
            fail("非法用户名应当被拒绝");
        } catch (BizException e) {
            assertEquals(ResultCode.PARAM_ERROR, e.getCode());
        }
    }

    @Test
    public void loginShouldIssueUsableToken() {
        userService.register(registerDTO("dave", RAW_PASSWORD));
        LoginVO loginVO = userService.login(loginDTO("dave", RAW_PASSWORD));
        assertNotNull(loginVO.getToken());
        assertEquals(Long.valueOf(7200L), loginVO.getExpiresIn());
        assertEquals("dave", loginVO.getUser().getUsername());
        assertEquals(Long.valueOf(loginVO.getUser().getId()), userService.resolveUserId(loginVO.getToken()));
    }

    @Test
    public void loginShouldRejectWrongPassword() {
        userService.register(registerDTO("erin", RAW_PASSWORD));
        try {
            userService.login(loginDTO("erin", "wrong-password"));
            fail("口令错误应当返回 401");
        } catch (BizException e) {
            assertEquals(ResultCode.UNAUTHORIZED, e.getCode());
        }
    }

    @Test
    public void resolveUserIdShouldReturnNullForUnknownOrBlankToken() {
        assertNull(userService.resolveUserId("not-a-real-token"));
        assertNull(userService.resolveUserId(""));
        assertNull(userService.resolveUserId(null));
    }

    @Test
    public void getUserInfoShouldRejectMissingUser() {
        try {
            userService.getUserInfo(424242L);
            fail("用户不存在应当返回 404");
        } catch (BizException e) {
            assertEquals(ResultCode.NOT_FOUND, e.getCode());
        }
    }

    private RegisterDTO registerDTO(String username, String password) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private LoginDTO loginDTO(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
}
