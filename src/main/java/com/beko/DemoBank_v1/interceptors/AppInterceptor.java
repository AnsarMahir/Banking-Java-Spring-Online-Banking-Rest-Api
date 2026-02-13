package com.beko.DemoBank_v1.interceptors;

import com.beko.DemoBank_v1.exception.CustomError;
import com.beko.DemoBank_v1.helpers.authorization.JwtService;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AppInterceptor implements HandlerInterceptor {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public AppInterceptor(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException, CustomError {

        if (request.getRequestURI().startsWith("/app")
                || request.getRequestURI().startsWith("/transact")
                || request.getRequestURI().startsWith("/logout")
                || request.getRequestURI().startsWith("/account")) {

            String header = request.getHeader("Authorization");
            if (!jwtService.isTokenIncluded(header)) {
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            final String token;
            try {
                token = jwtService.getAccessTokenFromHeader(header);
            } catch (IllegalArgumentException ex) {
                throw new CustomError("Invalid authorization header.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            if (jwtService.isTokenRevoked(token)) {
                throw new CustomError("Token has been revoked. Please log in again.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            Claims claims = jwtService.decodeToken(token);
            if (claims == null || claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
                throw new CustomError("Invalid or expired token.", HttpServletResponse.SC_UNAUTHORIZED);
            }

            String email = claims.getSubject();
            User user = userRepository.getUserDetails(email);

            request.getSession().setAttribute("user", user);
            request.getSession().setAttribute("token", token);

            if (user == null) {
                throw new CustomError("You need to be logged in.", HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // no-op
    }
}
