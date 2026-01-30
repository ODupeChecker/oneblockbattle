package com.oneblock.multione;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MultiOneBlockPlugin extends JavaPlugin {

    private OneBlockManager oneBlockManager;

    @Override
    public void onEnable() {
        getLogger().info("Enabling MultiOneBlock...");
        try {
            oneBlockManager = new OneBlockManager(this);
            Bukkit.getPluginManager().registerEvents(new OneBlockListener(oneBlockManager), this);
            registerCommands();
            oneBlockManager.loadPhaseConfig();
            oneBlockManager.load();
            oneBlockManager.startTicking();
            getLogger().info("MultiOneBlock enabled successfully.");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to enable MultiOneBlock. Plugin will remain loaded but may be inactive.", ex);
        }
    }

    @Override
    public void onDisable() {
        if (oneBlockManager != null) {
            oneBlockManager.shutdown();
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("oneblock");
        if (command != null) {
            OneBlockCommand executor = new OneBlockCommand(oneBlockManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().severe("oneblock command not defined in plugin.yml");
        }
        PluginCommand phaseCommand = getCommand("phase");
        if (phaseCommand != null) {
            PhaseCommand executor = new PhaseCommand(oneBlockManager);
            phaseCommand.setExecutor(executor);
            phaseCommand.setTabCompleter(executor);
        } else {
            getLogger().severe("phase command not defined in plugin.yml");
        }
    }
}
