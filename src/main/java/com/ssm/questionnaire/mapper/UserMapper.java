package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    int insert(User user);

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);
}
