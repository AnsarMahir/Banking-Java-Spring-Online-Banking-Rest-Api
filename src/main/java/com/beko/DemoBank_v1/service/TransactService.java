package com.beko.DemoBank_v1.service;

import com.beko.DemoBank_v1.models.PaymentRequest;
import com.beko.DemoBank_v1.models.TransferRequest;
import com.beko.DemoBank_v1.models.User;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface TransactService {

    ResponseEntity deposit(Map<String, String> requestMap, User user);

    ResponseEntity withdraw(Map<String, String> requestMap, User user);

    ResponseEntity payment(PaymentRequest request, User user);

    ResponseEntity transfer(TransferRequest request, User user);
}