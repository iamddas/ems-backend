package com.ems.notification.dto;

import com.ems.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private NotificationType type;

    @NotNull
    private Long recipientId;
}
