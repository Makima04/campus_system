package com.example.campussysteam.module.auth.service;

import com.example.campussysteam.module.auth.dto.AuthRequest;
import com.example.campussysteam.module.auth.dto.AuthResponse;
import com.example.campussysteam.module.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(AuthRequest request);
} 