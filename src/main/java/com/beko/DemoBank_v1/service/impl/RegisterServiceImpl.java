package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.helpers.HTML;
import com.beko.DemoBank_v1.helpers.Token;
import com.beko.DemoBank_v1.mailMessenger.MailMessenger;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import com.beko.DemoBank_v1.service.RegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);

    // CRITICAL FIX (V-12): Use SecureRandom and larger random space
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int VERIFICATION_CODE_BOUND = 999999; // 6-digit codes (100000-999999)

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailMessenger mailMessenger;

    @Override
    public ResponseEntity<?> registerUser(User user, String confirmPassword) {
        try {
            String firstName = user.getFirst_name();
            String lastName = user.getLast_name();
            String email = user.getEmail();
            String password = user.getPassword();

            // CRITICAL FIX (V-23): Check for duplicate email registration
            String existingEmail = userRepository.getUserEmail(email);
            if (existingEmail != null) {
                logger.warn("Registration attempt with existing email: {}", email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("An account with this email already exists.");
            }

            // Check password match
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body("Passwords do not match.");
            }

            // Generate secure verification token
            String token = Token.generateToken();

            // CRITICAL FIX (V-12): Generate cryptographically secure verification code
            int code = generateSecureVerificationCode();

            // Get email HTML body
            String emailBody = HTML.htmlEmailTemplate(token, Integer.toString(code));

            // Hash password
            String hashed_password = BCrypt.hashpw(password, BCrypt.gensalt());

            // Register user
            userRepository.registerUser(firstName, lastName, email, hashed_password, token, Integer.toString(code));

            // Send email notification
            sendEmailNotification(email, emailBody);

            // CRITICAL FIX (V-11, V-17): Don't return sensitive user data
            Map<String, Object> response = createSafeResponse();

            logger.info("User registered successfully: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during user registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed. Please try again.");
        }
    }

    // CRITICAL FIX (V-11, V-17): Don't expose user object with sensitive data
    private static Map<String, Object> createSafeResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful. Please check your email and verify your account.");
        // Don't include user object, password, token, or code
        return response;
    }

    private void sendEmailNotification(String email, String emailBody) {
        
        try {
            mailMessenger.htmlEmailMessenger("user@beko.com", email, "Verify Account", emailBody);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email.");
        }
    }

    // CRITICAL FIX (V-12): Cryptographically secure verification code generation
    private static int generateSecureVerificationCode() {
        // Generate 6-digit verification code (100000-999999)
        return 100000 + secureRandom.nextInt(900000);
    }

    @Deprecated
    private static int generateRandomCode() {
        // This method is deprecated, use generateSecureVerificationCode() instead
        return generateSecureVerificationCode();
    }
}