package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.helpers.Token;
import com.beko.DemoBank_v1.helpers.authorization.JwtService;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import com.beko.DemoBank_v1.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> login(String email, String password, HttpSession session, HttpServletResponse response) {
        logger.debug("Login attempt for email: {}", email);
        try {
            logger.debug("Validating input fields");
            validateInputFields(email, password);

            logger.debug("Fetching user email from database");
            String userEmailInDatabase = userRepository.getUserEmail(email);

            if (userEmailInDatabase == null) {
                logger.warn("User not found: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect Username or Password");
            }

            logger.debug("Fetching password for user: {}", userEmailInDatabase);
            String passwordInDatabase = userRepository.getUserPassword(userEmailInDatabase);

            logger.debug("Checking password match");
            if (!BCrypt.checkpw(password, passwordInDatabase)) {
                logger.warn("Password mismatch for user: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect Username or Password");
            }

            logger.debug("Checking account verification status");
            int verified = userRepository.isVerified(userEmailInDatabase);

            if (verified != 1) {
                logger.warn("Account not verified: {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account verification required.");
            }

            logger.debug("Fetching user details");
            User user = userRepository.getUserDetails(userEmailInDatabase);

            logger.debug("Generating JWT token");
            String jwt = jwtService.generateToken(user.getEmail());

            // Token'i JSON yanıtının içine ekleyin
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Authentication confirmed");
            responseBody.put("access_token", jwt);

            logger.debug("Setting session attributes");
            session.setAttribute("user", user);
            session.setAttribute("token", jwt);
            session.setAttribute("authenticated", true);

            logger.info("Login successful for user: {}", email);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            logger.error("Login error for user: {}. Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            Object tokenObj = session.getAttribute("token");
            if (tokenObj instanceof String) {
                jwtService.revokeToken((String) tokenObj);
            }
            session.invalidate();
            return ResponseEntity.ok("Logged out successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    private void validateInputFields(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username or Password Cannot Be Empty.");
        }
    }
}
