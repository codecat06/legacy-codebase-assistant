package com.example.codeintel.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codeintel.entity.GitRepo;

public interface GitRepoRepository extends JpaRepository<GitRepo, UUID> {

}
