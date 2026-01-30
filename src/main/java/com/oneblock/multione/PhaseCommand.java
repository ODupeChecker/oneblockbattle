package com.oneblock.multione;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhaseCommand implements CommandExecutor, TabCompleter {
    private final OneBlockManager manager;

    public PhaseCommand(OneBlockManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("oneblock.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /phase <number> addblock <block>");
            return true;
        }
        int phaseNumber;
        try {
            phaseNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Phase number must be an integer.");
            return true;
        }
        if (phaseNumber < 1 || phaseNumber >= Phase.PHASE_6.getNumber()) {
            sender.sendMessage("Phase number must be between 1 and " + (Phase.PHASE_6.getNumber() - 1) + ".");
            return true;
        }
        String sub = args[1].toLowerCase();
        if (!sub.equals("addblock")) {
            sender.sendMessage("Unknown subcommand. Use addblock.");
            return true;
        }
        Material material = Material.matchMaterial(args[2]);
        if (material == null) {
            sender.sendMessage("Unknown material: " + args[2]);
            return true;
        }
        boolean added = manager.addBlockToPhase(phaseNumber, material);
        if (added) {
            sender.sendMessage("Added " + material.name() + " to phase " + phaseNumber + " with a 1% chance.");
        } else {
            sender.sendMessage("That block is already configured for phase " + phaseNumber + ".");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("oneblock.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> phases = new ArrayList<>();
            for (int i = 1; i < Phase.PHASE_6.getNumber(); i++) {
                phases.add(String.valueOf(i));
            }
            return phases;
        }
        if (args.length == 2) {
            return Collections.singletonList("addblock");
        }
        if (args.length == 3) {
            String prefix = args[2].toUpperCase();
            List<String> matches = new ArrayList<>();
            for (Material material : Material.values()) {
                if (material.name().startsWith(prefix)) {
                    matches.add(material.name());
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }
}
