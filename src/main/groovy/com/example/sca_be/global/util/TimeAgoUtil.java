package com.example.sca_be.global.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for calculating "time ago" strings
 * Examples: "2시간 전", "1일 전", "방금 전"
 */
public class TimeAgoUtil {

    /**
     * Calculate time ago string from a given datetime
     * @param dateTime The datetime to calculate from
     * @return Korean time ago string (e.g., "2시간 전", "1일 전")
     */
    public static String calculate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        // Future time (shouldn't happen, but handle it)
        if (duration.isNegative()) {
            return "방금 전";
        }

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (days > 0) {
            return days + "일 전";
        } else if (hours > 0) {
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return "방금 전";
        }
    }

    /**
     * Calculate time ago string with more detailed format
     * Examples: "2일 3시간 전", "5시간 30분 전"
     */
    public static String calculateDetailed(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        if (duration.isNegative()) {
            return "방금 전";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            if (hours > 0) {
                return String.format("%d일 %d시간 전", days, hours);
            }
            return days + "일 전";
        } else if (hours > 0) {
            if (minutes > 0) {
                return String.format("%d시간 %d분 전", hours, minutes);
            }
            return hours + "시간 전";
        } else if (minutes > 0) {
            return minutes + "분 전";
        } else {
            return "방금 전";
        }
    }
}
