package com.ems.chat;

import com.ems.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String sender;

    @NotBlank
    @Column(nullable = false)
    private String senderName;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private long ts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChatMessageType type = ChatMessageType.BROADCAST;

    /** Only set when type == DIRECT; the recipient's email. */
    private String recipient;
}
