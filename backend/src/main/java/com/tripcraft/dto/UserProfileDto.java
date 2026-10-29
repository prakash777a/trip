package com.tripcraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** REST-safe profile payload; it intentionally has no password field. */
public record UserProfileDto(
        Long id,
        String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        String role,
        boolean active,
        List<String> interests
) {}
