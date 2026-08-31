package com.jobagent.service;

import com.jobagent.discovery.JobScraper;
import com.jobagent.entity.Job;
import com.jobagent.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates all registered JobScrapers on a schedule.
 * Deduplicates by (source + externalId) before saving.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobDiscoveryService {

    private final List<JobScraper> scrapers;  // Spring injects ALL implementations
    private final JobRepository jobRepository;

    /**
     * Runs every hour. Also callable manually via the controller.
     */
    @Scheduled(fixedRateString = "${scraper.interval-ms:3600000}")
    public int discoverJobs() {
        int totalNew = 0;

        for (JobScraper scraper : scrapers) {
            log.info("Running scraper: {}", scraper.getSource());
            try {
                List<Job> jobs = scraper.scrape();
                for (Job job : jobs) {
                    if (!jobRepository.existsBySourceAndExternalId(job.getSource(), job.getExternalId())) {
                        jobRepository.save(job);
                        totalNew++;
                        log.debug("Saved: {} at {} [{}]", job.getTitle(), job.getCompany(), job.getSource());
                    }
                }
                log.info("Scraper [{}]: processed {} jobs, {} new", scraper.getSource(), jobs.size(), totalNew);
            } catch (Exception e) {
                log.error("Scraper [{}] failed", scraper.getSource(), e);
            }
        }

        log.info("Discovery complete. Total new jobs saved: {}", totalNew);
        return totalNew;
    }
}

