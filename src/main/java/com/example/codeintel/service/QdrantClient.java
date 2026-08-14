package com.example.codeintel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
public class QdrantClient {

    private final RestClient restClient;
    private final String collectionName;

    public QdrantClient(
            @Value("${codeintel.qdrant.base-url}") String baseUrl,
            @Value("${codeintel.qdrant.collection}") String collectionName) {
        this.restClient = RestClient.create(baseUrl);
        this.collectionName = collectionName;
    }

    public void ensureCollection(int vectorSize) {
        try {
            restClient.put()
                    .uri("/collections/{name}", collectionName)
                    .body(Map.of("vectors", Map.of("size", vectorSize, "distance", "Cosine")))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.debug("Collection '{}' may already exist: {}", collectionName, e.getMessage());
        }
    }

    public void upsert(UUID pointId, List<Double> vector, Map<String, Object> payload) {
        restClient.put()
                .uri("/collections/{name}/points", collectionName)
                .body(Map.of("points", List.of(Map.of(
                        "id", pointId.toString(),
                        "vector", vector,
                        "payload", payload))))
                .retrieve()
                .toBodilessEntity();
    }
}
