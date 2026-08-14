package com.example.codeintel.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.eclipse.jgit.api.Git;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GitCloneService {

    private final Path reposRoot;

    public GitCloneService(@Value("${codeintel.storage.repos-path}") String reposPath) {
        this.reposRoot = Path.of(reposPath).toAbsolutePath();
        this.reposRoot.toFile().mkdirs();
    }

    public Path clone(String remoteUrl, UUID repositoryId) {
        Path targetDir = reposRoot.resolve(repositoryId.toString());

        try {
            if (Files.exists(targetDir)) {
                log.info("Removing existing clone at {} before re-cloning", targetDir);
                deleteRecursively(targetDir);
            }

            log.info("Cloning {} into {}", remoteUrl, targetDir);

            try (Git git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(targetDir.toFile())
                    .setDepth(1)
                    .call()) {
                log.info("Clone finished: {}", targetDir);
                return targetDir;
            }
        } catch (Exception e) {
            throw new RuntimeException("Clone failed for " + remoteUrl, e);
        }
    }

    private void deleteRecursively(Path dir) throws IOException {
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    public File resolve(UUID repositoryId) {
        return reposRoot.resolve(repositoryId.toString()).toFile();
    }
}
