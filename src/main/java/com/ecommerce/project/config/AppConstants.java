package com.ecommerce.project.config;

public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Pagination defaults
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "10";

    // Sorting defaults
    public static final String SORT_CATEGORIES_BY = "categoryId";
    public static final String SORT_PRODUCTS_BY = "productId";
    public static final String SORT_ORDERS_BY = "totalAmount";
    public static final String SORT_USERS_BY = "userId";
    public static final String SORT_DIR = "asc";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SELLER = "SELLER";
}