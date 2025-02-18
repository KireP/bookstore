package com.sporty.bookstore.user.repository;

import com.sporty.bookstore.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UserInfoRepository extends JpaRepository<UserInfo, BigInteger> {

    Optional<UserInfo> findByUsername(String username);
}