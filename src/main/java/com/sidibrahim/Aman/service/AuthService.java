package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.request.AuthRequestDto;
import com.sidibrahim.Aman.dto.response.AuthResponseDto;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.repository.UserRepository;
import com.sidibrahim.Aman.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;
    }
    public AuthResponseDto login(AuthRequestDto authRequestDto) {
        AuthResponseDto authResponseDto = new AuthResponseDto();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDto.getPhoneNumber(), authRequestDto.getPassword()));

        if (authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);
            User user = (User) userDetails;
            authResponseDto.setUserId(user.getId());
            authResponseDto.setJwt(jwt);
        }
        return authResponseDto;
    }
    @Transactional
    // Method to reset the user's password and return the new password
    public String resetPassword(Long userId) {
        // Generate a new 6-digit numeric password
        String newPassword = generateSixDigitPassword();

        // Fetch the user from the repository (you can handle exceptions if the user is not found)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GenericException("User not found"));

        // Set the new password (it must be encoded before saving)
        user.setPassword(passwordEncoder.encode(newPassword));
        if (!user.isEnabled()) user.setEnabled(true);

        // Save the updated user entity
        userRepository.save(user);

        // Return the new password to the admin (this will be shown in the admin panel)
        return newPassword;
    }

    // Helper method to generate a random 6-digit password
    private String generateSixDigitPassword() {
        SecureRandom random = new SecureRandom();
        int password = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(password);
    }
}
