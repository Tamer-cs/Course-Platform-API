package com.courseplatform.api.config;

import com.courseplatform.api.model.Course;
import com.courseplatform.api.model.Role;
import com.courseplatform.api.model.Subtopic;
import com.courseplatform.api.model.Topic;
import com.courseplatform.api.model.User;
import com.courseplatform.api.repository.CourseRepository;
import com.courseplatform.api.repository.SubtopicRepository;
import com.courseplatform.api.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private static final String FALLBACK_INSTRUCTOR_EMAIL = "seed.instructor@courseplatform.local";
    private static final String FALLBACK_INSTRUCTOR_NAME = "Course Platform Instructor";
    private static final String FALLBACK_INSTRUCTOR_PASSWORD = "seed-only-password";

    private final CourseRepository courseRepository;
    private final SubtopicRepository subtopicRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        long courseCount = courseRepository.count();
        if (courseCount != 0) {
            log.info("Skipping course seeding because {} courses already exist", courseCount);
            return;
        }

        ensureInstructorRoleAllowed();
        User fallbackInstructor = resolveFallbackInstructor();
        List<SeedCourse> seedCourses = loadSeedCourses();
        List<Subtopic> subtopicsToPersist = new ArrayList<>();

        for (SeedCourse seedCourse : seedCourses) {
            Course course = Course.builder()
                    .id(seedCourse.getId())
                    .title(seedCourse.getTitle())
                    .description(seedCourse.getDescription())
                    .owner(fallbackInstructor)
                    .build();

            if (seedCourse.getTopics() != null) {
                for (SeedTopic seedTopic : seedCourse.getTopics()) {
                    Topic topic = Topic.builder()
                            .id(seedTopic.getId())
                            .title(seedTopic.getTitle())
                            .description(seedTopic.getDescription())
                            .course(course)
                            .build();

                    course.getTopics().add(topic);

                    if (seedTopic.getSubtopics() != null) {
                        for (SeedSubtopic seedSubtopic : seedTopic.getSubtopics()) {
                            Subtopic subtopic = Subtopic.builder()
                                    .id(seedSubtopic.getId())
                                    .title(seedSubtopic.getTitle())
                                    .content(seedSubtopic.getContent())
                                    .topic(topic)
                                    .build();
                            subtopicsToPersist.add(subtopic);
                        }
                    }
                }
            }

            courseRepository.save(course);
        }

        subtopicRepository.saveAll(subtopicsToPersist);
        log.info("Seeded {} courses, {} topics, and {} subtopics", seedCourses.size(),
                seedCourses.stream().mapToInt(course -> course.getTopics() == null ? 0 : course.getTopics().size()).sum(),
                subtopicsToPersist.size());
    }

    private void ensureInstructorRoleAllowed() {
        try {
            entityManager.createNativeQuery("ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_role_check").executeUpdate();
            entityManager.createNativeQuery("ALTER TABLE user_roles ADD CONSTRAINT user_roles_role_check CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR'))").executeUpdate();
            log.info("Updated user_roles constraint to allow ROLE_INSTRUCTOR");
        } catch (Exception exception) {
            log.warn("Could not update user_roles constraint for ROLE_INSTRUCTOR; seeding may fail if the database still enforces the old enum set", exception);
        }
    }

    private User resolveFallbackInstructor() {
        return userRepository.findByEmail(FALLBACK_INSTRUCTOR_EMAIL)
                .map(existingUser -> {
                    if (existingUser.getRoles() == null) {
                        existingUser.setRoles(new HashSet<>());
                    }
                    if (existingUser.getRoles().add(Role.ROLE_INSTRUCTOR)) {
                        log.info("Added instructor role to existing fallback user {}", FALLBACK_INSTRUCTOR_EMAIL);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User createdUser = User.builder()
                            .fullName(FALLBACK_INSTRUCTOR_NAME)
                            .email(FALLBACK_INSTRUCTOR_EMAIL)
                            .password(FALLBACK_INSTRUCTOR_PASSWORD)
                            .roles(new HashSet<>(Set.of(Role.ROLE_INSTRUCTOR)))
                            .build();
                    log.info("Created fallback instructor user {}", FALLBACK_INSTRUCTOR_EMAIL);
                    return userRepository.save(createdUser);
                });
    }

    private List<SeedCourse> loadSeedCourses() throws IOException {
        Resource resource = new ClassPathResource("courses.json");
        if (!resource.exists()) {
            throw new IllegalStateException("Missing courses.json seed file in src/main/resources");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(
                    inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, SeedCourse.class)
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SeedCourse {
        private String id;
        private String title;
        private String description;
        private List<SeedTopic> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SeedTopic {
        private String id;
        private String title;
        private String description;
        private List<SeedSubtopic> subtopics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SeedSubtopic {
        private String id;
        private String title;
        private String content;
    }
}
