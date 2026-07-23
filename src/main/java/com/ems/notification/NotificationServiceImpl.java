package com.ems.notification;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.model.UserInfo;
import com.ems.notification.dto.NotificationRequest;
import com.ems.notification.dto.NotificationResponse;
import com.ems.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserInfoRepository userInfoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        UserInfo recipient = userInfoRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRecipientId()));
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : NotificationType.INFO)
                .recipient(recipient)
                .build();
        NotificationResponse response = toResponse(notificationRepository.save(notification));
        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/notifications", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdAndIsRead(recipientId, false)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = findOrThrow(id);
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllAsReadByRecipient(recipientId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        notificationRepository.deleteById(id);
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .recipientId(n.getRecipient().getId())
                .recipientEmail(n.getRecipient().getEmail())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
