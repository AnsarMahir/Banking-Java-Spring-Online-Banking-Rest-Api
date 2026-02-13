package com.beko.DemoBank_v1.controllers;

import com.beko.DemoBank_v1.models.PaymentRequest;
import com.beko.DemoBank_v1.models.TransferRequest;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.service.TransactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/transact")
public class TransactController {
    // CRITICAL FIX (V-07): Removed instance-level User variable to prevent thread-safety issues

    @Autowired
    private TransactService transactService;

    @PostMapping("/deposit")
    public ResponseEntity deposit(@RequestBody Map<String, String> requestMap, HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable instead of instance field
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return transactService.deposit(requestMap, user);
    }

    @PostMapping("/withdraw")
    public ResponseEntity withdraw(@RequestBody Map<String, String> requestMap, HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return transactService.withdraw(requestMap, user);
    }

    @PostMapping("/payment")
    public ResponseEntity payment(@RequestBody PaymentRequest request, HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return transactService.payment(request, user);
    }

    @PostMapping("/transfer")
    public ResponseEntity transfer(@RequestBody TransferRequest request, HttpSession session) {
        // CRITICAL FIX (V-07): Use local variable
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
        }

        return transactService.transfer(request, user);
    }
}