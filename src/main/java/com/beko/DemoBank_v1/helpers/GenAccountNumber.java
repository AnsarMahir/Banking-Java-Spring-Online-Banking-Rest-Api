package com.beko.DemoBank_v1.helpers;

import java.security.SecureRandom;

public class GenAccountNumber {

    // CRITICAL FIX (V-13): Use SecureRandom instead of Random
    private static final SecureRandom secureRandom = new SecureRandom();

    // CRITICAL FIX (V-13): Generate account numbers with much larger random space
    // Generate 10-digit account numbers (1000000000-9999999999)
    private static final long MIN_ACCOUNT_NUMBER = 1000000000L;
    private static final long MAX_ACCOUNT_NUMBER = 9999999999L;

    public static String generateAccountNumber() {
        // Generate a random 10-digit account number
        long range = MAX_ACCOUNT_NUMBER - MIN_ACCOUNT_NUMBER + 1;
        long randomLong = MIN_ACCOUNT_NUMBER + (long)(secureRandom.nextDouble() * range);

        return String.valueOf(randomLong);
    }
}