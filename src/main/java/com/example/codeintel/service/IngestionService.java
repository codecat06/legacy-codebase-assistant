package com.example.codeintel.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.codeintel.entity.GitRepo;
import com.example.codeintel.entity.IngestionJob;
import com.example.codeintel.entity.IngestionStatus;
import com.example.codeintel.repository.GitRepoRepository;
import com.example.codeintel.repository.IngestionJobRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {
    private final GitRepoRepository repositoryDao;
    private final IngestionJobRepository jobDao;
    private final GitCloneService cloneService;

    @Transactional
    public IngestionJob startIngestion(String name, String remoteUrl) {
        GitRepo repo = new GitRepo();
        repo.setName(name);
        repo.setRemoteUrl(remoteUrl);
        repo.setLocalPath("");
        repo = repositoryDao.save(repo);

        IngestionJob job = new IngestionJob();
        job.setRepo(repo);
        job.setStatus(IngestionStatus.PENDING);
        job = jobDao.save(job);

        runAsync(job.getId(), repo.getId());
        return job;
    }

    @Async
    private void runAsync(UUID jobId, UUID repoId) {
        // TODO Auto-generated method stub
        IngestionJob job = jobDao.findById(jobId).orElseThrow();
        GitRepo repo = repositoryDao.findById(repoId).orElseThrow();

        try {
            job.setStatus(IngestionStatus.CLONING);
            job.setStartedAt(Instant.now());
            jobDao.save(job);

            var localPath = cloneService.clone(repo.getRemoteUrl(), repo.getId());
            repo.setLocalPath(localPath.toString());
            repositoryDao.save(repo);
        } catch (Exception e) {
            log.error("Ingestion failed for job {}", jobId, e);
            job.setStatus(IngestionStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            jobDao.save(job);
        }
    }
}
