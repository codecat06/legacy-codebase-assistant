package com.example.codeintel.entity;

public enum IngestionStatus {
    PENDING,
    CLONING,
    PARSING,
    GRAPH_BUILDING,
    EMBEDDING,
    DONE,
    FAILED
}
