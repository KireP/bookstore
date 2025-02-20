package com.sporty.bookstore.config;

import com.sporty.bookstore.user.config.SecurityConfig;

public final class ApiConstants {

    private ApiConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ADMIN_PRE_AUTHORIZATION = "hasAuthority('" + SecurityConfig.ROLE_ADMIN + "')";
    public static final String ADMIN_OR_USER_PRE_AUTHORIZATION = "hasAnyAuthority('" + SecurityConfig.ROLE_ADMIN + "','" + SecurityConfig.ROLE_USER + "')";

    public static final String AUTH_URI = "/auth";
    public static final String TOKEN_URI = "/token";

    public static final String BASE_API_URI = "/api/v1";

    public static final String USERS_API_URI = BASE_API_URI + "/users";
    public static final String PERSONAL_PROFILE_API_URI = "/me";

    public static final String BOOKS_API_URI = BASE_API_URI + "/books";

    public static final String LOYALTY_POINTS_API_URI = BASE_API_URI + "/loyalty-points";
    public static final String PERSONAL_LOYALTY_POINTS_API_URI = "/me";

    public static final String ORDERS_API_URI = BASE_API_URI + "/orders";
    public static final String ORDER_SUMMARISE_PRICE_API_URI = "/summarise";
    public static final String ORDER_PURCHASE_API_URI = "/purchase";
}
