package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.entity.Option;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OptionMapper {

    int insert(Option option);

    List<Option> selectByQuestionId(@Param("questionId") Long questionId);

    int deleteByQuestionId(@Param("questionId") Long questionId);

    int deleteByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
