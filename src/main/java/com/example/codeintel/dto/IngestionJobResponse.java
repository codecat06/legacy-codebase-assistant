package com.example.codeintel.dto;

import com.example.codeintel.entity.IngestionJob;

import java.util.UUID;

public record IngestionJobResponse(UUID jobId, UUID repositoryId, String status, String errorMessage) {

    public static IngestionJobResponse from(IngestionJob job) {
        return new IngestionJobResponse(
                job.getId(),
                job.getRepo().getId(),
                job.getStatus().name(),
                job.getErrorMessage());
    }
}
