package com.example.codeintel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

@Service
public class ChatClient {
    private final RestClient restClient;
    private final String model;

    public ChatClient(
            @Value("${codeintel.ollama.base-url}") String baseUrl,
            @Value("${codeintel.ollama.chat-model}") String model) {
        this.restClient = RestClient.create(baseUrl);
        this.model = model;
    }

    public String chat(String systemPrompt, String userPrompt) {
        ChatResponse response = restClient.post().uri("/api/chat").body(new ChatRequest(model, List.of(
                new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userPrompt)), false)).retrieve().body(ChatResponse.class);

        return response.message().content();
    }

    private record ChatMessage(String role, String content) {
    }

    private record ChatRequest(String model, List<ChatMessage> messages, boolean stream) {
    }

    private record ChatResponse(ChatMessage message) {
    }
}
