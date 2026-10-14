package com.ssm.questionnaire.dto;

/**
 * 新增选项入参
 */
public class OptionCreateDTO {

    private String content;
    private Integer sortNo;

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
