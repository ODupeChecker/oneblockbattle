package com.oneblock.multione;

import com.oneblock.multione.manager.OneBlockManager;
import com.oneblock.multione.placeholder.OneBlockPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiOneBlockPlugin extends JavaPlugin {
    private OneBlockManager oneBlockManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        oneBlockManager = new OneBlockManager(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OneBlockPlaceholderExpansion(this, oneBlockManager).register();
            getLogger().info("Registered PlaceholderAPI expansion.");
        } else {
            getLogger().warning("PlaceholderAPI not found; placeholders will be unavailable.");
        }
    }

    public OneBlockManager getOneBlockManager() {
        return oneBlockManager;
    }
}
