package com.example.codeintel.controller;

import com.example.codeintel.dto.ImpactAnalysisResponse;
import com.example.codeintel.service.ImpactAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impact")
@RequiredArgsConstructor
public class ImpactAnalysisController {

    private final ImpactAnalysisService impactAnalysisService;

    @GetMapping
    public ResponseEntity<ImpactAnalysisResponse> impact(
            @RequestParam String fqn,
            @RequestParam(defaultValue = "UPSTREAM") String direction,
            @RequestParam(defaultValue = "3") int depth) {
        return ResponseEntity.ok(impactAnalysisService.analyze(fqn, direction, depth));
    }
}
