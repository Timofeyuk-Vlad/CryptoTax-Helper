package com.cryptotax.helper.entity;

public enum UserRole {
    ROLE_USER("Пользователь"),
    ROLE_PREMIUM("Премиум пользователь"),
    ROLE_ACCOUNTANT("Бухгалтер"),
    ROLE_ADMIN("Администратор"),
    ROLE_SUPER_ADMIN("Супер администратор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}