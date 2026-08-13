package com.example.codeintel.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codeintel.entity.CodeEdge;

public interface CodeEdgeRepository extends JpaRepository<CodeEdge, UUID> {

}
