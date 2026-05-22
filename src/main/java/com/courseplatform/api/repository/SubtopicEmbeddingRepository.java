package com.courseplatform.api.repository;

import com.courseplatform.api.model.SubtopicEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubtopicEmbeddingRepository extends JpaRepository<SubtopicEmbedding, Long> {
    Optional<SubtopicEmbedding> findBySubtopicId(String subtopicId);
}
