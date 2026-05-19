package com.sergiovitorino.springbootjwt.infrastructure.security;

public final class PiiMasker {

    private PiiMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "*" + email.substring(atIndex);
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
