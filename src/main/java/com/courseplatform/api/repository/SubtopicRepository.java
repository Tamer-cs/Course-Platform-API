package com.courseplatform.api.repository;

import com.courseplatform.api.model.Subtopic;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtopicRepository extends JpaRepository<Subtopic, String> {
	List<Subtopic> findByTopicId(String topicId);

	@Query("select count(s) from Subtopic s where s.topic.course.id = :courseId")
	long countByCourseId(@Param("courseId") String courseId);
}
