package com.jobagent.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for user registration - manual input only.
 */
@Data
public class UserRegistrationRequest {
    private String fullName;
    private String email;
    private String preferredLocation;
    private Integer minSalary;
    private List<String> targetRoles;
}

