package com.example.codeintel.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.codeintel.entity.CodeEdge;
import com.example.codeintel.repository.CodeEdgeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphTraversalService {
    private final CodeEdgeRepository edgeDao;

    public Set<UUID> expand(Set<UUID> seedIds, int maxHops) {
        Set<UUID> visited = new HashSet<>(seedIds);
        Set<UUID> frontier = new HashSet<>(seedIds);

        for (int hop = 0; hop < maxHops; hop++) {
            Set<UUID> next = new HashSet<>();

            for (UUID nodeId : frontier) {
                for (CodeEdge edge : edgeDao.findBySourceId(nodeId)) {
                    next.add(edge.getTarget().getId());
                }

                for (CodeEdge edge : edgeDao.findByTargetId(nodeId)) {
                    next.add(edge.getSource().getId());
                }
            }

            next.removeAll(visited);
            if (next.isEmpty())
                break;

            visited.addAll(next);
            frontier = next;

        }
        return visited;
    }
}
