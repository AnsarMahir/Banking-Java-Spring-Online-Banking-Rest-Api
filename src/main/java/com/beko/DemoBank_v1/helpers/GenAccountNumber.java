package com.beko.DemoBank_v1.helpers;

import java.security.SecureRandom;

public class GenAccountNumber {

    public static int generateAccountNumber() {
        SecureRandom secureRandom = new SecureRandom();
        // Generates a 10-digit account number between 1000000000 and 1999999999
        return 1000000000 + secureRandom.nextInt(1000000000);
    }
    // End of Generate Account Number
}