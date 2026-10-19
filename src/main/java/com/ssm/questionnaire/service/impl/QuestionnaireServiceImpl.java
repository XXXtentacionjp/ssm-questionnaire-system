package com.ssm.questionnaire.service.impl;

import com.ssm.questionnaire.common.BizException;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.PageResult;
import com.ssm.questionnaire.common.ResultCode;
import com.ssm.questionnaire.dto.OptionCreateDTO;
import com.ssm.questionnaire.dto.PageQuery;
import com.ssm.questionnaire.dto.QuestionCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireUpdateDTO;
import com.ssm.questionnaire.entity.Option;
import com.ssm.questionnaire.entity.Question;
import com.ssm.questionnaire.entity.Questionnaire;
import com.ssm.questionnaire.entity.User;
import com.ssm.questionnaire.mapper.OptionMapper;
import com.ssm.questionnaire.mapper.QuestionMapper;
import com.ssm.questionnaire.mapper.QuestionnaireMapper;
import com.ssm.questionnaire.mapper.UserMapper;
import com.ssm.questionnaire.service.QuestionnaireService;
import com.ssm.questionnaire.vo.OptionVO;
import com.ssm.questionnaire.vo.QuestionVO;
import com.ssm.questionnaire.vo.QuestionnaireVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireServiceImpl.class);

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final int CONTENT_MAX_LENGTH = 200;
    private static final int OPTION_MAX_SIZE = 20;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private OptionMapper optionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(QuestionnaireCreateDTO dto, Long creatorId) {
        if (dto == null) {
            throw new BizException("请求体不能为空");
        }
        String title = trim(dto.getTitle());
        if (!StringUtils.hasText(title)) {
            throw new BizException("问卷标题不能为空");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new BizException("问卷标题长度不能超过 " + TITLE_MAX_LENGTH);
        }
        String description = trim(dto.getDescription());
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BizException("问卷说明长度不能超过 " + DESCRIPTION_MAX_LENGTH);
        }
        int status = resolveStatus(dto.getStatus(), Constants.QUESTIONNAIRE_DRAFT);
        List<QuestionCreateDTO> questions = dto.getQuestions();
        if (status == Constants.QUESTIONNAIRE_PUBLISHED && (questions == null || questions.isEmpty())) {
            throw new BizException("发布前请至少添加一道题目");
        }

        Date now = new Date();
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(title);
        questionnaire.setDescription(description);
        questionnaire.setCreatorId(creatorId);
        questionnaire.setStatus(status);
        questionnaire.setCreateTime(now);
        questionnaire.setUpdateTime(now);
        questionnaireMapper.insert(questionnaire);

        if (questions != null && !questions.isEmpty()) {
            saveQuestions(questionnaire.getId(), questions);
        }
        log.info("问卷创建成功, questionnaireId={}, creatorId={}", questionnaire.getId(), creatorId);
        return questionnaire.getId();
    }

    @Override
    public PageResult<QuestionnaireVO> pageMyOwn(Long creatorId, PageQuery query) {
        long total = questionnaireMapper.countByCreatorId(creatorId);
        List<QuestionnaireVO> voList;
        if (total == 0) {
            voList = Collections.emptyList();
        } else {
            List<Questionnaire> list = questionnaireMapper.selectByCreatorId(creatorId, query.getOffset(), query.getPageSize());
            User creator = userMapper.selectById(creatorId);
            String creatorName = creator == null ? null : creator.getNickname();
            voList = new ArrayList<QuestionnaireVO>(list.size());
            for (Questionnaire questionnaire : list) {
                QuestionnaireVO vo = QuestionnaireVO.from(questionnaire);
                vo.setCreatorName(creatorName);
                vo.setQuestionCount(questionMapper.countByQuestionnaireId(questionnaire.getId()));
                voList.add(vo);
            }
        }
        return new PageResult<QuestionnaireVO>(voList, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public QuestionnaireVO detail(Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new BizException(ResultCode.NOT_FOUND, "问卷不存在");
        }
        QuestionnaireVO vo = QuestionnaireVO.from(questionnaire);
        User creator = userMapper.selectById(questionnaire.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getNickname());
        }
        List<Question> questions = questionMapper.selectByQuestionnaireId(id);
        vo.setQuestionCount(questions.size());
        List<QuestionVO> questionVOList = new ArrayList<QuestionVO>(questions.size());
        for (Question question : questions) {
            QuestionVO questionVO = QuestionVO.from(question);
            List<OptionVO> optionVOList = new ArrayList<OptionVO>();
            for (Option option : optionMapper.selectByQuestionId(question.getId())) {
                optionVOList.add(OptionVO.from(option));
            }
            questionVO.setOptions(optionVOList);
            questionVOList.add(questionVO);
        }
        vo.setQuestions(questionVOList);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuestionnaireUpdateDTO dto, Long operatorId) {
        if (dto == null || dto.getId() == null) {
            throw new BizException("问卷 id 不能为空");
        }
        Questionnaire existing = questionnaireMapper.selectById(dto.getId());
        assertOwner(existing, operatorId, "无权修改他人问卷");

        String title = trim(dto.getTitle());
        if (title != null) {
            if (!StringUtils.hasText(title)) {
                throw new BizException("问卷标题不能为空");
            }
            if (title.length() > TITLE_MAX_LENGTH) {
                throw new BizException("问卷标题长度不能超过 " + TITLE_MAX_LENGTH);
            }
        }
        String description = trim(dto.getDescription());
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BizException("问卷说明长度不能超过 " + DESCRIPTION_MAX_LENGTH);
        }
        Integer status = dto.getStatus() == null ? null : resolveStatus(dto.getStatus(), Constants.QUESTIONNAIRE_DRAFT);
        List<QuestionCreateDTO> questions = dto.getQuestions();
        int finalStatus = status == null ? existing.getStatus() : status;
        if (finalStatus == Constants.QUESTIONNAIRE_PUBLISHED) {
            int questionCount = questions == null
                    ? questionMapper.countByQuestionnaireId(dto.getId())
                    : questions.size();
            if (questionCount == 0) {
                throw new BizException("发布前请至少添加一道题目");
            }
        }

        Questionnaire toUpdate = new Questionnaire();
        toUpdate.setId(dto.getId());
        toUpdate.setTitle(title);
        toUpdate.setDescription(description);
        toUpdate.setStatus(status);
        toUpdate.setUpdateTime(new Date());
        questionnaireMapper.updateById(toUpdate);

        if (questions != null) {
            optionMapper.deleteByQuestionnaireId(dto.getId());
            questionMapper.deleteByQuestionnaireId(dto.getId());
            if (!questions.isEmpty()) {
                saveQuestions(dto.getId(), questions);
            }
        }
        log.info("问卷更新成功, questionnaireId={}, operatorId={}", dto.getId(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long operatorId) {
        Questionnaire existing = questionnaireMapper.selectById(id);
        assertOwner(existing, operatorId, "无权删除他人问卷");
        optionMapper.deleteByQuestionnaireId(id);
        questionMapper.deleteByQuestionnaireId(id);
        questionnaireMapper.deleteById(id);
        log.info("问卷删除成功, questionnaireId={}, operatorId={}", id, operatorId);
    }

    private void assertOwner(Questionnaire existing, Long operatorId, String message) {
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "问卷不存在");
        }
        if (operatorId == null || !operatorId.equals(existing.getCreatorId())) {
            throw new BizException(ResultCode.FORBIDDEN, message);
        }
    }

    private int resolveStatus(Integer status, int defaultStatus) {
        if (status == null) {
            return defaultStatus;
        }
        if (status != Constants.QUESTIONNAIRE_DRAFT && status != Constants.QUESTIONNAIRE_PUBLISHED) {
            throw new BizException("问卷状态只能是 0（草稿）或 1（已发布）");
        }
        return status;
    }

    /**
     * 题目按数组顺序落库，选项同理，sort_no 缺省时用下标 + 1
     */
    private void saveQuestions(Long questionnaireId, List<QuestionCreateDTO> questions) {
        Date now = new Date();
        for (int i = 0; i < questions.size(); i++) {
            QuestionCreateDTO item = questions.get(i);
            if (item == null) {
                throw new BizException("第 " + (i + 1) + " 道题目为空");
            }
            String content = trim(item.getContent());
            if (!StringUtils.hasText(content)) {
                throw new BizException("第 " + (i + 1) + " 道题目内容不能为空");
            }
            if (content.length() > CONTENT_MAX_LENGTH) {
                throw new BizException("第 " + (i + 1) + " 道题目内容长度不能超过 " + CONTENT_MAX_LENGTH);
            }
            String type = normalizeType(item.getType());
            List<OptionCreateDTO> options = item.getOptions();
            if (options == null || options.size() < 2) {
                throw new BizException("第 " + (i + 1) + " 道题目至少需要 2 个选项");
            }
            if (options.size() > OPTION_MAX_SIZE) {
                throw new BizException("第 " + (i + 1) + " 道题目选项数不能超过 " + OPTION_MAX_SIZE);
            }

            Question question = new Question();
            question.setQuestionnaireId(questionnaireId);
            question.setContent(content);
            question.setType(type);
            question.setSortNo(item.getSortNo() == null ? i + 1 : item.getSortNo());
            question.setCreateTime(now);
            questionMapper.insert(question);

            for (int j = 0; j < options.size(); j++) {
                OptionCreateDTO optionDTO = options.get(j);
                if (optionDTO == null || !StringUtils.hasText(trim(optionDTO.getContent()))) {
                    throw new BizException("第 " + (i + 1) + " 道题目第 " + (j + 1) + " 个选项内容不能为空");
                }
                String optionContent = trim(optionDTO.getContent());
                if (optionContent.length() > CONTENT_MAX_LENGTH) {
                    throw new BizException("选项内容长度不能超过 " + CONTENT_MAX_LENGTH);
                }
                Option option = new Option();
                option.setQuestionId(question.getId());
                option.setContent(optionContent);
                option.setSortNo(optionDTO.getSortNo() == null ? j + 1 : optionDTO.getSortNo());
                optionMapper.insert(option);
            }
        }
    }

    private String normalizeType(String type) {
        String raw = trim(type);
        String normalized = raw == null ? null : raw.toUpperCase();
        if (!Constants.QUESTION_SINGLE.equals(normalized) && !Constants.QUESTION_MULTIPLE.equals(normalized)) {
            throw new BizException("题目类型只能是 SINGLE 或 MULTIPLE");
        }
        return normalized;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
