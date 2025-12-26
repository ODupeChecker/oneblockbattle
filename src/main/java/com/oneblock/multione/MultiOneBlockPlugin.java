package com.oneblock.multione;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiOneBlockPlugin extends JavaPlugin implements Listener {

    private OneBlockManager oneBlockManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        oneBlockManager = new OneBlockManager(this);
        Bukkit.getPluginManager().registerEvents(new OneBlockListener(oneBlockManager), this);
        registerCommands();
        oneBlockManager.load();
        oneBlockManager.startTicking();
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
    }
}
