package com.courseplatform.api.repository;

import com.courseplatform.api.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

	@Query(value = "SELECT c.id AS course_id, c.title AS course_title, t.title AS topic_title, s.id AS subtopic_id, s.title AS subtopic_title, s.content AS subtopic_content, " +
			"CASE " +
			"WHEN LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 300 " +
			"WHEN LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 200 " +
			"WHEN LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 150 " +
			"WHEN LOWER(s.content) LIKE LOWER(CONCAT('%', :q, '%')) THEN 100 " +
			"ELSE 0 END AS relevance_score " +
			"FROM courses c " +
			"LEFT JOIN topics t ON t.course_id = c.id " +
			"LEFT JOIN subtopics s ON s.topic_id = t.id " +
			"WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
			"OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
			"OR LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
			"OR LOWER(s.content) LIKE LOWER(CONCAT('%', :q, '%')) " +
			"ORDER BY relevance_score DESC, c.title ASC " +
			"LIMIT :limit", nativeQuery = true)
	List<Object[]> searchWeighted(@Param("q") String q, @Param("limit") int limit);
}
