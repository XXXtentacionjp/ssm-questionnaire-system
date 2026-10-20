package com.ssm.questionnaire.controller;

import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.common.PageResult;
import com.ssm.questionnaire.common.Result;
import com.ssm.questionnaire.dto.PageQuery;
import com.ssm.questionnaire.dto.QuestionnaireCreateDTO;
import com.ssm.questionnaire.dto.QuestionnaireUpdateDTO;
import com.ssm.questionnaire.service.QuestionnaireService;
import com.ssm.questionnaire.vo.QuestionnaireVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService questionnaireService;

    /**
     * 创建问卷：POST /api/questionnaire/create
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody QuestionnaireCreateDTO dto, HttpServletRequest request) {
        Long creatorId = currentUserId(request);
        return Result.success(questionnaireService.create(dto, creatorId));
    }

    /**
     * 我的问卷列表：GET /api/questionnaire/list?pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<PageResult<QuestionnaireVO>> list(PageQuery query, HttpServletRequest request) {
        Long creatorId = currentUserId(request);
        return Result.success(questionnaireService.pageMyOwn(creatorId, query));
    }

    /**
     * 问卷详情：GET /api/questionnaire/1001
     */
    @GetMapping("/{id}")
    public Result<QuestionnaireVO> detail(@PathVariable("id") Long id) {
        return Result.success(questionnaireService.detail(id));
    }

    /**
     * 修改问卷：PUT /api/questionnaire/update
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody QuestionnaireUpdateDTO dto, HttpServletRequest request) {
        Long operatorId = currentUserId(request);
        questionnaireService.update(dto, operatorId);
        return Result.success();
    }

    /**
     * 删除问卷：DELETE /api/questionnaire/delete/1001
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, HttpServletRequest request) {
        Long operatorId = currentUserId(request);
        questionnaireService.delete(id, operatorId);
        return Result.success();
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(Constants.CURRENT_USER_ID);
    }
}
