package com.example.codeintel.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.codeintel.entity.CodeEdge;

public interface CodeEdgeRepository extends JpaRepository<CodeEdge, UUID> {

    List<CodeEdge> findBySourceId(UUID sourceId);

    List<CodeEdge> findByTargetId(UUID targetId);

    @Modifying
    @Query("delete from CodeEdge e where e.source.id in :nodeIds or e.target.id in :nodeIds")
    void deleteBySourceIdInOrTargetIdIn(@Param("nodeIds") List<UUID> nodeIds);
}
