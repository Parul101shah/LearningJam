package com.jobagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobagent.dto.ExtractedProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Uses Spring AI (OpenAI) to extract structured profile data from raw resume text.
 *
 * Flow: Resume PDF → PDFBox raw text → THIS SERVICE → ExtractedProfile DTO → Preview to user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAIExtractor {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public ExtractedProfile extract(String resumeText) {
        // Truncate to avoid token limits
        String truncated = resumeText.length() > 4000
                ? resumeText.substring(0, 4000)
                : resumeText;

        String prompt = """
                Extract a structured profile from this resume. Return ONLY valid JSON, no markdown fences.

                {
                  "skills": ["skill1", "skill2", ...],
                  "roles": ["most recent job title", "previous job title", ...],
                  "totalExperienceYears": <number>,
                  "education": "<highest degree - institution>",
                  "profileSummary": "<3-4 sentence summary of this candidate's strengths, experience, and expertise>"
                }

                RESUME TEXT:
                %s
                """.formatted(truncated);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI extraction response: {}", response);

            // Clean markdown fences if AI includes them
            response = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(response, ExtractedProfile.class);
        } catch (Exception e) {
            log.error("Failed to extract profile via AI", e);
            // Return empty profile on failure — user can fill manually
            return new ExtractedProfile();
        }
    }
}

