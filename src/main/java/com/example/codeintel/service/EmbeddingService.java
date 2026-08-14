package com.example.codeintel.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.codeintel.dto.ParsedClass;
import com.example.codeintel.dto.ParsedFile;
import com.example.codeintel.dto.ParsedMethod;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
    private static final int VECTOR_SIZE = 768;

    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;

    public void embedAll(UUID repositoryId, List<ParsedFile> files, Map<String, UUID> methodNodeIdsByFqn) {
        qdrantClient.ensureCollection(VECTOR_SIZE);

        int embedded = 0;
        int skipped = 0;

        for (ParsedFile file : files) {
            for (ParsedClass cls : file.classes()) {
                String classFqn = buildFqn(file.packageName(), cls.name());

                for (ParsedMethod method : cls.methods()) {
                    String methodFqn = classFqn + "." + method.name();
                    UUID nodeId = methodNodeIdsByFqn.get(methodFqn);

                    if (nodeId == null || method.sourceText() == null || method.sourceText().isBlank()) {
                        skipped++;
                        continue;
                    }

                    List<Double> vector = embeddingClient.embed(method.sourceText());

                    qdrantClient.upsert(nodeId, vector, Map.of(
                            "repositoryId", repositoryId.toString(),
                            "fqn", methodFqn,
                            "filePath", file.file(),
                            "startLine", method.startLine()));

                    embedded++;
                }

            }

        }
    }

    private String buildFqn(String packageName, String className) {
        return (packageName == null || packageName.isBlank()) ? className : packageName + "." + className;
    }

}
