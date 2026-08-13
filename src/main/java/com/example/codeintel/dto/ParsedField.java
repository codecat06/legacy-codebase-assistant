package com.example.codeintel.dto;

import java.util.List;

public record ParsedField(String name, String type, List<String> annotations) {
}
