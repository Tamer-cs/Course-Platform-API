package com.courseplatform.api.service;

import com.courseplatform.api.dto.CourseResponse;
import com.courseplatform.api.dto.TopicResponse;
import com.courseplatform.api.exception.ResourceNotFoundException;
import com.courseplatform.api.mapper.CourseMapper;
import com.courseplatform.api.model.Course;
import com.courseplatform.api.model.Subtopic;
import com.courseplatform.api.model.Topic;
import com.courseplatform.api.repository.CourseRepository;
import com.courseplatform.api.repository.SubtopicRepository;
import com.courseplatform.api.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final SubtopicRepository subtopicRepository;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        List<TopicResponse> topicResponses = topicRepository.findByCourseId(id).stream()
                .map(this::mapTopicWithSubtopics)
                .collect(Collectors.toList());

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .topics(topicResponses)
                .build();
    }

    private TopicResponse mapTopicWithSubtopics(Topic topic) {
        List<Subtopic> subtopics = subtopicRepository.findByTopicId(topic.getId());
        return CourseMapper.toDto(topic, subtopics);
    }
}