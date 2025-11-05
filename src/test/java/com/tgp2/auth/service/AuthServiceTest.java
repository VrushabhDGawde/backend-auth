package com.tgp2.auth.service;
import com.tgp2.auth.dto.*;
import com.tgp2.auth.entity.RefreshToken;
import com.tgp2.auth.entity.User;
import com.tgp2.auth.exception.TokenRefreshException;
import com.tgp2.auth.repository.UserRepository;
import com.tgp2.auth.security.JwtUtils;
import com.tgp2.auth.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_Success() {
        // Arrange
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123","Student");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        // Act
        String response = authService.registerUser(request);

        // Assert
        assertEquals("User registered successfully!", response);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Arrange
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123","Student");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.registerUser(request));

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Arrange
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123","Student");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.registerUser(request));

        assertEquals("Username already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_PasswordIsEncoded() {
        // Arrange
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123","Student");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        // Act
        authService.registerUser(request);

        // Assert
        verify(passwordEncoder).encode("password123");
    }


    //---------------------------------Login Service-------------------------------------//
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;


    @Test
    void loginUser_Success() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        UserDetailsImpl userDetails =
                new UserDetailsImpl(1L, "testuser", "test@example.com", "encodedPass", "Student", null);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtUtils.generateJwtToken(userDetails)).thenReturn("jwt-token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setId(100L);
        mockRefreshToken.setToken("refresh-token");
        mockRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.createRefreshToken(1L)).thenReturn(mockRefreshToken);

        // Act
        ResponseEntity<?> response = authService.loginUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);

        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("jwt-token", jwtResponse.getAccessToken());
        assertEquals("refresh-token", jwtResponse.getRefreshToken());
        assertEquals("Bearer", jwtResponse.getTokenType());

        verify(jwtUtils).generateJwtToken(userDetails);
        verify(refreshTokenService).createRefreshToken(1L);
    }

    @Test
    void loginUser_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("wrong@example.com", "badpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act
        ResponseEntity<?> response = authService.loginUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());

        verify(jwtUtils, never()).generateJwtToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @Test
    void loginUser_JwtAndRefreshTokenGenerated() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "encodedPass","Student" ,null);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(userDetails)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(1L))
                .thenReturn(new RefreshToken(1L, userDetails.getId().toString(), Instant.parse("2025-09-25T10:15:30Z"), null));

        // Act
        authService.loginUser(loginRequest);

        // Assert
        verify(jwtUtils, times(1)).generateJwtToken(userDetails);
        verify(refreshTokenService, times(1)).createRefreshToken(1L);
    }


    //---------------------------------Get RefreshToken---------------------------//

    @Test
    void getRefreshToken_ValidToken() {
        // Arrange
        String requestRefreshToken = "valid-refresh-token";

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken(requestRefreshToken);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshToken.setUser(user);

        when(refreshTokenService.findByToken(requestRefreshToken)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtUtils.generateTokenFromEmail("test@example.com")).thenReturn("new-jwt-token");

        // Act
        ResponseEntity<?> response = authService.getRefreshToken(requestRefreshToken);

        // Assert
        assertTrue(response.getBody() instanceof TokenRefreshResponse);

        TokenRefreshResponse tokenResponse = (TokenRefreshResponse) response.getBody();
        assertEquals("new-jwt-token", tokenResponse.getAccessToken());
        assertEquals(requestRefreshToken, tokenResponse.getRefreshToken());

        verify(jwtUtils).generateTokenFromEmail("test@example.com");
    }

    @Test
    void getRefreshToken_TokenNotFound() {
        // Arrange
        String requestRefreshToken = "missing-token";
        when(refreshTokenService.findByToken(requestRefreshToken)).thenReturn(Optional.empty());

        // Act & Assert
        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> authService.getRefreshToken(requestRefreshToken));
        assertEquals("Failed for ["+requestRefreshToken+"]: Refresh token is not in database!", exception.getMessage());
        verify(jwtUtils, never()).generateTokenFromEmail(anyString());
    }

    @Test
    void getRefreshToken_TokenExpired() {
        // Arrange
        String requestRefreshToken = "expired-token";

        User user = new User();
        user.setEmail("test@example.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(requestRefreshToken);
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60)); // expired
        refreshToken.setUser(user);

        when(refreshTokenService.findByToken(requestRefreshToken)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenThrow(new TokenRefreshException(requestRefreshToken, "Refresh token expired"));

        // Act & Assert
        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> authService.getRefreshToken(requestRefreshToken));
        assertEquals("Failed for ["+requestRefreshToken+"]: Refresh token expired", exception.getMessage());
        verify(jwtUtils, never()).generateTokenFromEmail(anyString());
    }

    //----------------------------------Logout user ----------------------//
    @Test
    void logoutUser_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "test@example.com", "encodedPass", "Student",null
        );
        userDetails.setUser(user); // assuming you added this to your UserDetailsImpl

        // Act
        ResponseEntity<ApiResponse> response = authService.logoutUser(userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logged out successfully", response.getBody().getMessage());

        verify(refreshTokenService, times(1)).deleteByUser(user);
    }

    @Test
    void logoutUser_UserNotAuthenticated() {
        // Act
        ResponseEntity<ApiResponse> response = authService.logoutUser(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not authenticated", response.getBody(). getMessage());

        verify(refreshTokenService, never()).deleteByUser(any(User.class));
    }
}