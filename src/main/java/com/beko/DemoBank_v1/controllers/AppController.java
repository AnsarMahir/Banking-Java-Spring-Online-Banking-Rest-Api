package com.beko.DemoBank_v1.controllers;

import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    // CRITICAL FIX (V-07): Removed instance-level User variable to prevent thread-safety issues

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable instead of instance field
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return appService.getDashboard(user);
    }

    @GetMapping("/payment_history")
    public ResponseEntity<?> getPaymentHistory(HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return appService.getPaymentHistory(user);
    }

    @GetMapping("/transaction_history")
    public ResponseEntity<?> getTransactionHistory(HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return appService.getTransactionHistory(user);
    }

    @PostMapping("/account_transaction_history")
    public ResponseEntity<?> getAccountTransactionHistory(@RequestBody Map<String, String> requestMap, HttpSession session) {
        // CRITICAL FIX (V-05): Pass user to service for ownership validation
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return appService.getAccountTransactionHistory(requestMap, user);
    }
}