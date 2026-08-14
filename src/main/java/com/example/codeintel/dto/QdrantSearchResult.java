package com.example.codeintel.dto;

import java.util.Map;

public record QdrantSearchResult(String id, double score, Map<String, Object> payload) {
}
