package com.example.sca_be.global.config;

import com.example.sca_be.global.websocket.ActivityLogWebSocketHandler;
import com.example.sca_be.global.websocket.NotificationWebSocketHandler;
import com.example.sca_be.global.websocket.RaidLogWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RaidWebSocketConfig implements WebSocketConfigurer {

    private final RaidLogWebSocketHandler raidLogWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ActivityLogWebSocketHandler activityLogWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Raid logs WebSocket
        registry.addHandler(raidLogWebSocketHandler, "/ws/raids/*/logs")
                .setAllowedOriginPatterns("*");

        // Student notifications WebSocket
        registry.addHandler(notificationWebSocketHandler, "/ws/students/*/notifications")
                .setAllowedOriginPatterns("*");

        // Student activity logs WebSocket
        registry.addHandler(activityLogWebSocketHandler, "/ws/students/*/activity-logs")
                .setAllowedOriginPatterns("*");
    }
}

