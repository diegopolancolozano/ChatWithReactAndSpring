package com.example.demo.repository;

import com.example.demo.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByParticipant1OrParticipant2(String participant1, String participant2);
    
    @Query("SELECT c FROM Chat c WHERE (c.participant1 = ?1 AND c.participant2 = ?2) OR (c.participant1 = ?2 AND c.participant2 = ?1)")
    Optional<Chat> findByParticipants(String participant1, String participant2);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.messages WHERE c.participant1 = ?1 OR c.participant2 = ?1 ORDER BY c.createdAt DESC")
    List<Chat> findByParticipant1OrParticipant2WithMessages(String username);
}