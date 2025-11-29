package com.example.sca_be.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final NotificationSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer studentId = resolveStudentId(session.getUri());
        if (studentId == null) {
            log.warn("Closing session {} due to invalid student id", session.getId());
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        sessionManager.register(studentId, session);
        log.info("Notification WebSocket connected for student {}", studentId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Read-only channel, ignore messages from clients
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.remove(session);
    }

    private Integer resolveStudentId(URI uri) {
        if (uri == null) {
            return null;
        }

        String path = uri.getPath(); // e.g. /ws/students/1/notifications
        String[] segments = path.split("/");
        if (segments.length < 5) {
            return null;
        }

        try {
            return Integer.parseInt(segments[3]);
        } catch (NumberFormatException e) {
            log.warn("Invalid student id in websocket path: {}", path);
            return null;
        }
    }
}
