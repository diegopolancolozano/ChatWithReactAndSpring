package com.example.demo.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long id;
    private String sender;
    private String receiver;
    private String message;
    private LocalDateTime timestamp;

    public ChatMessageDTO(Long id, String sender, String receiver, String message, LocalDateTime timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }
}