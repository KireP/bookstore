package com.sporty.bookstore.user.filter;

import com.sporty.bookstore.user.service.JwtService;
import com.sporty.bookstore.user.service.UserInfoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final static String BEARER_TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserInfoService userInfoService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        var token = Optional.ofNullable(authHeader)
                .filter(header -> header.startsWith(BEARER_TOKEN_PREFIX))
                .map(header -> header.substring(BEARER_TOKEN_PREFIX.length()));
        var username = Optional.<String>empty();
        try {
            username = token.map(jwtService::extractUsername);
        } catch (Exception exception) {
            // Exception while parsing the token.
            // Some of the reasons might be token expiry or invalid token (invalid signature).
            // We do not authenticate the user in this case.
            logger.warn("Failed to extract username from token", exception);
        }

        if (username.isPresent() && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            var userDetails = userInfoService.loadUserByUsername(username.get());
            if (jwtService.validateToken(token.get(), userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
