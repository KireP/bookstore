package com.sporty.bookstore.purchase.integration;

import com.sporty.bookstore.helper.AuthHeaderService;
import com.sporty.bookstore.helper.IntegrationTest;
import com.sporty.bookstore.purchase.entity.UserLoyaltyPoints;
import com.sporty.bookstore.purchase.repository.UserLoyaltyPointsRepository;
import com.sporty.bookstore.user.entity.UserInfo;
import com.sporty.bookstore.user.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static com.sporty.bookstore.config.ApiConstants.LOYALTY_POINTS_API_URI;
import static com.sporty.bookstore.config.ApiConstants.PERSONAL_LOYALTY_POINTS_API_URI;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoyaltyPointsControllerTest extends IntegrationTest {

    private static final String GET_MY_LOYALTY_POINTS_ENDPOINT = LOYALTY_POINTS_API_URI + PERSONAL_LOYALTY_POINTS_API_URI;
    private static final String GET_USER_LOYALTY_POINTS_ENDPOINT = LOYALTY_POINTS_API_URI + "/{userId}";

    @Autowired
    private UserLoyaltyPointsRepository userLoyaltyPointsRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void testGetMyLoyaltyPointsEndpoint_asAdminUser_returnsStatusOK() throws Exception {
        var loyaltyPoints = 5;
        setLoyaltyPointsToUser(AuthHeaderService.ADMIN_USER_USERNAME, loyaltyPoints);

        mockMvc.perform(
                        get(GET_MY_LOYALTY_POINTS_ENDPOINT)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(loyaltyPoints));
    }

    @Test
    void testGetMyLoyaltyPointsEndpoint_asRegularUser_returnsStatusOK() throws Exception {
        var loyaltyPoints = 10;
        setLoyaltyPointsToUser(AuthHeaderService.REGULAR_USER_USERNAME, loyaltyPoints);

        mockMvc.perform(
                        get(GET_MY_LOYALTY_POINTS_ENDPOINT)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(loyaltyPoints));
    }

    @Test
    void testGetMyLoyaltyPointsEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(GET_MY_LOYALTY_POINTS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserLoyaltyPointsEndpoint_asAdminUser_returnsStatusOK() throws Exception {
        var loyaltyPoints = 6;
        setLoyaltyPointsToUser(AuthHeaderService.REGULAR_USER_USERNAME, loyaltyPoints);
        var userId = userInfoRepository.findByUsername(AuthHeaderService.REGULAR_USER_USERNAME)
                .map(UserInfo::getId)
                .orElseThrow();

        mockMvc.perform(
                        get(GET_USER_LOYALTY_POINTS_ENDPOINT, userId)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(loyaltyPoints));
    }

    @Test
    void testGetUserLoyaltyPointsEndpoint_asRegularUser_returnsStatusForbidden() throws Exception {
        mockMvc.perform(
                        get(GET_USER_LOYALTY_POINTS_ENDPOINT, 1)
                                .headers(authHeaderService.getRegularUserAuthHeader())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserLoyaltyPointsEndpoint_forNonexistentUser_asAdminUser_returnsStatusNotFound() throws Exception {
        mockMvc.perform(
                        get(GET_USER_LOYALTY_POINTS_ENDPOINT, 123456)
                                .headers(authHeaderService.getAdminUserAuthHeader())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserLoyaltyPointsEndpoint_withoutJwtAuth_returnsStatusForbidden() throws Exception {
        mockMvc.perform(get(GET_USER_LOYALTY_POINTS_ENDPOINT, 1))
                .andExpect(status().isForbidden());
    }

    private void setLoyaltyPointsToUser(String username, int loyaltyPoints) {
        transactionTemplate.executeWithoutResult(x -> {
            var user = userInfoRepository.findByUsername(username).orElseThrow();
            var userLoyaltyPoints = userLoyaltyPointsRepository.findByUserInfo(user)
                    .orElseGet(() -> {
                        var userPointsToCreate = new UserLoyaltyPoints();
                        userPointsToCreate.setUserInfo(user);
                        userPointsToCreate.setLoyaltyPoints(0);
                        return userPointsToCreate;
                    });
            userLoyaltyPoints.setLoyaltyPoints(loyaltyPoints);
            userLoyaltyPointsRepository.save(userLoyaltyPoints);
        });
    }
}
