package com.ssm.questionnaire.mapper;

import com.ssm.questionnaire.BaseIntegrationTest;
import com.ssm.questionnaire.common.Constants;
import com.ssm.questionnaire.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserMapperTest extends BaseIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void insertShouldFillGeneratedId() {
        User user = newUser("mapper_test");
        int rows = userMapper.insert(user);
        assertEquals(1, rows);
        assertNotNull("自增主键应回填", user.getId());
    }

    @Test
    public void selectByUsernameShouldRoundTripColumns() {
        User saved = newUser("round_trip");
        saved.setEmail("round@example.com");
        userMapper.insert(saved);

        User found = userMapper.selectByUsername("round_trip");
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("下划线列要映射成驼峰属性", saved.getPassword(), found.getPassword());
        assertEquals("小写昵称", found.getNickname());
        assertEquals("round@example.com", found.getEmail());
        assertEquals(Constants.ROLE_USER, found.getRole());
        assertEquals(Integer.valueOf(Constants.USER_STATUS_NORMAL), found.getStatus());
        assertNotNull(found.getCreateTime());
    }

    @Test
    public void selectByIdShouldReturnNullForMissingRow() {
        assertNull(userMapper.selectById(999999L));
    }

    private User newUser(String username) {
        Date now = new Date();
        User user = new User();
        user.setUsername(username);
        user.setPassword("8b56356d2022ae650980ffcec7d82133");
        user.setNickname("小写昵称");
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return user;
    }
}
