package com.ems.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String phone;

    /** Base64 data URI, or null to leave the current picture unchanged. */
    private String profilePictureUrl;
}
