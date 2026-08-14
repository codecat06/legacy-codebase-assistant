package com.example.codeintel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codeintel.entity.CodeNode;
import java.util.*;

public interface CodeNodeRepository extends JpaRepository<CodeNode, UUID> {

    List<CodeNode> findByFqn(String fqn);

    List<CodeNode> findByRepoId(UUID repoId);

}
