package com.courseplatform.api.controller;

import com.courseplatform.api.dto.SearchQueryResultDTO;
import com.courseplatform.api.service.EmbeddingService;
import com.courseplatform.api.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final EmbeddingService embeddingService;

    @GetMapping
        @Operation(
            summary = "Keyword search",
            description = "Runs the weighted keyword search first and falls back to fuzzy trigram matching when no exact results are found.")
    public ResponseEntity<List<SearchQueryResultDTO>> search(@RequestParam(name = "q") String q,
                                                              @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.search(q, limit));
    }

    @GetMapping("/semantic")
        @Operation(
            summary = "Semantic search",
            description = "Converts the query into an embedding vector and ranks stored subtopics by cosine similarity.")
    public ResponseEntity<List<SearchQueryResultDTO>> semanticSearch(@RequestParam(name = "q") String q,
                                                                      @RequestParam(name = "limit", required = false, defaultValue = "3") int limit) {
        float[] queryVector = embeddingService.generateEmbedding(q);
        return ResponseEntity.ok(searchService.performSemanticSearch(queryVector, limit));
    }

        @GetMapping("/hybrid")
        @Operation(
            summary = "Hybrid search",
            description = "Combines normalized keyword and semantic scores, deduplicates by subtopicId, and returns the strongest ranked results.")
        public ResponseEntity<List<SearchQueryResultDTO>> hybridSearch(
            @Parameter(description = "Search phrase to evaluate against keyword and semantic indexes", example = "velocity and motion")
            @RequestParam(name = "q") String q,
            @Parameter(description = "Maximum number of results to return", example = "10")
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.hybridSearch(q, limit));
        }
}
