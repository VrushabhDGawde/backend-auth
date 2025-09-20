package com.tgp2.auth.service;

import com.tgp2.auth.dto.*;
import com.tgp2.auth.entity.RefreshToken;
import com.tgp2.auth.entity.User;
import com.tgp2.auth.exception.TokenRefreshException;
import com.tgp2.auth.repository.UserRepository;
import com.tgp2.auth.security.JwtUtils;
import com.tgp2.auth.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public String registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        return "User registered successfully!";
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtils.generateJwtToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            JwtResponse resp =  JwtResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .build();
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }

    }

    public ResponseEntity<?> getRefreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromEmail(user.getEmail());
                    TokenRefreshResponse response = new TokenRefreshResponse(token, requestRefreshToken);
                    return ResponseEntity.ok(response);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    public ResponseEntity<ApiResponse> logoutUser(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "User not authenticated"));
        }
        refreshTokenService.deleteByUser(userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }
}
