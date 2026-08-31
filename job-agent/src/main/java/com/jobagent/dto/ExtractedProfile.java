package com.jobagent.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * AI-extracted profile from resume. Shown as preview for user to confirm/edit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedProfile {
    private List<String> skills;
    private List<String> roles;           // past job titles from resume
    private int totalExperienceYears;
    private String education;
    private String profileSummary;        // AI-generated 3-4 line summary
}

