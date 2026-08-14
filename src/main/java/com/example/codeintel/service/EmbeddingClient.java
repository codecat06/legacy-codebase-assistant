package com.example.codeintel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

@Service
public class EmbeddingClient {
    private final RestClient restClient;
    private final String model;

    public EmbeddingClient(@Value("${codeintel.ollama.base-url}") String baseUrl,
            @Value("${codeintel.ollama.embedding-model}") String model) {
        this.restClient = RestClient.create(baseUrl);
        this.model = model;
    }

    public List<Double> embed(String text) {
        EmbeddingResponse response = restClient.post().uri("/api/embeddings").body(new EmbeddingRequest(model, text))
                .retrieve().body(EmbeddingResponse.class);

        return response.embedding();
    }

    private record EmbeddingRequest(String model, String prompt) {
    }

    private record EmbeddingResponse(List<Double> embedding) {
    }
}
