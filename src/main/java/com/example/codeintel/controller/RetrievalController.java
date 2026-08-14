package com.example.codeintel.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeintel.dto.RetrievedNodeResponse;
import com.example.codeintel.entity.CodeNode;
import com.example.codeintel.service.RetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/retrieve")
public class RetrievalController {
    private final RetrievalService retrievalService;

    @PostMapping
    public ResponseEntity<List<RetrievedNodeResponse>> retrieve(@RequestBody RetrieveRequest request) {
        List<CodeNode> context = retrievalService.retrieve(request.question, request.topK() > 0 ? request.topK() : 5,
                request.hops() > 0 ? request.hops() : 2);

        return ResponseEntity.ok(context.stream().map(RetrievedNodeResponse::from).toList());
    }

    private record RetrieveRequest(String question, int topK, int hops) {
    }
}
