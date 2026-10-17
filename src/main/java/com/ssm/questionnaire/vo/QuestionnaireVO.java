package com.ssm.questionnaire.vo;

import com.ssm.questionnaire.entity.Questionnaire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 问卷出参。列表接口不返回 questions，详情接口才返回。
 */
public class QuestionnaireVO {

    private Long id;
    private String title;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Integer status;
    private Integer questionCount;
    private Date createTime;
    private Date updateTime;
    private List<QuestionVO> questions = new ArrayList<QuestionVO>();

    public static QuestionnaireVO from(Questionnaire questionnaire) {
        QuestionnaireVO vo = new QuestionnaireVO();
        vo.setId(questionnaire.getId());
        vo.setTitle(questionnaire.getTitle());
        vo.setDescription(questionnaire.getDescription());
        vo.setCreatorId(questionnaire.getCreatorId());
        vo.setStatus(questionnaire.getStatus());
        vo.setCreateTime(questionnaire.getCreateTime());
        vo.setUpdateTime(questionnaire.getUpdateTime());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<QuestionVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionVO> questions) {
        this.questions = questions;
    }
}
