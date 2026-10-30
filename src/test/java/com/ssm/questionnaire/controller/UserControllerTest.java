package com.ssm.questionnaire.controller;

import com.ssm.questionnaire.BaseIntegrationTest;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends BaseIntegrationTest {

    @Test
    public void registerLoginThenReadOwnInfo() throws Exception {
        RegisterDTO register = new RegisterDTO();
        register.setUsername("grace");
        register.setPassword("123456");
        register.setNickname("格蕾丝");
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("grace"))
                .andExpect(jsonPath("$.data.role").value(Constants.ROLE_USER));

        MvcResult loginResult = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(loginDTO("grace", "123456"))))
                .andExpect(status().isOk())
                .andReturn();

        String token = JSON.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
        assertNotNull(token);

        mockMvc.perform(get("/api/user/info").header(Constants.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("grace"))
                .andExpect(jsonPath("$.data.nickname").value("格蕾丝"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    public void userInfoWithoutTokenShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    public void loginWithWrongPasswordShouldReturn401() throws Exception {
        RegisterDTO register = new RegisterDTO();
        register.setUsername("henry");
        register.setPassword("123456");
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(register))).andExpect(status().isOk());

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(loginDTO("henry", "bad-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    public void duplicatedRegisterShouldReturn400() throws Exception {
        RegisterDTO register = new RegisterDTO();
        register.setUsername("ivan");
        register.setPassword("123456");
        String body = JSON.writeValueAsString(register);
        mockMvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    private LoginDTO loginDTO(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
}
