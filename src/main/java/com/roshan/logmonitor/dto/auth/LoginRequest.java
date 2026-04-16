package com.roshan.logmonitor.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // Change this from 'email' to 'username'
    private String password;
}