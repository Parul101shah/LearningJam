package com.jobagent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source", "externalId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;

    private String source;       // GREENHOUSE, LEVER, NAUKRI
    private String sourceUrl;    // original posting URL
    private String externalId;   // ID from the source platform

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.NEW;

    private LocalDateTime discoveredAt;

    private Integer matchScore;  // AI match score (0-100)

    @PrePersist
    protected void onCreate() {
        if (discoveredAt == null) {
            discoveredAt = LocalDateTime.now();
        }
    }
}

