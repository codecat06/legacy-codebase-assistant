package com.example.codeintel.service;

import com.example.codeintel.dto.ParsedFile;
import com.example.codeintel.entity.GitRepo;
import com.example.codeintel.entity.IngestionJob;
import com.example.codeintel.entity.IngestionStatus;
import com.example.codeintel.repository.GitRepoRepository;
import com.example.codeintel.repository.IngestionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionRunner {

    private final GitRepoRepository repositoryDao;
    private final IngestionJobRepository jobDao;
    private final GitCloneService cloneService;
    private final ParserClient parserClient;

    @Async
    public void run(UUID jobId, UUID repoId) {
        IngestionJob job = jobDao.findById(jobId).orElseThrow();
        GitRepo repo = repositoryDao.findById(repoId).orElseThrow();

        try {
            job.setStatus(IngestionStatus.CLONING);
            job.setStartedAt(Instant.now());
            jobDao.save(job);

            Path localPath = cloneService.clone(repo.getRemoteUrl(), repo.getId());
            repo.setLocalPath(localPath.toString());
            repositoryDao.save(repo);

            job.setStatus(IngestionStatus.PARSING);
            jobDao.save(job);

            parseRepository(localPath.toFile());

            job.setStatus(IngestionStatus.DONE);
            job.setFinishedAt(Instant.now());
            jobDao.save(job);

        } catch (Exception e) {
            log.error("Ingestion failed for job {}", jobId, e);
            job.setStatus(IngestionStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            jobDao.save(job);
        }
    }

    private void parseRepository(File repoDir) throws IOException {
        try (var paths = Files.walk(repoDir.toPath())) {
            List<Path> javaFiles = paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();

            log.info("Found {} Java files to parse in {}", javaFiles.size(), repoDir);

            for (Path path : javaFiles) {
                String content = Files.readString(path);
                String relativePath = repoDir.toPath().relativize(path).toString();

                ParsedFile parsed = parserClient.parse(relativePath, content);
                log.info("Parsed {}: {} class(es)", relativePath, parsed.classes().size());
            }
        }
    }
}
