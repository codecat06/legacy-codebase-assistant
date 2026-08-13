package com.example.codeintel.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codeintel.entity.IngestionJob;

public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {

}
