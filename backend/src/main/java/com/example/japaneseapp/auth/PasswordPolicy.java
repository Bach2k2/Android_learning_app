package com.example.japaneseapp.auth;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    public static boolean fitsBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public static boolean meetsAdminMinimum(String password) {
        return password.codePointCount(0, password.length()) >= 12 && fitsBcrypt(password);
    }
}
