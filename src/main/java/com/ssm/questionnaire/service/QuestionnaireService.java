package com.ssm.questionnaire.service;

import com.ssm.questionnaire.common.PageResult;
import com.ssm.questionnaire.dto.PageQuery;
import com.ssm.questionnaire.dto.QuestionnaireCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireUpdateDTO;
import com.ssm.questionnaire.vo.QuestionnaireVO;

public interface QuestionnaireService {

    /**
     * 创建问卷，连同题目和选项一起落库，返回新问卷 id
     */
    Long create(QuestionnaireCreateDTO dto, Long creatorId);

    /**
     * 分页查询当前用户创建的问卷
     */
    PageResult<QuestionnaireVO> pageMyOwn(Long creatorId, PageQuery query);

    /**
     * 问卷详情，含题目与选项
     */
    QuestionnaireVO detail(Long id);

    /**
     * 修改问卷，只有创建者能改
     */
    void update(QuestionnaireUpdateDTO dto, Long operatorId);

    /**
     * 删除问卷，级联删除题目与选项，只有创建者能删
     */
    void delete(Long id, Long operatorId);
}
