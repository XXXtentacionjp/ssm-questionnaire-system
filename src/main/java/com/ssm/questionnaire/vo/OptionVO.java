package com.ssm.questionnaire.vo;

import com.ssm.questionnaire.entity.Option;

/**
 * 选项出参
 */
public class OptionVO {

    private Long id;
    private Long questionId;
    private String content;
    private Integer sortNo;

    public static OptionVO from(Option option) {
        OptionVO vo = new OptionVO();
        vo.setId(option.getId());
        vo.setQuestionId(option.getQuestionId());
        vo.setContent(option.getContent());
        vo.setSortNo(option.getSortNo());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
