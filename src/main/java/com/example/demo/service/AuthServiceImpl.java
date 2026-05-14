package com.example.demo.service;

import com.example.demo.dto.AdminUserDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.AdminUser;
import com.example.demo.repository.AdminUserRepository;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ── Login ─────────────────────────────────────────────────────
    @Override
    public LoginResponse login(LoginRequest request) {
        AdminUser user = adminUserRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .message("Connexion réussie")
                .build();
    }

    // ── Register ──────────────────────────────────────────────────
    @Override
    public LoginResponse register(RegisterRequest request) {
        if (adminUserRepository.findByUsername(
                request.getUsername()).isPresent()) {
            throw new RuntimeException("Utilisateur déjà existant");
        }

        AdminUser newUser = new AdminUser();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(
                passwordEncoder.encode(request.getPassword()));
        adminUserRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(newUser.getUsername())
                .message("Compte créé avec succès")
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────
    @Override
    public void logout(String username) {
        // JWT est stateless — le logout est géré côté Angular
        // en supprimant le token du localStorage
    }

    // ── Get Profile ───────────────────────────────────────────────
    @Override
    public AdminUserDTO getProfile(String username) {
        AdminUser user = adminUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        AdminUserDTO dto = new AdminUserDTO();
        dto.setUsername(user.getUsername());
        return dto;
    }

    // ── UserDetailsService (pour Spring Security) ─────────────────
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        AdminUser user = adminUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Utilisateur introuvable : " + username));

        return new User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>());
    }
}