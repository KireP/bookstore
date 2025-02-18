package com.sporty.bookstore.user.service;

import com.sporty.bookstore.user.dto.request.AuthRequestDto;
import com.sporty.bookstore.user.dto.response.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponseDto getToken(AuthRequestDto authRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            var authorities = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            return TokenResponseDto.builder()
                    .token(jwtService.generateToken(authRequest.getUsername(), authorities))
                    .build();
        } else {
            throw new UsernameNotFoundException("Invalid user request");
        }
    }
}
