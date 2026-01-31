package com.oneblock.multione.manager;

import com.oneblock.multione.MultiOneBlockPlugin;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OneBlockManager {
    private final MultiOneBlockPlugin plugin;
    private long nextResetEpochSeconds;

    public OneBlockManager(MultiOneBlockPlugin plugin) {
        this.plugin = plugin;
        nextResetEpochSeconds = plugin.getConfig().getLong("next-reset-epoch-seconds", 0L);
    }

    public long getSecondsUntilNextReset() {
        if (nextResetEpochSeconds <= 0L) {
            return -1L;
        }
        long now = Instant.now().getEpochSecond();
        return Math.max(0L, nextResetEpochSeconds - now);
    }

    public String getNextResetTimestamp() {
        if (nextResetEpochSeconds <= 0L) {
            return "N/A";
        }
        ZonedDateTime time = Instant.ofEpochSecond(nextResetEpochSeconds)
            .atZone(ZoneId.systemDefault());
        return time.toLocalDateTime().toString();
    }

    public void setNextResetEpochSeconds(long nextResetEpochSeconds) {
        this.nextResetEpochSeconds = nextResetEpochSeconds;
        plugin.getConfig().set("next-reset-epoch-seconds", nextResetEpochSeconds);
        plugin.saveConfig();
    }
}
