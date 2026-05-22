package com.courseplatform.api.repository;

import com.courseplatform.api.model.Subtopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtopicRepository extends JpaRepository<Subtopic, String> {
	List<Subtopic> findByTopicId(String topicId);
}
