package com.jobagent.repository;

import com.jobagent.entity.Job;
import com.jobagent.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsBySourceAndExternalId(String source, String externalId);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByStatusOrderByMatchScoreDesc(JobStatus status);

    List<Job> findTop20ByStatusOrderByMatchScoreDesc(JobStatus status);
}

