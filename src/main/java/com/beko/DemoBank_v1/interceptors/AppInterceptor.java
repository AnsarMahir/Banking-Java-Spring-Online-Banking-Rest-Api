package com.beko.DemoBank_v1.interceptors;

import com.beko.DemoBank_v1.exception.CustomError;
import com.beko.DemoBank_v1.helpers.authorization.JwtService;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.BlacklistedTokenRepository;
import com.beko.DemoBank_v1.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class AppInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AppInterceptor.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    
    @Autowired
    public AppInterceptor(UserRepository userRepository, BlacklistedTokenRepository blacklistedTokenRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        // CRITICAL FIX (V-22): Use @Autowired JwtService instead of manual instantiation
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException, CustomError {

        logger.debug("Processing authentication for URI: {}", request.getRequestURI());

        // Check if request requires authentication
        if (request.getRequestURI().startsWith("/app") ||
                request.getRequestURI().startsWith("/transact") ||
                request.getRequestURI().startsWith("/logout") ||
                request.getRequestURI().startsWith("/account")) {

            // Get Authorization header
            String header = request.getHeader("Authorization");

            // Check if token is included
            if (!jwtService.isTokenIncluded(header)) {
                logger.warn("Missing authorization token for URI: {}", request.getRequestURI());
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            // CRITICAL FIX (V-21): Removed debug logging of sensitive tokens
            // Never log: System.out.println("Header: " + header);

            // Get Access Token From Header
            String token = jwtService.getAccessTokenFromHeader(header);

            //Check if token is blacklisted
            if(blacklistedTokenRepository.isTokenBlacklisted(token)) {
                throw new CustomError("Token has been revoked. Please login again.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            // CRITICAL FIX (V-06): Add null check before using claims
            Claims claims = jwtService.decodeToken(token);

            if (claims == null) {
                logger.warn("Invalid or expired token for URI: {}", request.getRequestURI());
                throw new CustomError("Invalid or expired token. Please login again.",
                        HttpServletResponse.SC_UNAUTHORIZED);
            }

            String email = claims.getSubject();

            if (email == null || email.isEmpty()) {
                logger.warn("Token missing email subject");
                throw new CustomError("Invalid token. Please login again.",
                        HttpServletResponse.SC_UNAUTHORIZED);
            }

            // Get User By Email
            User user = userRepository.getUserDetails(email);

            if (user == null) {
                logger.warn("User not found for email in token: {}", email);
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            // Open Session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("token", token);

            // CRITICAL FIX (V-21): Removed debug logging of sensitive session data
            // Never log: System.out.println("User: " + user);
            // Never log: System.out.println("Token: " + token);

            logger.debug("Authentication successful for user: {}", email);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        logger.debug("Post-handle processing complete");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        if (ex != null) {
            logger.error("Request completed with exception", ex);
        }
    }
}