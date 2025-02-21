package com.sporty.bookstore.user.integration;

import com.sporty.bookstore.helper.AuthHeaderService;
import com.sporty.bookstore.helper.IntegrationTest;
import com.sporty.bookstore.user.dto.request.AuthRequestDto;
import org.junit.jupiter.api.Test;

import static com.sporty.bookstore.config.ApiConstants.AUTH_URI;
import static com.sporty.bookstore.config.ApiConstants.TOKEN_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTest {

    private static final String GET_TOKEN_ENDPOINT = AUTH_URI + TOKEN_URI;

    @Test
    void testGetTokenEndpoint_withValidCredentials_returnsStatusOK() throws Exception {
        var request = AuthRequestDto.builder()
                .username(AuthHeaderService.REGULAR_USER_USERNAME)
                .password(AuthHeaderService.PASSWORD)
                .build();

        mockMvc.perform(
                        post(GET_TOKEN_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testGetTokenEndpoint_withMissingData_returnsStatusBadRequest() throws Exception {
        var request = AuthRequestDto.builder()
                .username(AuthHeaderService.REGULAR_USER_USERNAME)
                .build();

        mockMvc.perform(
                        post(GET_TOKEN_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTokenEndpoint_withInvalidCredentials_returnsStatusForbidden() throws Exception {
        var request = AuthRequestDto.builder()
                .username(AuthHeaderService.REGULAR_USER_USERNAME)
                .password("invalidPassword")
                .build();

        mockMvc.perform(
                        post(GET_TOKEN_ENDPOINT)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());
    }
}
