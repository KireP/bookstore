package com.sporty.bookstore.user.mapper;

import com.sporty.bookstore.user.dto.request.NewUserRequestDto;
import com.sporty.bookstore.user.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = UserMapperImpl.class)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testMapNewUserRequestDtoToUserInfo() {
        var newUserRequestDto = NewUserRequestDto.builder()
                .username("testUsername")
                .password("testPassword")
                .roles(List.of("ROLE_ADMIN", "ROLE_USER"))
                .build();

        var result = userMapper.toUserInfo(newUserRequestDto);

        assertEquals(result.getUsername(), newUserRequestDto.getUsername());
        assertEquals(result.getPassword(), newUserRequestDto.getPassword());
        assertEquals(result.getRoles(), String.join(",", newUserRequestDto.getRoles()));
    }

    @Test
    void testMapUserInfoToUserInfoResponseDto() {
        var userInfo = UserInfo.builder()
                .id(BigInteger.ONE)
                .username("testUsername")
                .roles("ROLE_ADMIN,ROLE_USER")
                .build();

        var result = userMapper.toUserInfoResponseDto(userInfo);

        assertEquals(result.getId(), userInfo.getId());
        assertEquals(result.getUsername(), userInfo.getUsername());
        assertEquals(result.getRoles().size(), 2);
        assertEquals(result.getRoles().get(0), "ROLE_ADMIN");
        assertEquals(result.getRoles().get(1), "ROLE_USER");
    }
}