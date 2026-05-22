package com.courseplatform.api.mapper;

import com.courseplatform.api.dto.EnrollmentResponse;
import com.courseplatform.api.model.Enrollment;

public final class EnrollmentMapper {
    private EnrollmentMapper() {}

    public static EnrollmentResponse toDto(Enrollment e) {
        if (e == null) return null;
        return EnrollmentResponse.builder()
                .id(e.getId())
                .courseId(e.getCourse() != null ? e.getCourse().getId() : null)
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .enrolledAt(e.getEnrolledAt())
                .build();
    }
}
