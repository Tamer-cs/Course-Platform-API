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

	// Fuzzy trigram similarity fallback. Returns similarity score as the last column.
	@Query(value = "SELECT c.id AS course_id, c.title AS course_title, t.title AS topic_title, s.id AS subtopic_id, s.title AS subtopic_title, s.content AS subtopic_content, " +
			"GREATEST( COALESCE(similarity(LOWER(c.title), LOWER(:q)),0), COALESCE(similarity(LOWER(t.title), LOWER(:q)),0), COALESCE(similarity(LOWER(s.title), LOWER(:q)),0), COALESCE(similarity(LOWER(s.content), LOWER(:q)),0) ) AS similarity_score " +
			"FROM courses c " +
			"LEFT JOIN topics t ON t.course_id = c.id " +
			"LEFT JOIN subtopics s ON s.topic_id = t.id " +
			"WHERE similarity(LOWER(c.title), LOWER(:q)) > 0.15 " +
			"OR similarity(LOWER(t.title), LOWER(:q)) > 0.15 " +
			"OR similarity(LOWER(s.title), LOWER(:q)) > 0.15 " +
			"OR similarity(LOWER(s.content), LOWER(:q)) > 0.15 " +
			"ORDER BY similarity_score DESC, c.title ASC " +
			"LIMIT :limit", nativeQuery = true)
	List<Object[]> searchFuzzy(@Param("q") String q, @Param("limit") int limit);
}
