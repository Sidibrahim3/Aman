package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.request.AuthRequestDto;
import com.sidibrahim.Aman.dto.response.AuthResponseDto;
import com.sidibrahim.Aman.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto authRequestDto) {
        return ResponseEntity.ok(authService.login(authRequestDto));
    }

    // Admin can call this to generate a 6-digit password for the user
    @PostMapping("/reset-password/{userId}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<String> resetPassword(@PathVariable Long userId) {
        // The new password is generated and returned to the admin
        String newPassword = authService.resetPassword(userId);
        return ResponseEntity.ok("New password: " + newPassword);
    }
}
