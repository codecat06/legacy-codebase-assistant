package com.example.codeintel.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.codeintel.dto.ParsedFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

@Service
public class ParserClient {
    private final RestClient restClient;

    public ParserClient(@Value("${codeintel.parser.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public ParsedFile parse(String filePath, String content) {
        return restClient.post().uri("/parse").contentType(MediaType.APPLICATION_JSON)
                .body(new ParseRequest(filePath, content)).retrieve().body(ParsedFile.class);
    }

    private record ParseRequest(String filePath, String content) {
    }

}
