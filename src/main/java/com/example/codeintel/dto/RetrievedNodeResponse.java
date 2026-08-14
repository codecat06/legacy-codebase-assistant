package com.example.codeintel.dto;

import com.example.codeintel.entity.CodeNode;

public record RetrievedNodeResponse(String fqn, String filePath, String sourceText) {
    public static RetrievedNodeResponse from(CodeNode node) {
        return new RetrievedNodeResponse(node.getFqn(), node.getFilePath(), node.getSourceText());
    }
}
