package com.sporty.bookstore.user.integration;

import com.sporty.bookstore.helper.AuthHeaderService;
import com.sporty.bookstore.helper.IntegrationTest;
import com.sporty.bookstore.user.dto.request.NewUserRequestDto;
import com.sporty.bookstore.user.entity.UserInfo;
import com.sporty.bookstore.user.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.sporty.bookstore.config.ApiConstants.PERSONAL_PROFILE_API_URI;
import static com.sporty.bookstore.config.ApiConstants.USERS_API_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends IntegrationTest {

    private static final String GET_MY_PROFILE_ENDPOINT = USERS_API_URI + PERSONAL_PROFILE_API_URI;
    private static final String GET_USER_PROFILE_ENDPOINT = USERS_API_URI + "/{userId}";
    private static final String CREATE_USER_ENDPOINT = USERS_API_URI;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    void testGetMyUserProfileEndpoint_asAdminUser_returnsStatusOK() throws Exception {
        mockMvc.perform(
                        get(GET_MY_PROFILE_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roles").isNotEmpty())
                .andExpect(jsonPath("$.username").value(AuthHeaderService.ADMIN_USER_USERNAME));
    }

    @Test
    void testGetMyUserProfileEndpoint_asRegularUser_returnsStatusOK() throws Exception {
        mockMvc.perform(
                        get(GET_MY_PROFILE_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roles").isNotEmpty())
                .andExpect(jsonPath("$.username").value(AuthHeaderService.REGULAR_USER_USERNAME));
    }

    @Test
    void testGetMyUserProfileEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(GET_MY_PROFILE_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserProfileEndpoint_asAdminUser_returnsStatusOK() throws Exception {
        var userId = userInfoRepository.findByUsername(AuthHeaderService.REGULAR_USER_USERNAME)
                .map(UserInfo::getId)
                .orElseThrow();

        mockMvc.perform(
                        get(GET_USER_PROFILE_ENDPOINT, userId)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roles").isNotEmpty())
                .andExpect(jsonPath("$.username").isNotEmpty());
    }

    @Test
    void testGetUserProfileEndpoint_withNonexistentUserId_asAdminUser_returnsStatusNotFound() throws Exception {
        mockMvc.perform(
                        get(GET_USER_PROFILE_ENDPOINT, 123456)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserProfileEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        mockMvc.perform(
                        get(GET_USER_PROFILE_ENDPOINT, 1)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserProfileEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(GET_USER_PROFILE_ENDPOINT, 1))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateUserEndpoint_asAdminUser_returnsStatusCreated() throws Exception {
        var request = NewUserRequestDto.builder()
                .username("test@test.com")
                .password("123")
                .roles(List.of("ROLE_ADMIN", "ROLE_USER"))
                .build();

        mockMvc.perform(
                        post(CREATE_USER_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.roles.[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles.[1]").value("ROLE_USER"))
                .andExpect(jsonPath("$.roles.[2]").doesNotExist())
                .andExpect(jsonPath("$.username").value(request.getUsername()));

        var user = userInfoRepository.findByUsername(request.getUsername()).orElseThrow();
        assertEquals(user.getUsername(), request.getUsername());
        assertEquals(user.getRoles(), String.join(",", request.getRoles()));
    }

    @Test
    void testCreateUserEndpoint_withAlreadyExistingUsername_asAdminUser_returnsStatusBadRequest() throws Exception {
        var request = NewUserRequestDto.builder()
                .username(AuthHeaderService.REGULAR_USER_USERNAME)
                .password("123")
                .roles(List.of("ROLE_ADMIN", "ROLE_USER"))
                .build();

        mockMvc.perform(
                        post(CREATE_USER_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserEndpoint_withMissingData_asAdminUser_returnsStatusBadRequest() throws Exception {
        var request = NewUserRequestDto.builder()
                .username("test5@test.com")
                .build();

        mockMvc.perform(
                        post(CREATE_USER_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        var request = NewUserRequestDto.builder()
                .username("user5@user.com")
                .password("123")
                .roles(List.of("ROLE_USER"))
                .build();

        mockMvc.perform(
                        post(CREATE_USER_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateUserEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        var request = NewUserRequestDto.builder()
                .username("user6@user.com")
                .password("123")
                .roles(List.of("ROLE_USER"))
                .build();

        mockMvc.perform(
                        post(CREATE_USER_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }
}