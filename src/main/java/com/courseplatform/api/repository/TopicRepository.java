package com.courseplatform.api.repository;

import com.courseplatform.api.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {
	List<Topic> findByCourseId(String courseId);
}
