package com.courseplatform.api.repository;

import com.courseplatform.api.model.SubtopicProgress;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubtopicProgressRepository extends JpaRepository<SubtopicProgress, Long> {
    Optional<SubtopicProgress> findByUserIdAndSubtopicId(Long userId, String subtopicId);

    @Query("select count(distinct sp.subtopic.id) from SubtopicProgress sp where sp.user.id = :userId and sp.subtopic.topic.course.id = :courseId")
    long countCompletedSubtopicsByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") String courseId);
}
