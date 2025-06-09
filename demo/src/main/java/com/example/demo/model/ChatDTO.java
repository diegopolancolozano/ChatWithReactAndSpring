package com.example.demo.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Long id;
    private String participant1;
    private String participant2;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageTime;
    
    public String getOtherParticipant(String currentUser) {
        return participant1.equals(currentUser) ? participant2 : participant1;
    }
}