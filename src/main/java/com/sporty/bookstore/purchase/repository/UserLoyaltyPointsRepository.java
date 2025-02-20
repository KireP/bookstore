package com.sporty.bookstore.purchase.repository;

import com.sporty.bookstore.purchase.entity.UserLoyaltyPoints;
import com.sporty.bookstore.user.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UserLoyaltyPointsRepository extends JpaRepository<UserLoyaltyPoints, BigInteger> {

    Optional<UserLoyaltyPoints> findByUserInfo(UserInfo userInfo);
}