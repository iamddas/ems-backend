package com.ems.chat;

import com.ems.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private static final List<ChatMessageType> PUBLIC_TYPES = List.of(ChatMessageType.BROADCAST, ChatMessageType.EMERGENCY);

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getRecentMessages() {
        List<ChatMessageEntity> recent = chatMessageRepository.findTop50ByTypeInOrderByTsDesc(PUBLIC_TYPES);
        recent.sort(Comparator.comparingLong(ChatMessageEntity::getTs));
        return ResponseEntity.ok(recent.stream().map(this::toDto).toList());
    }

    @GetMapping("/messages/direct/{otherEmail}")
    public ResponseEntity<List<ChatMessage>> getDirectMessages(@PathVariable String otherEmail) {
        String me = getCurrentUser().getEmail();
        List<ChatMessageEntity> conversation = chatMessageRepository.findConversation(me, otherEmail, PageRequest.of(0, 50));
        conversation.sort(Comparator.comparingLong(ChatMessageEntity::getTs));
        return ResponseEntity.ok(conversation.stream().map(this::toDto).toList());
    }

    private UserInfo getCurrentUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private ChatMessage toDto(ChatMessageEntity entity) {
        ChatMessage dto = new ChatMessage();
        dto.setFrom(entity.getSender());
        dto.setName(entity.getSenderName());
        dto.setText(entity.getText());
        dto.setTs(entity.getTs());
        dto.setType(entity.getType());
        dto.setTo(entity.getRecipient());
        return dto;
    }
}
