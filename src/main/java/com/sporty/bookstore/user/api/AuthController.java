package com.sporty.bookstore.user.api;

import com.sporty.bookstore.config.ApiConstants;
import com.sporty.bookstore.user.dto.request.AuthRequestDto;
import com.sporty.bookstore.user.dto.response.TokenResponseDto;
import com.sporty.bookstore.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstants.AUTH_URI)
@RestController
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiConstants.TOKEN_URI)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Retrieves a JWT token by supplying credentials.")
    public TokenResponseDto getToken(@RequestBody @Valid AuthRequestDto authRequest) {
        return authService.getToken(authRequest);
    }
}
