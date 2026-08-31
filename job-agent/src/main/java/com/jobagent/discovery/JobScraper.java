package com.jobagent.discovery;

import com.jobagent.entity.Job;

import java.util.List;

/**
 * Strategy interface for job scrapers. Each job board gets its own implementation.
 */
public interface JobScraper {

    /** Identifier for this source (e.g. "GREENHOUSE", "LEVER") */
    String getSource();

    /** Scrape and return a list of jobs from the source */
    List<Job> scrape();
}

