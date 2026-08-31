package com.jobagent.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobagent.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes jobs from Greenhouse public API.
 * Most companies on Greenhouse expose: https://boards-api.greenhouse.io/v1/boards/{company}/jobs
 */
@Component
@Slf4j
public class GreenhouseScraper implements JobScraper {

    @Value("${scraper.greenhouse.companies}")
    private List<String> companies;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getSource() {
        return "GREENHOUSE";
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();

        for (String company : companies) {
            try {
                String listUrl = "https://boards-api.greenhouse.io/v1/boards/" + company + "/jobs";
                JsonNode root = restTemplate.getForObject(listUrl, JsonNode.class);

                if (root == null || !root.has("jobs")) continue;

                for (JsonNode node : root.get("jobs")) {
                    try {
                        Job job = new Job();
                        job.setTitle(node.get("title").asText());
                        job.setCompany(company);
                        job.setLocation(node.path("location").path("name").asText("Remote"));
                        job.setSource("GREENHOUSE");
                        job.setExternalId(node.get("id").asText());
                        job.setSourceUrl(node.get("absolute_url").asText());

                        // Fetch full job detail for description
                        String detailUrl = listUrl + "/" + node.get("id").asText();
                        JsonNode detail = restTemplate.getForObject(detailUrl, JsonNode.class);
                        if (detail != null && detail.has("content")) {
                            // Strip HTML tags from description
                            job.setDescription(Jsoup.parse(detail.get("content").asText()).text());
                        }

                        jobs.add(job);
                    } catch (Exception e) {
                        log.warn("Failed to parse Greenhouse job from {}: {}", company, e.getMessage());
                    }
                }

                log.info("Greenhouse [{}]: found {} jobs", company, jobs.size());
            } catch (Exception e) {
                log.error("Failed to scrape Greenhouse for company: {}", company, e);
            }
        }

        return jobs;
    }
}

