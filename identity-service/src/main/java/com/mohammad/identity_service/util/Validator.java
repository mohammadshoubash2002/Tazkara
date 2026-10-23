package com.mohammad.identity_service.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d\\W_]{8,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");

    private Validator() {
        // Prevent instantiation of utility class
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        };

        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
}