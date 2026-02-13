package com.beko.DemoBank_v1.service.impl;

import com.beko.DemoBank_v1.helpers.HTML;
import com.beko.DemoBank_v1.helpers.Token;
import com.beko.DemoBank_v1.mailMessenger.MailMessenger;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import com.beko.DemoBank_v1.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.util.*;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailMessenger mailMessenger;

    @Override
    public ResponseEntity<?> registerUser(User user, String confirmPassword) {
        String firstName = user.getFirst_name();
        String lastName = user.getLast_name();
        String email = user.getEmail();
        String password = user.getPassword();

        // CHECK PASSWORD COMPLEXITY:
        String passwordError = validatePasswordStrength(password);
        if (passwordError != null)
            return ResponseEntity.badRequest().body(passwordError);

        // CHECK FOR PASSWORD MATCH:
        if (!password.equals(confirmPassword))
            return ResponseEntity.badRequest().body("Passwords do not match.");

        // TODO: GET TOKEN STRING:
        String token = Token.generateToken();

        int code = generateRandomCode();

        // TODO: GET EMAIL HTML BODY
        String emailBody = HTML.htmlEmailTemplate(token, Integer.toString(code));

        // TODO: HASH PASSWORD:
        String hashed_password = BCrypt.hashpw(password, BCrypt.gensalt());

        // TODO: REGISTER USER:
        userRepository.registerUser(firstName, lastName, email, hashed_password, token, Integer.toString(code));

        sendEmailNotification(email, emailBody);

        Map<String, Object> response = createResponse(user);

        return ResponseEntity.ok(response);
    }

    private static Map<String, Object> createResponse(User user) {
        // TODO: RETURN REGISTIRATION SUCCESS
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration success. Please check your email and verify your account.");
        response.put("user", user); // veya başka verileri ekleyebilirsiniz
        return response;
    }

    private void sendEmailNotification(String email, String emailBody) {

        try {
            mailMessenger.htmlEmailMessenger("user@beko.com", email, "Verify Account", emailBody);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String validatePasswordStrength(String password) {
        if (password.length() < 8)
            return "Password must be at least 8 characters long.";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter.";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one numeric digit.";
        if (!password.matches(".*[@#$%&!^*()_+=\\-].*"))
            return "Password must contain at least one special character (e.g., @, #, $, %, &).";
        return null;
    }

    private static int generateRandomCode() {
        SecureRandom secureRandom = new SecureRandom();
        // Generates a 6-digit code between 100000 and 999999 (1,000,000 possible
        // values)
        return 100000 + secureRandom.nextInt(900000);
    }
}
