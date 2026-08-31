package com.jobagent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Manual input during registration ---
    private String fullName;
    private String email;
    private String preferredLocation;
    private Integer minSalary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_target_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> targetRoles = new ArrayList<>();  // what they WANT (manual)

    // --- AI-extracted from resume ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_past_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> pastRoles = new ArrayList<>();     // what they've DONE (AI-extracted)

    private Integer totalExperienceYears;
    private String education;

    @Column(columnDefinition = "TEXT")
    private String profileSummary;   // AI-generated summary

    @Column(columnDefinition = "TEXT")
    private String resumeText;       // raw text from PDF for matching
}

