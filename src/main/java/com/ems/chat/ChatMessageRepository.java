package com.ems.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findTop50ByTypeInOrderByTsDesc(List<ChatMessageType> types);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.type = 'DIRECT' AND "
            + "((m.sender = :a AND m.recipient = :b) OR (m.sender = :b AND m.recipient = :a)) "
            + "ORDER BY m.ts DESC")
    List<ChatMessageEntity> findConversation(@Param("a") String a, @Param("b") String b, Pageable pageable);
}
