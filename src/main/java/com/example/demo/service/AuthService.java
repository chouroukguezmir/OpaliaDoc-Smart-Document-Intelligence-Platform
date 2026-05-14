package com.example.demo.service;

import com.example.demo.dto.AdminUserDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    void logout(String username);
    AdminUserDTO getProfile(String username);
}
