package com.ssm.questionnaire.dto;

import java.util.List;

/**
 * 修改问卷入参。
 * questions 传 null 表示只改问卷本身、不动题目；传数组表示按新数组整体替换题目。
 */
public class QuestionnaireUpdateDTO {

    private Long id;
    private String title;
    private String description;
    private Integer status;
    private List<QuestionCreateDTO> questions;

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
