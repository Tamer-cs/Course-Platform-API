package com.courseplatform.api.service;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import com.courseplatform.api.util.VectorMathUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class EmbeddingService {

    private static final String ENGINE_PROPERTY = "ai.djl.repository.zoo.default_engine";
    private static final String ONNX_ENGINE = "OnnxRuntime";
    private static final String MODEL_URL = "djl://ai.djl.huggingface.onnxruntime/TaylorAI/bge-micro-v2";
    private static final int EMBEDDING_DIMENSION = 384;

    private final AtomicReference<ZooModel<String, float[]>> modelRef = new AtomicReference<>();

    static {
        System.setProperty(ENGINE_PROPERTY, ONNX_ENGINE);
    }

    @PreDestroy
    public void shutdown() {
        ZooModel<String, float[]> model = modelRef.getAndSet(null);
        if (model != null) {
            model.close();
        }
    }

    public float[] generateEmbedding(String input) {
        String normalizedInput = normalizeInput(input);
        if (normalizedInput.isBlank()) {
            return new float[EMBEDDING_DIMENSION];
        }

        try {
            ZooModel<String, float[]> model = getOrLoadModel();
            if (model != null) {
                try (Predictor<String, float[]> predictor = model.newPredictor()) {
                    float[] embedding = predictor.predict(normalizedInput);
                    if (embedding != null && embedding.length > 0) {
                        VectorMathUtils.normalizeInPlace(embedding);
                        return embedding;
                    }
                    throw new IllegalStateException("Embedding model returned an empty vector");
                }
            }
        } catch (Exception exception) {
            log.warn(
                    "DJL semantic embedding failed; using deterministic fallback vector instead so startup and seeding continue: {}",
                    exception.getMessage());
            log.debug("Embedding inference failure", exception);
        }

        return buildDeterministicFallback(normalizedInput);
    }

    public byte[] generateEmbeddingBytes(String input) {
        return VectorMathUtils.toByteArray(generateEmbedding(input));
    }

    private ZooModel<String, float[]> getOrLoadModel() {
        ZooModel<String, float[]> cached = modelRef.get();
        if (cached != null) {
            return cached;
        }

        synchronized (modelRef) {
            cached = modelRef.get();
            if (cached != null) {
                return cached;
            }

            try {
                Criteria<String, float[]> criteria =
                        Criteria.builder()
                                .setTypes(String.class, float[].class)
                                .optApplication(ai.djl.Application.NLP.TEXT_EMBEDDING)
                                .optModelUrls(MODEL_URL)
                                .optEngine(ONNX_ENGINE)
                                .build();

                ZooModel<String, float[]> model = criteria.loadModel();
                modelRef.set(model);
                log.info("Loaded ONNX semantic embedding model {}", MODEL_URL);
                return model;
            } catch (Exception exception) {
                log.warn(
                        "Unable to initialize ONNX semantic embedding model; deterministic fallback will be used: {}",
                        exception.getMessage());
                log.debug("Model initialization failure", exception);
                return null;
            }
        }
    }

    private String normalizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    private float[] buildDeterministicFallback(String text) {
        float[] vector = new float[EMBEDDING_DIMENSION];
        if (text == null || text.isBlank()) {
            return vector;
        }

        byte[] seed = digest(text);
        for (int index = 0; index < vector.length; index++) {
            int left = seed[index % seed.length] & 0xFF;
            int right = seed[(index + 11) % seed.length] & 0xFF;
            int combined = (left << 8) | right;
            vector[index] = ((combined % 2000) / 1000.0f) - 1.0f;
        }

        VectorMathUtils.normalizeInPlace(vector);
        return vector;
    }

    private byte[] digest(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            byte[] raw = text.getBytes(StandardCharsets.UTF_8);
            byte[] truncated = new byte[Math.min(32, raw.length)];
            System.arraycopy(raw, 0, truncated, 0, truncated.length);
            return truncated;
        }
    }
}