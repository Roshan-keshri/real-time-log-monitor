package com.roshan.logmonitor.service;

import com.roshan.logmonitor.dto.auth.LoginRequest;
import com.roshan.logmonitor.dto.auth.RegisterRequest;
import com.roshan.logmonitor.entity.Company;
import com.roshan.logmonitor.entity.User;
import com.roshan.logmonitor.repository.CompanyRepository;
import com.roshan.logmonitor.repository.UserRepository;
import com.roshan.logmonitor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        boolean isNewCompany = false;
        Company company = companyRepository.findByName(request.getCompanyName()).orElse(null);

        if (company == null) {
            company = new Company();
            company.setName(request.getCompanyName());
            company.setApiKey("log_" + UUID.randomUUID().toString().replace("-", ""));
            company = companyRepository.save(company);
            isNewCompany = true;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setCompany(company);

        userRepository.save(user);

        if (isNewCompany) {
            return "Registration successful! You created a new company. Your API Key is: " + company.getApiKey();
        } else {
            return "Registration successful! You have joined the existing company: " + company.getName();
        }
    }

    public String login(LoginRequest request) {
        // This checks if the password matches the database
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        return jwtUtil.generateToken(request.getUsername());
    }
}