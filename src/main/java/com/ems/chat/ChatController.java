package com.ems.chat;

import com.ems.model.Role;
import com.ems.model.UserInfo;
import com.ems.notification.NotificationService;
import com.ems.notification.NotificationType;
import com.ems.notification.dto.NotificationRequest;
import com.ems.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserInfoRepository userInfoRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(ChatMessage message, Principal principal) {
        UserInfo sender = principal != null
                ? userInfoRepository.findByEmail(principal.getName()).orElse(null)
                : null;

        if (message.getType() == ChatMessageType.EMERGENCY && !isAdmin(sender)) {
            log.warn("Rejected EMERGENCY chat message from non-admin: {}", message.getFrom());
            return;
        }
        if (message.getType() == ChatMessageType.DIRECT && message.getTo() == null) {
            log.warn("Rejected DIRECT chat message with no recipient from: {}", message.getFrom());
            return;
        }

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .sender(message.getFrom())
                .senderName(message.getName())
                .text(message.getText())
                .ts(message.getTs())
                .type(message.getType())
                .recipient(message.getType() == ChatMessageType.DIRECT ? message.getTo() : null)
                .build();
        chatMessageRepository.save(entity);

        if (message.getType() == ChatMessageType.DIRECT) {
            messagingTemplate.convertAndSendToUser(message.getTo(), "/queue/chat", message);
            if (!message.getTo().equals(message.getFrom())) {
                messagingTemplate.convertAndSendToUser(message.getFrom(), "/queue/chat", message);
            }
            return;
        }

        messagingTemplate.convertAndSend("/topic/chat", message);

        if (message.getType() == ChatMessageType.EMERGENCY) {
            notifyEveryoneExcept(sender, message.getText());
        }
    }

    private boolean isAdmin(UserInfo user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN);
    }

    private void notifyEveryoneExcept(UserInfo sender, String text) {
        List<UserInfo> recipients = userInfoRepository.findAll();
        for (UserInfo recipient : recipients) {
            if (sender != null && recipient.getId().equals(sender.getId())) {
                continue;
            }
            NotificationRequest notification = new NotificationRequest();
            notification.setTitle("Emergency Alert");
            notification.setMessage(text);
            notification.setType(NotificationType.ALERT);
            notification.setRecipientId(recipient.getId());
            notificationService.create(notification);
        }
    }
}
