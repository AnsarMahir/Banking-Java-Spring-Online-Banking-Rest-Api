package com.beko.DemoBank_v1.service;

import com.beko.DemoBank_v1.models.User;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AppService {
    ResponseEntity<?> getDashboard(User user);

    ResponseEntity<?> getPaymentHistory(User user);

    ResponseEntity<?> getTransactionHistory(User user);

    // CRITICAL FIX (V-05): Added User parameter for ownership validation
    ResponseEntity<?> getAccountTransactionHistory(Map<String, String> requestMap, User user);
}