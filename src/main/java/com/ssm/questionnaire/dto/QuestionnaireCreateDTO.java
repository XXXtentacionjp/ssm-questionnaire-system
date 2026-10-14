package com.ssm.questionnaire.dto;

import java.util.List;

/**
 * 创建问卷入参，题目和选项一次性提交
 */
public class QuestionnaireCreateDTO {

    private String title;
    private String description;
    /** 0 草稿 1 已发布，不传按草稿处理 */
    private Integer status;
    private List<QuestionCreateDTO> questions;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<QuestionCreateDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionCreateDTO> questions) {
        this.questions = questions;
    }
}
