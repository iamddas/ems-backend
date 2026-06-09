package com.ems.notification.dto;

import com.ems.notification.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Long recipientId;
    private String recipientEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
