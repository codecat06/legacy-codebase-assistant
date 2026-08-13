package com.example.codeintel.service;

import com.example.codeintel.entity.GitRepo;
import com.example.codeintel.entity.IngestionJob;
import com.example.codeintel.entity.IngestionStatus;
import com.example.codeintel.repository.GitRepoRepository;
import com.example.codeintel.repository.IngestionJobRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final GitRepoRepository repositoryDao;
    private final IngestionJobRepository jobDao;
    private final IngestionRunner ingestionRunner;

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

        ingestionRunner.run(job.getId(), repo.getId());
        return job;
    }
}
