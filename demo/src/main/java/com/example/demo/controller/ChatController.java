package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessageDTO messageDTO) {
        // Buscar chat existente en cualquier dirección
        Chat chat = chatRepository.findByParticipants(messageDTO.getSender(), messageDTO.getReceiver())
            .orElseGet(() -> {
                // Crear nuevo chat si no existe
                Chat newChat = new Chat();
                newChat.setParticipant1(messageDTO.getSender());
                newChat.setParticipant2(messageDTO.getReceiver());
                newChat.setCreatedAt(LocalDateTime.now());
                return chatRepository.save(newChat);
            });

        // Guardar mensaje
        ChatMessage message = new ChatMessage();
        message.setSender(messageDTO.getSender());
        message.setReceiver(messageDTO.getReceiver());
        message.setMessage(messageDTO.getMessage());
        message.setTimestamp(LocalDateTime.now());
        message.setChat(chat);
        chatMessageRepository.save(message);

        // Preparar DTO para respuesta
        ChatMessageDTO responseDto = new ChatMessageDTO(
            message.getId(),
            message.getSender(),
            message.getReceiver(),
            message.getMessage(),
            message.getTimestamp()
        );

        // Enviar a ambos participantes
        messagingTemplate.convertAndSend("/subscribeTo/" + messageDTO.getReceiver(), responseDto);
        messagingTemplate.convertAndSend("/subscribeTo/" + messageDTO.getSender(), responseDto);
    }

    @GetMapping("/list/{username}")
    public ResponseEntity<List<ChatDTO>> getUserChats(@PathVariable String username) {
        try {
            List<Chat> chats = chatRepository.findByParticipant1OrParticipant2WithMessages(username);
            
            List<ChatDTO> chatDTOs = chats.stream()
                .map(chat -> {
                    // Obtener último mensaje si existe
                    LocalDateTime lastMessageTime = null;
                    if (!chat.getMessages().isEmpty()) {
                        lastMessageTime = chat.getMessages().stream()
                            .max(Comparator.comparing(ChatMessage::getTimestamp))
                            .map(ChatMessage::getTimestamp)
                            .orElse(null);
                    }
                    
                    return new ChatDTO(
                        chat.getId(),
                        chat.getParticipant1(),
                        chat.getParticipant2(),
                        chat.getCreatedAt(),
                        lastMessageTime
                    );
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(chatDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/messages/{chatId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable Long chatId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByTimestampAsc(chatId);
        
        List<ChatMessageDTO> messageDTOs = messages.stream()
            .map(msg -> new ChatMessageDTO(
                msg.getId(),
                msg.getSender(),
                msg.getReceiver(),
                msg.getMessage(),
                msg.getTimestamp()
            ))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(messageDTOs);
    }

    @GetMapping("/between/{user1}/{user2}")
    public ResponseEntity<ChatDTO> getChatBetweenUsers(
            @PathVariable String user1,
            @PathVariable String user2) {
        
        Chat chat = chatRepository.findByParticipants(user1, user2)
            .orElseGet(() -> {
                Chat newChat = new Chat();
                newChat.setParticipant1(user1);
                newChat.setParticipant2(user2);
                newChat.setCreatedAt(LocalDateTime.now());
                return chatRepository.save(newChat);
            });
        
        return ResponseEntity.ok(new ChatDTO(
            chat.getId(),
            chat.getParticipant1(),
            chat.getParticipant2(),
            chat.getCreatedAt(),
            null
        ));
    }
}