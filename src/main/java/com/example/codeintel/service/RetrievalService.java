package com.example.codeintel.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.codeintel.dto.QdrantSearchResult;
import com.example.codeintel.entity.CodeNode;
import com.example.codeintel.repository.CodeNodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;
    private final GraphTraversalService graphTraversalService;
    private final CodeNodeRepository nodeDao;

    public List<CodeNode> retrieve(String question, int topK, int hops) {
        List<Double> queryVector = embeddingClient.embed(question);

        List<QdrantSearchResult> hits = qdrantClient.search(queryVector, topK);

        Set<UUID> seedIds = hits.stream().map(hit -> UUID.fromString(hit.id())).collect(Collectors.toSet());

        Set<UUID> expandedIds = graphTraversalService.expand(seedIds, hops);

        return nodeDao.findAllById(expandedIds);
    }
}
