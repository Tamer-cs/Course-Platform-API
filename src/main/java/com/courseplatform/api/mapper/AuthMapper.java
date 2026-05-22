package com.courseplatform.api.mapper;

import com.courseplatform.api.dto.AuthResponse;

public final class AuthMapper {
    private AuthMapper() {}

    public static AuthResponse toDto(String token) {
        return AuthResponse.builder().token(token).build();
    }
}
