package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.entity.UserToken;
import org.apache.ibatis.annotations.Param;

public interface UserTokenMapper {

    int insert(UserToken userToken);

    UserToken selectByToken(@Param("token") String token);
}
