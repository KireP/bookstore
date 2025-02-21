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

    @Mapping(target = "roles", source = "newUserRequestDto.roles", qualifiedByName = "concatenateRoles")
    UserInfo toUserInfo(NewUserRequestDto newUserRequestDto);

    @Mapping(target = "roles", source = "userInfo.roles", qualifiedByName = "getRoles")
    UserInfoResponseDto toUserInfoResponseDto(UserInfo userInfo);

    @Named("concatenateRoles")
    default String concatenateRoles(List<String> roles) {
        return String.join(",", roles);
    }

    @Named("getRoles")
    default List<String> getRoles(String roles) {
        return Arrays.asList(roles.split(","));
    }
}
