package com.beko.DemoBank_v1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * CRITICAL FIX (V-02, V-16): Spring Security Configuration
 *
 * This configuration addresses multiple critical vulnerabilities:
 * - V-02: Adds Spring Security framework
 * - V-16: Enables CSRF protection
 * - Adds security response headers
 * - Configures session management
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // CRITICAL FIX (V-16): Enable CSRF protection for state-changing operations
                .csrf()
                .ignoringAntMatchers("/login", "/register") // Allow these endpoints without CSRF for initial setup
                .and()

                // Configure authorization
                .authorizeRequests()
                .antMatchers("/register", "/login", "/verify").permitAll()
                .antMatchers("/app/**", "/transact/**", "/account/**", "/logout").authenticated()
                .anyRequest().permitAll()
                .and()

                // Session management
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Limit to one concurrent session per user
                .maxSessionsPreventsLogin(false) // Allow new session, invalidate old one
                .and()
                .sessionFixation().migrateSession() // Prevent session fixation attacks
                .and()

                // Security Headers
                .headers()
                // X-Frame-Options: Prevent clickjacking
                .frameOptions().deny()

                // X-Content-Type-Options: Prevent MIME-sniffing
                .contentTypeOptions()
                .and()

                // X-XSS-Protection (Spring Boot 2.7 syntax)
                .xssProtection()
                .and()

                // Referrer-Policy
                .referrerPolicy()
                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()

                // Content-Security-Policy
                .contentSecurityPolicy("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                .and()

                // HSTS (HTTP Strict Transport Security) - Enable in production with HTTPS
                // Commented out for development, uncomment for production
                // .httpStrictTransportSecurity()
                //     .includeSubDomains(true)
                //     .maxAgeInSeconds(31536000) // 1 year
                .and()

                // Disable HTTP Basic and Form Login since we're using JWT
                .httpBasic().disable()
                .formLogin().disable()
                .logout().disable(); // Custom logout is handled in AuthController
    }
}