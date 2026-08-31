package com.jobagent.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobagent.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes jobs from Lever public API.
 * Most companies on Lever expose: https://api.lever.co/v0/postings/{company}
 */
@Component
@Slf4j
public class LeverScraper implements JobScraper {

    @Value("${scraper.lever.companies}")
    private List<String> companies;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getSource() {
        return "LEVER";
    }

    @Override
    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();

        for (String company : companies) {
            try {
                String url = "https://api.lever.co/v0/postings/" + company;
                JsonNode[] postings = restTemplate.getForObject(url, JsonNode[].class);

                if (postings == null) continue;

                for (JsonNode node : postings) {
                    try {
                        Job job = new Job();
                        job.setTitle(node.get("text").asText());
                        job.setCompany(company);
                        job.setLocation(node.path("categories").path("location").asText("Remote"));
                        job.setSource("LEVER");
                        job.setExternalId(node.get("id").asText());
                        job.setSourceUrl(node.get("hostedUrl").asText());

                        // Lever provides plain text description
                        String desc = node.has("descriptionPlain")
                                ? node.get("descriptionPlain").asText()
                                : node.path("description").asText("");
                        job.setDescription(desc);

                        jobs.add(job);
                    } catch (Exception e) {
                        log.warn("Failed to parse Lever job from {}: {}", company, e.getMessage());
                    }
                }

                log.info("Lever [{}]: found {} jobs", company, jobs.size());
            } catch (Exception e) {
                log.error("Failed to scrape Lever for company: {}", company, e);
            }
        }

        return jobs;
    }
}

