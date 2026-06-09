package com.ems.notification;

import com.ems.notification.dto.NotificationRequest;
import com.ems.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse create(NotificationRequest request);
    NotificationResponse getById(Long id);
    List<NotificationResponse> getAll();
    List<NotificationResponse> getByRecipient(Long recipientId);
    List<NotificationResponse> getUnreadByRecipient(Long recipientId);
    NotificationResponse markAsRead(Long id);
    void markAllAsRead(Long recipientId);
    void delete(Long id);
}
