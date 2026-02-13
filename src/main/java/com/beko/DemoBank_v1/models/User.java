package com.beko.DemoBank_v1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // CRITICAL FIX: Changed from "user" to "users" to match actual table name
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long user_id;

    @NotEmpty(message = "The First name field cannot be empty.")
    @Size(min = 3, message = "The first name field must be greater than 3 characters")
    private String first_name;

    @NotEmpty(message = "The Last name field cannot be empty.")
    @Size(min = 3, message = "The last name field must be greater than 3 characters")
    private String last_name;

    @Email
    @NotEmpty(message = "Email field cannot be empty.")
    @Pattern(regexp = "([a-zA-Z0-9]+(?:[._+-][a-zA-Z0-9]+)*)@([a-zA-Z0-9]+(?:[.-][a-zA-Z0-9]+)*[.][a-zA-Z]{2,})",
            message = "Please enter a valid email.")
    private String email;

    // CRITICAL FIX (V-11, V-17): Add @JsonIgnore to prevent password exposure in API responses
    @NotEmpty(message = "Password field cannot be empty.")
    @NotNull
    @Size(min = 8, message = "Password must be at least 8 characters long")  // CRITICAL FIX (V-33): Add password policy
    @JsonIgnore
    private String password;

    // CRITICAL FIX (V-11, V-17): Add @JsonIgnore to prevent token exposure
    @JsonIgnore
    private String token;

    // CRITICAL FIX (V-11, V-17): Add @JsonIgnore to prevent verification code exposure
    @JsonIgnore
    private String code;

    private int verified;
    private LocalDate verified_at;
    private LocalDateTime create_at;
    private LocalDateTime updated_at;

    // Getters and Setters
    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public LocalDate getVerified_at() {
        return verified_at;
    }

    public void setVerified_at(LocalDate verified_at) {
        this.verified_at = verified_at;
    }

    public LocalDateTime getCreate_at() {
        return create_at;
    }

    public void setCreate_at(LocalDateTime create_at) {
        this.create_at = create_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}