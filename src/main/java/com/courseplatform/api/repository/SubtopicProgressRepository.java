package com.courseplatform.api.repository;

import com.courseplatform.api.model.SubtopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubtopicProgressRepository extends JpaRepository<SubtopicProgress, Long> {
    Optional<SubtopicProgress> findByUserIdAndSubtopicId(Long userId, String subtopicId);
}
