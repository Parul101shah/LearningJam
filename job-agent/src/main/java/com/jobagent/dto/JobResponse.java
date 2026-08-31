package com.jobagent.dto;

import com.jobagent.entity.JobStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning job info to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String location;
    private String source;
    private String sourceUrl;
    private JobStatus status;
    private Integer matchScore;
    private LocalDateTime discoveredAt;
}

