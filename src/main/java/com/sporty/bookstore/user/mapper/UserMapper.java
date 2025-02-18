package com.sporty.bookstore.user.mapper;

import com.sporty.bookstore.user.dto.request.NewUserRequestDto;
import com.sporty.bookstore.user.dto.response.UserInfoResponseDto;
import com.sporty.bookstore.user.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserInfo toUserInfo(NewUserRequestDto newUserRequestDto);

    @Mapping(target = "roles", source = "userInfo.roles", qualifiedByName = "getRoles")
    UserInfoResponseDto toUserInfoResponseDto(UserInfo userInfo);

    @Named("getRoles")
    default List<String> getRoles(String roles) {
        return Arrays.asList(roles.split(","));
    }
}
