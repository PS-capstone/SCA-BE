package com.example.sca_be.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationSessionManager {

    private final Map<Integer, Set<WebSocketSession>> studentSessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionStudentMap = new ConcurrentHashMap<>();

    public void register(Integer studentId, WebSocketSession session) {
        studentSessions.computeIfAbsent(studentId, key -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(session);
        sessionStudentMap.put(session.getId(), studentId);
        log.debug("Registered WebSocket session {} for student {}", session.getId(), studentId);
    }

    public void remove(WebSocketSession session) {
        Integer studentId = sessionStudentMap.remove(session.getId());
        if (studentId == null) {
            return;
        }

        Set<WebSocketSession> sessions = studentSessions.get(studentId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                studentSessions.remove(studentId);
            }
        }
        log.debug("Removed WebSocket session {} from student {}", session.getId(), studentId);
    }

    public void broadcast(Integer studentId, String payload) {
        Set<WebSocketSession> sessions = studentSessions.get(studentId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("No active sessions for student {}", studentId);
            return;
        }

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                    log.debug("Sent notification to student {} session {}", studentId, session.getId());
                }
            } catch (IOException e) {
                log.warn("Failed to send notification message to session {}", session.getId(), e);
            }
        }
    }
}
