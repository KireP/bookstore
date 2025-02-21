package com.sporty.bookstore.user.filter;

import com.sporty.bookstore.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        var token = Optional.ofNullable(authHeader)
                .filter(header -> header.startsWith(BEARER_TOKEN_PREFIX))
                .map(header -> header.substring(BEARER_TOKEN_PREFIX.length()));
        try {
            var username = token.map(jwtService::extractUsername);
            if (username.isPresent() && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                var userDetails = userDetailsService.loadUserByUsername(username.get());
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
        } catch (Exception exception) {
            // Exception while parsing the token or loading user.
            // One of the reasons might be invalidity of token (invalid signature).
            // We do not authenticate the user in this case.
            logger.warn("Error while trying to retrieve user from token", exception);
        }

        filterChain.doFilter(request, response);
    }
}
