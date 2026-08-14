package com.example.codeintel.service;

import com.example.codeintel.entity.CodeNode;
import com.example.codeintel.repository.CodeEdgeRepository;
import com.example.codeintel.repository.CodeNodeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryCleanupService {

    private final CodeNodeRepository nodeDao;
    private final CodeEdgeRepository edgeDao;
    private final QdrantClient qdrantClient;

    @Transactional
    public void cleanup(UUID repositoryId) {
        List<UUID> nodeIds = nodeDao.findByRepoId(repositoryId).stream()
                .map(CodeNode::getId)
                .toList();

        if (nodeIds.isEmpty()) {
            log.info("No existing graph data for repo {}, nothing to clean up", repositoryId);
            return;
        }

        edgeDao.deleteBySourceIdInOrTargetIdIn(nodeIds);
        nodeDao.deleteAllById(nodeIds);
        qdrantClient.deleteByRepository(repositoryId);

        log.info("Cleaned up {} old node(s) and their edges/vectors for repo {}", nodeIds.size(), repositoryId);
    }
}
