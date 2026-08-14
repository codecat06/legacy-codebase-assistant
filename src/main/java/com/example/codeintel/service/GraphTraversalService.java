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

    private Set<UUID> directionalExpand(Set<UUID> seedIds, int maxHops, boolean upstream) {
        Set<UUID> visited = new HashSet<>(seedIds);
        Set<UUID> frontier = new HashSet<>(seedIds);

        for (int hop = 0; hop < maxHops; hop++) {
            Set<UUID> next = new HashSet<>();

            for (UUID nodeId : frontier) {
                if (upstream) {
                    for (CodeEdge edge : edgeDao.findByTargetId(nodeId)) {
                        next.add(edge.getSource().getId()); // bunu kim çağırıyor
                    }
                } else {
                    for (CodeEdge edge : edgeDao.findBySourceId(nodeId)) {
                        next.add(edge.getTarget().getId()); // bu neyi çağırıyor
                    }
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

    public Set<UUID> expandUpstream(Set<UUID> seedIds, int maxHops) {
        return directionalExpand(seedIds, maxHops, true);
    }

    public Set<UUID> expandDownstream(Set<UUID> seedIds, int maxHops) {
        return directionalExpand(seedIds, maxHops, false);
    }
}
