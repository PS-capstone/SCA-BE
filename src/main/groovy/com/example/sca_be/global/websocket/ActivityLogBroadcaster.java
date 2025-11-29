package com.example.sca_be.global.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogBroadcaster {

    private final ActivityLogSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * Broadcast an activity log message to a specific student
     * @param studentId The student ID to send the activity log to
     * @param message The message object to broadcast (will be serialized to JSON)
     */
    public void broadcast(Integer studentId, Object message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            sessionManager.broadcast(studentId, payload);
            log.debug("Broadcasted activity log to student {}", studentId);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize activity log message for student {}", studentId, e);
        }
    }
}
