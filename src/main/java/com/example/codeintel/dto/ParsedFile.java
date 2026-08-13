package com.example.codeintel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ParsedFile(
        String file,
        @JsonProperty("package") String packageName,
        List<String> imports,
        List<ParsedClass> classes) {
}
