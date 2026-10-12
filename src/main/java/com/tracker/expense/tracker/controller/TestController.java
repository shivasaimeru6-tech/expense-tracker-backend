package com.tracker.expense.tracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/test/secure")
    public String secureEndpoint(Authentication authentication) {
        return "Hello, " + authentication.getName() + "! You are authenticated.";
    }
}
