package com.example.sca_be.global.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationBroadcaster {

    private final NotificationSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * Broadcast a notification message to a specific student
     * @param studentId The student ID to send the notification to
     * @param message The message object to broadcast (will be serialized to JSON)
     */
    public void broadcast(Integer studentId, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            sessionManager.broadcast(studentId, payload);
            log.debug("Broadcasted notification to student {}", studentId);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize notification message for student {}", studentId, e);
        }
    }
}
