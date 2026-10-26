package com.ssm.questionnaire.service;

import com.ssm.questionnaire.BaseIntegrationTest;
import com.ssm.questionnaire.common.BizException;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.PageResult;
import com.ssm.questionnaire.common.ResultCode;
import com.ssm.questionnaire.dto.OptionCreateDTO;
import com.ssm.questionnaire.dto.PageQuery;
import com.ssm.questionnaire.dto.QuestionCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireUpdateDTO;
import com.ssm.questionnaire.dto.RegisterDTO;
import com.ssm.questionnaire.mapper.QuestionMapper;
import com.ssm.questionnaire.mapper.QuestionnaireMapper;
import com.ssm.questionnaire.vo.QuestionVO;
import com.ssm.questionnaire.vo.QuestionnaireVO;
import com.ssm.questionnaire.vo.UserVO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class QuestionnaireServiceTest extends BaseIntegrationTest {

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Test
    public void createShouldPersistQuestionsAndOptions() {
        Long creatorId = createUser("creator_a");
        QuestionnaireCreateDTO dto = createDTO("Java 学习情况调查", Constants.QUESTIONNAIRE_PUBLISHED,
                question("最难的部分是什么？", "SINGLE", "集合框架", "多线程"),
                question("学习方式有哪些？", "MULTIPLE", "看视频", "刷题", "读源码"));

        Long id = questionnaireService.create(dto, creatorId);
        assertNotNull(id);

        QuestionnaireVO detail = questionnaireService.detail(id);
        assertEquals("Java 学习情况调查", detail.getTitle());
        assertEquals("creator_a", detail.getCreatorName());
        assertEquals(Integer.valueOf(2), detail.getQuestionCount());
        assertEquals(2, detail.getQuestions().size());

        QuestionVO second = detail.getQuestions().get(1);
        assertEquals("MULTIPLE", second.getType());
        assertEquals(Integer.valueOf(2), second.getSortNo());
        assertEquals(3, second.getOptions().size());
        assertEquals("看视频", second.getOptions().get(0).getContent());
    }

    @Test
    public void createShouldAcceptDraftWithoutQuestions() {
        Long creatorId = createUser("creator_b");
        Long id = questionnaireService.create(createDTO("还没想好", Constants.QUESTIONNAIRE_DRAFT), creatorId);
        assertEquals(Integer.valueOf(0), questionnaireService.detail(id).getQuestionCount());
    }

    @Test
    public void createShouldRejectPublishedQuestionnaireWithoutQuestions() {
        Long creatorId = createUser("creator_c");
        try {
            questionnaireService.create(createDTO("空问卷直接发布", Constants.QUESTIONNAIRE_PUBLISHED), creatorId);
            fail("没有题目的问卷不应该能直接发布");
        } catch (BizException e) {
            assertEquals(ResultCode.PARAM_ERROR, e.getCode());
        }
    }

    @Test
    public void createShouldRejectQuestionWithLessThanTwoOptions() {
        Long creatorId = createUser("creator_d");
        try {
            questionnaireService.create(createDTO("选项太少", Constants.QUESTIONNAIRE_PUBLISHED,
                    question("只有一个选项", "SINGLE", "就一个")), creatorId);
            fail("选项不足两个的题目应该被拒绝");
        } catch (BizException e) {
            assertEquals("第 1 道题目至少需要 2 个选项", e.getMessage());
        }
    }

    @Test
    public void createShouldRejectUnknownQuestionType() {
        Long creatorId = createUser("creator_e");
        try {
            questionnaireService.create(createDTO("类型写错", Constants.QUESTIONNAIRE_PUBLISHED,
                    question("这是什么类型", "JUDGE", "对", "错")), creatorId);
            fail("非法题目类型应该被拒绝");
        } catch (BizException e) {
            assertEquals("题目类型只能是 SINGLE 或 MULTIPLE", e.getMessage());
        }
    }

    @Test
    public void pageMyOwnShouldPageByCreator() {
        Long creatorId = createUser("pager");
        for (int i = 1; i <= 12; i++) {
            questionnaireService.create(createDTO("第 " + i + " 份问卷", Constants.QUESTIONNAIRE_DRAFT), creatorId);
        }
        PageQuery secondPage = new PageQuery();
        secondPage.setPageNum(2);
        secondPage.setPageSize(5);

        PageResult<QuestionnaireVO> result = questionnaireService.pageMyOwn(creatorId, secondPage);
        assertEquals(12L, result.getTotal());
        assertEquals(3, result.getPages());
        assertEquals(5, result.getList().size());
        assertEquals(2, result.getPageNum());
    }

    @Test
    public void pageMyOwnShouldNotReturnOtherUsersQuestionnaires() {
        Long owner = createUser("owner");
        Long stranger = createUser("stranger");
        Long id = questionnaireService.create(createDTO("我的私有问卷", Constants.QUESTIONNAIRE_DRAFT), owner);

        PageQuery query = new PageQuery();
        assertEquals(0L, questionnaireService.pageMyOwn(stranger, query).getTotal());
        assertEquals(1L, questionnaireService.pageMyOwn(owner, query).getTotal());
        assertEquals(id, questionnaireService.pageMyOwn(owner, query).getList().get(0).getId());
    }

    @Test
    public void updateWithoutQuestionsShouldKeepExistingOnes() {
        Long creatorId = createUser("updater_a");
        Long id = questionnaireService.create(createDTO("改标题", Constants.QUESTIONNAIRE_PUBLISHED,
                question("题目一", "SINGLE", "A", "B")), creatorId);

        QuestionnaireUpdateDTO dto = new QuestionnaireUpdateDTO();
        dto.setId(id);
        dto.setTitle("改过的标题");
        questionnaireService.update(dto, creatorId);

        QuestionnaireVO detail = questionnaireService.detail(id);
        assertEquals("改过的标题", detail.getTitle());
        assertEquals(1, detail.getQuestions().size());
    }

    @Test
    public void updateShouldReplaceWholeQuestionList() {
        Long creatorId = createUser("updater_b");
        Long id = questionnaireService.create(createDTO("换题目", Constants.QUESTIONNAIRE_PUBLISHED,
                question("旧题目一", "SINGLE", "A", "B"),
                question("旧题目二", "SINGLE", "C", "D")), creatorId);

        QuestionnaireUpdateDTO dto = new QuestionnaireUpdateDTO();
        dto.setId(id);
        dto.setQuestions(Arrays.asList(question("新题目", "MULTIPLE", "X", "Y", "Z")));
        questionnaireService.update(dto, creatorId);

        QuestionnaireVO detail = questionnaireService.detail(id);
        assertEquals(1, detail.getQuestions().size());
        assertEquals("新题目", detail.getQuestions().get(0).getContent());
        assertEquals(3, detail.getQuestions().get(0).getOptions().size());
    }

    @Test
    public void updateShouldRejectOtherUsersQuestionnaire() {
        Long owner = createUser("owner_b");
        Long stranger = createUser("stranger_b");
        Long id = questionnaireService.create(createDTO("别人的问卷", Constants.QUESTIONNAIRE_DRAFT), owner);

        QuestionnaireUpdateDTO dto = new QuestionnaireUpdateDTO();
        dto.setId(id);
        dto.setTitle("想改就改");
        try {
            questionnaireService.update(dto, stranger);
            fail("非创建者不应能修改问卷");
        } catch (BizException e) {
            assertEquals(ResultCode.FORBIDDEN, e.getCode());
        }
    }

    @Test
    public void deleteShouldCascadeToQuestionsAndOptions() {
        Long creatorId = createUser("deleter");
        Long id = questionnaireService.create(createDTO("要删掉", Constants.QUESTIONNAIRE_PUBLISHED,
                question("题目", "SINGLE", "A", "B")), creatorId);
        assertEquals(1, questionMapper.countByQuestionnaireId(id));

        questionnaireService.delete(id, creatorId);

        assertNull(questionnaireMapper.selectById(id));
        assertEquals(0, questionMapper.countByQuestionnaireId(id));
    }

    @Test
    public void detailShouldRejectMissingQuestionnaire() {
        try {
            questionnaireService.detail(888888L);
            fail("问卷不存在应该返回 404");
        } catch (BizException e) {
            assertEquals(ResultCode.NOT_FOUND, e.getCode());
        }
    }

    private Long createUser(String username) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("123456");
        return userService.register(dto).getId();
    }

    private QuestionnaireCreateDTO createDTO(String title, Integer status, QuestionCreateDTO... questions) {
        QuestionnaireCreateDTO dto = new QuestionnaireCreateDTO();
        dto.setTitle(title);
        dto.setDescription("由单元测试创建");
        dto.setStatus(status);
        List<QuestionCreateDTO> list = new ArrayList<QuestionCreateDTO>(Arrays.asList(questions));
        dto.setQuestions(list);
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
