package com.oneblock.multione.placeholder;

import com.oneblock.multione.MultiOneBlockPlugin;
import com.oneblock.multione.manager.OneBlockManager;
import com.oneblock.multione.util.TimeFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class OneBlockPlaceholderExpansion extends PlaceholderExpansion {
    private final MultiOneBlockPlugin plugin;
    private final OneBlockManager manager;

    public OneBlockPlaceholderExpansion(MultiOneBlockPlugin plugin, OneBlockManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public String getIdentifier() {
        return "multione";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isBlank()) {
            return "";
        }

        if (params.equalsIgnoreCase("next")) {
            long seconds = manager.getSecondsUntilNextReset();
            return TimeFormatter.formatDuration(seconds);
        }

        if (params.equalsIgnoreCase("next_timestamp")) {
            return manager.getNextResetTimestamp();
        }

        return "";
    }
}
