package com.ssm.questionnaire.dto;

import java.util.List;

/**
 * 新增题目入参
 */
public class QuestionCreateDTO {

    private String content;
    /** SINGLE / MULTIPLE */
    private String type;
    private Integer sortNo;
    private List<OptionCreateDTO> options;

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

    public List<OptionCreateDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionCreateDTO> options) {
        this.options = options;
    }
}
