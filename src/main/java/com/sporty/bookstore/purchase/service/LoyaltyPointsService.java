package com.sporty.bookstore.purchase.service;

import com.sporty.bookstore.purchase.dto.response.LoyaltyPointsResponseDto;
import com.sporty.bookstore.purchase.entity.UserLoyaltyPoints;
import com.sporty.bookstore.purchase.repository.UserLoyaltyPointsRepository;
import com.sporty.bookstore.user.entity.UserInfo;
import com.sporty.bookstore.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class LoyaltyPointsService {

    @Value("${loyalty-points.max}")
    private int maxLoyaltyPoints;

    private final UserLoyaltyPointsRepository userLoyaltyPointsRepository;
    private final UserInfoService userInfoService;

    @Transactional(readOnly = true)
    public LoyaltyPointsResponseDto getLoyaltyPointsForLoggedInUser() {
        var user = userInfoService.loadUserInfoEntityFromSecurityContext();
        return getLoyaltyPoints(user);
    }

    @Transactional(readOnly = true)
    public LoyaltyPointsResponseDto getLoyaltyPointsByUserId(BigInteger userId) {
        var user = userInfoService.loadUserInfoEntityById(userId);
        return getLoyaltyPoints(user);
    }

    @Transactional
    public LoyaltyPointsResponseDto applyLoyaltyPointsToLoggedInUser(int loyaltyPoints, boolean add) {
        var user = userInfoService.loadUserInfoEntityFromSecurityContext();
        var userPoints = userLoyaltyPointsRepository.findByUserInfo(user)
                .orElseGet(() -> {
                    var userPointsToCreate = new UserLoyaltyPoints();
                    userPointsToCreate.setUserInfo(user);
                    userPointsToCreate.setLoyaltyPoints(0);
                    return userPointsToCreate;
                });
        if (add) {
            userPoints.setLoyaltyPoints(Math.min(maxLoyaltyPoints, userPoints.getLoyaltyPoints() + loyaltyPoints));
        } else {
            userPoints.setLoyaltyPoints(Math.min(maxLoyaltyPoints, loyaltyPoints));
        }
        userPoints = userLoyaltyPointsRepository.save(userPoints);
        return new LoyaltyPointsResponseDto(userPoints.getLoyaltyPoints());
    }

    public int retrieveMaxLoyaltyPoints() {
        return maxLoyaltyPoints;
    }

    private LoyaltyPointsResponseDto getLoyaltyPoints(UserInfo user) {
        var loyaltyPoints = userLoyaltyPointsRepository.findByUserInfo(user)
                .map(UserLoyaltyPoints::getLoyaltyPoints)
                .orElse(0);
        return new LoyaltyPointsResponseDto(loyaltyPoints);
    }
}
