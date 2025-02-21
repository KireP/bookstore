package com.sporty.bookstore.helper;

import com.sporty.bookstore.user.dto.request.AuthRequestDto;
import com.sporty.bookstore.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthHeaderService {

    public static final String ADMIN_USER_USERNAME = "test.admin@sporty.com";
    public static final String REGULAR_USER_USERNAME = "test.user@sporty.com";
    public static final String PASSWORD = "123456";

    private final AuthService authService;

    public HttpHeaders getAdminUserAuthHeader() {
        var token = authService.getToken(new AuthRequestDto(ADMIN_USER_USERNAME, PASSWORD)).getToken();
        return getAuthHeader(token);
    }

    public HttpHeaders getRegularUserAuthHeader() {
        var token = authService.getToken(new AuthRequestDto(REGULAR_USER_USERNAME, PASSWORD)).getToken();
        return getAuthHeader(token);
    }

    private HttpHeaders getAuthHeader(String token) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + token));
        return httpHeaders;
    }

}
