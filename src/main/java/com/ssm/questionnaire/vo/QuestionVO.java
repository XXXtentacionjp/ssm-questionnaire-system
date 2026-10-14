package com.ssm.questionnaire.vo;

import com.ssm.questionnaire.entity.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目出参，带选项
 */
public class QuestionVO {

    private Long id;
    private Long questionnaireId;
    private String content;
    private String type;
    private Integer sortNo;
    private List<OptionVO> options = new ArrayList<OptionVO>();

    public static QuestionVO from(Question question) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setQuestionnaireId(question.getQuestionnaireId());
        vo.setContent(question.getContent());
        vo.setType(question.getType());
        vo.setSortNo(question.getSortNo());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public List<OptionVO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionVO> options) {
        this.options = options;
    }
}
