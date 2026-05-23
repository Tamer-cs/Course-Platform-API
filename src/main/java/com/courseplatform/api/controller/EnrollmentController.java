package com.courseplatform.api.controller;

import com.courseplatform.api.dto.EnrollmentResponse;
import com.courseplatform.api.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/join/{courseId}")
    public ResponseEntity<EnrollmentResponse> joinCourse(@PathVariable String courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.joinCourse(courseId));
    }
}