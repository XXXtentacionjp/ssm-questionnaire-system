package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.entity.Questionnaire;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QuestionnaireMapper {

    int insert(Questionnaire questionnaire);

    Questionnaire selectById(@Param("id") Long id);

    /** 本轮只查自己创建的问卷，公开列表还没做 */
    List<Questionnaire> selectByCreatorId(@Param("creatorId") Long creatorId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    long countByCreatorId(@Param("creatorId") Long creatorId);

    int updateById(Questionnaire questionnaire);

    int deleteById(@Param("id") Long id);
}
