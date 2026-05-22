package com.courseplatform.api.mapper;

import com.courseplatform.api.dto.AuthRegisterRequest;
import com.courseplatform.api.model.User;

public class UserMapper {
    public static User fromRegisterRequest(AuthRegisterRequest req) {
        if (req == null) return null;
        return User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(req.getPassword())
                .build();
    }
}
