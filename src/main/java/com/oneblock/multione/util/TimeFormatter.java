package com.oneblock.multione.util;

import java.util.concurrent.TimeUnit;

public final class TimeFormatter {
    private TimeFormatter() {
    }

    public static String formatDuration(long seconds) {
        if (seconds < 0) {
            return "N/A";
        }
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds));
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds));

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, remainingSeconds);
        }
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, remainingSeconds);
        }
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, remainingSeconds);
        }
        return String.format("%ds", remainingSeconds);
    }
}
