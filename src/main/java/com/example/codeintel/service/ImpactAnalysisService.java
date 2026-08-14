package com.example.codeintel.service;

import java.util.*;
import org.springframework.stereotype.Service;

import com.example.codeintel.dto.ImpactAnalysisResponse;
import com.example.codeintel.entity.CodeNode;
import com.example.codeintel.repository.CodeNodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImpactAnalysisService {

    private final CodeNodeRepository nodeDao;
    private final GraphTraversalService traversalService;

    public ImpactAnalysisResponse analyze(String fqn, String direction, int depth) {
        List<CodeNode> matches = nodeDao.findByFqn(fqn);
        if (matches.isEmpty()) {
            throw new NoSuchElementException("No node found for fqn: " + fqn);
        }

        CodeNode root = matches.get(0);
        Set<UUID> seedIds = Set.of(root.getId());

        Set<UUID> affectedIds = switch (direction.toUpperCase()) {
            case "UPSTREAM" -> traversalService.expandUpstream(seedIds, depth);
            case "DOWNSTREAM" -> traversalService.expandDownstream(seedIds, depth);
            default -> traversalService.expand(seedIds, depth);
        };

        affectedIds.remove(root.getId());

        List<String> affectedFqns = nodeDao.findAllById(affectedIds).stream().map(CodeNode::getFqn).sorted().toList();

        return new ImpactAnalysisResponse(fqn, direction.toUpperCase(), depth, affectedFqns);
    }

}
