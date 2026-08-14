package com.example.codeintel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "code_node")
@Data
public class CodeNode {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "source_text", columnDefinition = "text")
    private String sourceText;

    @ManyToOne
    @JoinColumn(name = "repository_id")
    private GitRepo repo;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(nullable = false)
    private String fqn;

    @Column(name = "file_path", nullable = false)
    private String filePath;
}
