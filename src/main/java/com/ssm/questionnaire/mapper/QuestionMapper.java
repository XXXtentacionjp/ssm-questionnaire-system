package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionMapper {

    int insert(Question question);

    List<Question> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    int countByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    int deleteByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);
}
