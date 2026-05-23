package com.courseplatform.api.service;

import com.courseplatform.api.dto.ProgressResponse;
import com.courseplatform.api.exception.NotEnrolledException;
import com.courseplatform.api.exception.ResourceNotFoundException;
import com.courseplatform.api.model.Course;
import com.courseplatform.api.model.Subtopic;
import com.courseplatform.api.model.SubtopicProgress;
import com.courseplatform.api.model.User;
import com.courseplatform.api.repository.EnrollmentRepository;
import com.courseplatform.api.repository.SubtopicProgressRepository;
import com.courseplatform.api.repository.SubtopicRepository;
import com.courseplatform.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final SubtopicProgressRepository subtopicProgressRepository;
    private final SubtopicRepository subtopicRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProgressResponse completeSubtopic(String subtopicId) {
        User user = currentUser();
        Subtopic subtopic = subtopicRepository.findById(subtopicId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtopic not found with id: " + subtopicId));

        Course course = subtopic.getTopic().getCourse();
        if (enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId()).isEmpty()) {
            throw new NotEnrolledException("User must be enrolled in the course before completing subtopics");
        }

        SubtopicProgress progress = subtopicProgressRepository.findByUserIdAndSubtopicId(user.getId(), subtopicId)
                .orElseGet(() -> subtopicProgressRepository.save(SubtopicProgress.builder()
                        .user(user)
                        .subtopic(subtopic)
                        .build()));

        return ProgressResponse.builder()
                .id(progress.getId())
                .subtopicId(subtopic.getId())
                .userId(user.getId())
                .courseId(course.getId())
                .completedAt(ISO_FORMATTER.format(progress.getCompletedAt()))
                .completionPercentage(formatCompletionPercentage(user.getId(), course.getId()))
                .build();
    }

    @Transactional(readOnly = true)
    public String getCourseProgressPercentage(Long userId, String courseId) {
        return formatCompletionPercentage(userId, courseId);
    }

    private String formatCompletionPercentage(Long userId, String courseId) {
        long totalSubtopics = subtopicRepository.countByCourseId(courseId);
        if (totalSubtopics == 0L) {
            return "0.00";
        }

        long completedSubtopics = subtopicProgressRepository.countCompletedSubtopicsByUserIdAndCourseId(userId, courseId);
        BigDecimal percentage = BigDecimal.valueOf(completedSubtopics)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSubtopics), 2, RoundingMode.HALF_UP);
        return percentage.setScale(2, RoundingMode.HALF_UP).toPlainString();
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