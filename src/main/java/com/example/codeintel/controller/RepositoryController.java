package com.example.codeintel.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeintel.dto.CreateRepositoryRequest;
import com.example.codeintel.dto.IngestionJobResponse;
import com.example.codeintel.entity.IngestionJob;
import com.example.codeintel.repository.IngestionJobRepository;
import com.example.codeintel.service.IngestionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repos")
public class RepositoryController {
    private final IngestionService ingestionService;
    private final IngestionJobRepository jobDao;

    @PostMapping
    public ResponseEntity<IngestionJobResponse> createRepository(@RequestBody CreateRepositoryRequest request) {
        IngestionJob job = ingestionService.startIngestion(request.name(), request.remoteUrl());
        return ResponseEntity.ok(IngestionJobResponse.from(job));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<IngestionJobResponse> getJobStatus(@PathVariable UUID jobId) {
        IngestionJob job = jobDao.findById(jobId).orElseThrow();
        return ResponseEntity.ok(IngestionJobResponse.from(job));
    }
}
