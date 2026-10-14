package com.ssm.questionnaire.entity;

import java.util.Date;

/**
 * 答卷表 t_answer
 * 按基础需求要求先把表结构定下来，提交答卷的接口还没做。
 */
public class Answer {

    private Long id;
    private Long questionnaireId;
    private Long userId;
    private Date submitTime;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
}
