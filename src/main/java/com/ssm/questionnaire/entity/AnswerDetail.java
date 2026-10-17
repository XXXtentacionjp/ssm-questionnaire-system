package com.ssm.questionnaire.entity;

/**
 * 答卷明细表 t_answer_detail，一行代表一个被选中的选项
 */
public class AnswerDetail {

    private Long id;
    private Long answerId;
    private Long questionId;
    private Long optionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }
}
