package com.ssm.questionnaire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssm.questionnaire.BaseIntegrationTest;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.dto.LoginDTO;
import com.ssm.questionnaire.dto.OptionCreateDTO;
import com.ssm.questionnaire.dto.QuestionCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireUpdateDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionnaireControllerTest extends BaseIntegrationTest {

    @Test
    public void crudFlowForOwnQuestionnaire() throws Exception {
        String token = loginAs("survey_owner");

        long id = create(token, createDTO("数据库课程问卷", Constants.QUESTIONNAIRE_PUBLISHED,
                question("你最常用哪个数据库？", "SINGLE", "MySQL", "Oracle"),
                question("你还在用哪些工具？", "MULTIPLE", "Redis", "MongoDB", "H2")));

        MvcResult detail = mockMvc.perform(get("/api/questionnaire/" + id).header(Constants.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode data = JSON.readTree(detail.getResponse().getContentAsString()).path("data");
        assertEquals("数据库课程问卷", data.path("title").asText());
        assertEquals(2, data.path("questions").size());
        assertEquals("SINGLE", data.path("questions").get(0).path("type").asText());
        assertEquals(3, data.path("questions").get(1).path("options").size());

        QuestionnaireUpdateDTO update = new QuestionnaireUpdateDTO();
        update.setId(id);
        update.setTitle("数据库课程问卷（已改名）");
        mockMvc.perform(put("/api/questionnaire/update")
                        .header(Constants.TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        JsonNode renamed = JSON.readTree(mockMvc.perform(get("/api/questionnaire/" + id)
                        .header(Constants.TOKEN_HEADER, token))
                .andReturn().getResponse().getContentAsString()).path("data");
        assertEquals("数据库课程问卷（已改名）", renamed.path("title").asText());
        assertEquals(2, renamed.path("questions").size());

        mockMvc.perform(delete("/api/questionnaire/delete/" + id).header(Constants.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/questionnaire/" + id).header(Constants.TOKEN_HEADER, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    public void listShouldHonourPagingParameters() throws Exception {
        String token = loginAs("survey_pager");
        for (int i = 1; i <= 3; i++) {
            create(token, createDTO("分页问卷 " + i, Constants.QUESTIONNAIRE_DRAFT));
        }

        MvcResult result = mockMvc.perform(get("/api/questionnaire/list")
                        .param("pageNum", "1")
                        .param("pageSize", "2")
                        .header(Constants.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode page = JSON.readTree(result.getResponse().getContentAsString()).path("data");
        assertEquals(3L, page.path("total").asLong());
        assertEquals(2, page.path("pages").asInt());
        assertEquals(2, page.path("list").size());
        assertTrue(page.path("list").get(0).path("questions").size() == 0);
    }

    @Test
    public void createWithoutTokenShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/questionnaire/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(createDTO("没有令牌", Constants.QUESTIONNAIRE_DRAFT))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    public void createWithIllegalQuestionShouldReturn400() throws Exception {
        String token = loginAs("survey_validator");
        mockMvc.perform(post("/api/questionnaire/create")
                        .header(Constants.TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(createDTO("少一个选项", Constants.QUESTIONNAIRE_PUBLISHED,
                                question("只有一个选项", "SINGLE", "A")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    public void othersQuestionnaireShouldNotAppearInMyList() throws Exception {
        String ownerToken = loginAs("survey_owner_a");
        String otherToken = loginAs("survey_owner_b");
        long id = create(ownerToken, createDTO("只属于我的问卷", Constants.QUESTIONNAIRE_DRAFT));

        MvcResult result = mockMvc.perform(get("/api/questionnaire/list").header(Constants.TOKEN_HEADER, otherToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode page = JSON.readTree(result.getResponse().getContentAsString()).path("data");
        assertEquals(0L, page.path("total").asLong());

        // 别人创建的问卷，列表看不到，改和删也应当被拒绝
        QuestionnaireUpdateDTO update = new QuestionnaireUpdateDTO();
        update.setId(id);
        update.setTitle("想改就改");
        mockMvc.perform(put("/api/questionnaire/update")
                        .header(Constants.TOKEN_HEADER, otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(update)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    private long create(String token, QuestionnaireCreateDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/questionnaire/create")
                        .header(Constants.TOKEN_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString()).path("data").asLong();
    }

    /**
     * 注册一个测试账号并登录，拿到后续请求要用的 X-Token
     */
    private String loginAs(String username) throws Exception {
        RegisterDTO register = new RegisterDTO();
        register.setUsername(username);
        register.setPassword("123456");
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(register))).andExpect(status().isOk());

        LoginDTO login = new LoginDTO();
        login.setUsername(username);
        login.setPassword("123456");
        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    private QuestionnaireCreateDTO createDTO(String title, Integer status, QuestionCreateDTO... questions) {
        QuestionnaireCreateDTO dto = new QuestionnaireCreateDTO();
        dto.setTitle(title);
        dto.setDescription("接口测试创建");
        dto.setStatus(status);
        dto.setQuestions(new ArrayList<QuestionCreateDTO>(Arrays.asList(questions)));
        return dto;
    }

    private QuestionCreateDTO question(String content, String type, String... optionContents) {
        QuestionCreateDTO question = new QuestionCreateDTO();
        question.setContent(content);
        question.setType(type);
        List<OptionCreateDTO> options = new ArrayList<OptionCreateDTO>();
        for (String optionContent : optionContents) {
            OptionCreateDTO option = new OptionCreateDTO();
            option.setContent(optionContent);
            options.add(option);
        }
        question.setOptions(options);
        return question;
    }
}
