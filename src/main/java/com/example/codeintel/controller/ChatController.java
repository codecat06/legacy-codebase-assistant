package com.example.codeintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.codeintel.dto.AskResponse;
import com.example.codeintel.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ask")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) {
        AskResponse response = chatService.ask(request.question, request.topK() > 0 ? request.topK() : 5,
                request.hops() > 0 ? request.hops() : 2);

        return ResponseEntity.ok(response);
    }

    private record AskRequest(String question, int topK, int hops) {
    }
}
