package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class CorsConfig {
    @GetMapping("/test")
    public String test() {
        return "Backend is working";
    }
}