package com.example.codeintel.service;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
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
        log.info("Cloning {} into {}", remoteUrl, targetDir);

        try (Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(targetDir.toFile())
                .setDepth(1)
                .call()) {
            log.info("Clone finished: {}", targetDir);
            return targetDir;
        } catch (Exception e) {
            throw new RuntimeException("Clone failed for " + remoteUrl, e);
        }
    }

    public File resolve(UUID repositoryId) {
        return reposRoot.resolve(repositoryId.toString()).toFile();
    }

}