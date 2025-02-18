package com.sporty.bookstore.user.api;

import com.sporty.bookstore.config.ApiConstants;
import com.sporty.bookstore.user.dto.request.NewUserRequestDto;
import com.sporty.bookstore.user.dto.response.UserInfoResponseDto;
import com.sporty.bookstore.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RequestMapping(ApiConstants.USERS_API_URI)
@RestController
@SecurityRequirement(name = "bearerAuth")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService userInfoService;

    @GetMapping(ApiConstants.PERSONAL_PROFILE_API_URI)
    @PreAuthorize(ApiConstants.ADMIN_OR_USER_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN, ROLE_USER] Retrieves the logged-in user's profile.")
    public UserInfoResponseDto getMyProfile() {
        return userInfoService.loadUserFromSecurityContext();
    }

    @GetMapping("/{userId}")
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @Operation(summary = "[ROLE_ADMIN] Retrieves any user's profile.")
    public UserInfoResponseDto getProfile(@PathVariable("userId") BigInteger userId) {
        return userInfoService.loadUserById(userId);
    }

    @PostMapping
    @PreAuthorize(ApiConstants.ADMIN_PRE_AUTHORIZATION)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[ROLE_ADMIN] Creates a user.")
    public UserInfoResponseDto createNewUser(@RequestBody @Valid NewUserRequestDto request) {
        return userInfoService.createNewUser(request);
    }

}
