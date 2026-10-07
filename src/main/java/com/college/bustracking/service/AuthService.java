package com.college.bustracking.service;

import com.college.bustracking.dto.CreateUserRequest;
import com.college.bustracking.dto.LoginRequest;
import com.college.bustracking.dto.LoginResponse;
import com.college.bustracking.dto.RefreshTokenRequest;
import com.college.bustracking.dto.SignupRequest;
import com.college.bustracking.dto.UserResponse;
import com.college.bustracking.entity.Role;
import com.college.bustracking.entity.User;
import com.college.bustracking.exception.ApiException;
import com.college.bustracking.mapper.EntityMapper;
import com.college.bustracking.repository.UserRepository;
import com.college.bustracking.security.UserPrincipal;
import com.college.bustracking.util.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EntityMapper mapper;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (request.getRole() != null && request.getRole() != Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only student signup is allowed publicly");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
        return mapper.toUser(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account is not registered as a " + request.getRole().name().toLowerCase());
        }
        return tokens(user);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        Claims claims = jwtService.parse(request.getRefreshToken());
        if (!jwtService.isRefreshToken(request.getRefreshToken())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        User user = userRepository.findByEmail(claims.get("email", String.class))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        return tokens(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        return mapper.toUser(userRepository.save(user));
    }

    private LoginResponse tokens(User user) {
        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name()))
                .refreshToken(jwtService.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().name()))
                .tokenType("Bearer")
                .user(mapper.toUser(user))
                .build();
    }
}
