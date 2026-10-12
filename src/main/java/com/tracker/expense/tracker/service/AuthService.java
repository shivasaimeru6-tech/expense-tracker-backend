package com.tracker.expense.tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tracker.expense.tracker.dto.LoginRequest;
import com.tracker.expense.tracker.dto.RegisterRequest;
import com.tracker.expense.tracker.exception.EmailAlreadyExistsException;
import com.tracker.expense.tracker.exception.InvalidCredentialsException;
import com.tracker.expense.tracker.model.Role;
import com.tracker.expense.tracker.model.User;
import com.tracker.expense.tracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
public User register(RegisterRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
    }

    User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();

    return userRepository.save(user);
}
public com.tracker.expense.tracker.dto.AuthResponse login(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new InvalidCredentialsException("Invalid email or password");
    }

    String token = jwtService.generateToken(user.getEmail());

    return new com.tracker.expense.tracker.dto.AuthResponse(token, user.getEmail(), user.getFullName());
}


}