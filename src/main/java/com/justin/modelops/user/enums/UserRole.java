package com.justin.modelops.user.enums;

/**
 * Application roles. Spring Security authorities are derived by prefixing with
 * {@code ROLE_} (e.g. {@code USER} -> {@code ROLE_USER}).
 */
public enum UserRole {
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
