package com.example.codeintel.dto;

import java.util.List;

public record ParsedClass(
                String name,
                List<String> annotations,
                List<ParsedField> fields,
                List<ParsedMethod> methods) {
}