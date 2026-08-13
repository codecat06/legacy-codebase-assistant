package com.example.codeintel.dto;

import java.util.List;

public record ParsedMethod(
        String name,
        String returnType,
        List<String> annotations,
        List<ParsedParam> params,
        List<MethodCallRef> calls) {
}
