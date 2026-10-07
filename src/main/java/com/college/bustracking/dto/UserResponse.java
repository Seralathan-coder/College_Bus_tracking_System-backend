package com.college.bustracking.dto;

import com.college.bustracking.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Instant createdAt;
}
