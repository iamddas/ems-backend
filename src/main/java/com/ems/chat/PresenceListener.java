package com.ems.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PresenceListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        broadcastOnlineUsers();
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        broadcastOnlineUsers();
    }

    private void broadcastOnlineUsers() {
        Set<String> emails = simpUserRegistry.getUsers().stream().map(SimpUser::getName).collect(Collectors.toSet());
        messagingTemplate.convertAndSend("/topic/presence", emails);
    }
}
