package com.example.codeintel.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Collate;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "repository")
@Data
public class GitRepo {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "remote_url", nullable = false)
    private String remoteUrl;

    @Column(name = "local_path", nullable = false)
    private String localPath;

    @Column(name = "last_indexed_commit")
    private String lastIndexedCommit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
