package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId AND m.receiver.id = :otherId) OR (m.sender.id = :otherId AND m.receiver.id = :userId) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId") Long userId, @Param("otherId") Long otherId);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.isRead = 0")
    List<Message> findUnreadByReceiver(@Param("userId") Long userId);

    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = 0")
    long countUnreadByReceiver(@Param("userId") Long userId);
}
