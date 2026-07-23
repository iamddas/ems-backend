package com.ems.chat;

import lombok.Data;

@Data
public class ChatMessage {
    private String from;
    private String name;
    private String text;
    private long ts;
    private ChatMessageType type = ChatMessageType.BROADCAST;
    /** Only set when type == DIRECT; the recipient's email. */
    private String to;
}
