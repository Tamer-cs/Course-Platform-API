package com.courseplatform.api.service;

import com.courseplatform.api.dto.EnrollmentResponse;
import com.courseplatform.api.exception.ConflictException;
import com.courseplatform.api.exception.ResourceNotFoundException;
import com.courseplatform.api.mapper.EnrollmentMapper;
import com.courseplatform.api.model.Course;
import com.courseplatform.api.model.Enrollment;
import com.courseplatform.api.model.User;
import com.courseplatform.api.repository.CourseRepository;
import com.courseplatform.api.repository.EnrollmentRepository;
import com.courseplatform.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public EnrollmentResponse joinCourse(String courseId) {
        User user = currentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId).isPresent()) {
            throw new ConflictException("User is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        try {
            return EnrollmentMapper.toDto(enrollmentRepository.save(enrollment));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ConflictException("User is already enrolled in this course");
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        String email = principal instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : String.valueOf(principal);

        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Authenticated user not found"));
    }
}