package com.tracker.expense.tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracker.expense.tracker.dto.LoginRequest;
import com.tracker.expense.tracker.dto.RegisterRequest;
import com.tracker.expense.tracker.model.User;
import com.tracker.expense.tracker.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = authService.register(request);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<com.tracker.expense.tracker.dto.AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        com.tracker.expense.tracker.dto.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
