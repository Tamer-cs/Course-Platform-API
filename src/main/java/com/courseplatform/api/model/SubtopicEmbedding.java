package com.courseplatform.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "subtopic_embeddings", indexes = @Index(columnList = "subtopic_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtopicEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subtopic_id", nullable = false, length = 128)
    private String subtopicId;

    // Store embedding as JSON string via converter
    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = com.courseplatform.api.model.EmbeddingConverter.class)
    private float[] embeddingVector;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
