package com.beko.DemoBank_v1.util;

public class SecureLogUtil {

    public static String maskSensitive(String input) {
        if (input == null || input.length() <= 4) {
            return "****";
        }

        int visibleChars = Math.min(4, input.length() / 3);
        int start = visibleChars;
        int end = input.length() - visibleChars;

        StringBuilder masked = new StringBuilder();
        masked.append(input, 0, start);
        masked.append("****");
        masked.append(input, end, input.length());

        return masked.toString();
    }
}
