package com.sporty.bookstore.user.service;

import com.sporty.bookstore.exception.UserIdNotFoundException;
import com.sporty.bookstore.exception.UsernameAlreadyExistsException;
import com.sporty.bookstore.user.dto.request.NewUserRequestDto;
import com.sporty.bookstore.user.dto.response.UserInfoResponseDto;
import com.sporty.bookstore.user.entity.UserInfo;
import com.sporty.bookstore.user.mapper.UserMapper;
import com.sporty.bookstore.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userInfoRepository.findByUsername(username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User with username %s not found", username)));
    }

    public UserInfoResponseDto loadUserFromSecurityContext() {
        return userMapper.toUserInfoResponseDto(loadUserInfoEntityFromSecurityContext());
    }

    public UserInfo loadUserInfoEntityFromSecurityContext() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(UserInfoDetails.class::isInstance)
                .map(UserInfoDetails.class::cast)
                .flatMap(userInfoDetails -> userInfoRepository.findByUsername(userInfoDetails.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found in security context"));
    }

    public UserInfoResponseDto loadUserById(BigInteger id) {
        return userMapper.toUserInfoResponseDto(loadUserInfoEntityById(id));
    }

    public UserInfo loadUserInfoEntityById(BigInteger id) {
        return userInfoRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
    }

    @Transactional
    public UserInfoResponseDto createNewUser(NewUserRequestDto newUser) {
        if (userInfoRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(newUser.getUsername());
        }
        var userInfo = userMapper.toUserInfo(newUser);
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfo = userInfoRepository.save(userInfo);
        return userMapper.toUserInfoResponseDto(userInfo);
    }
}
