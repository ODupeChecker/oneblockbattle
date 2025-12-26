package com.oneblock.multione;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OneBlockCommand implements CommandExecutor, TabCompleter {

    private final OneBlockManager manager;

    public OneBlockCommand(OneBlockManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("oneblock.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("Usage: /oneblock add/remove <coords>");
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            default -> sender.sendMessage("Unknown subcommand. Use add or remove.");
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        ParseResult parse = parse(sender, args);
        if (!parse.valid) {
            sender.sendMessage(parse.message);
            return;
        }
        boolean created = manager.addOneBlock(parse.world, parse.x, parse.y, parse.z);
        if (created) {
            sender.sendMessage("OneBlock registered at " + parse.world.getName() + " " + parse.x + " " + parse.y + " " + parse.z);
        } else {
            sender.sendMessage("A OneBlock already exists at those coordinates.");
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        ParseResult parse = parse(sender, args);
        if (!parse.valid) {
            sender.sendMessage(parse.message);
            return;
        }
        boolean removed = manager.removeOneBlock(parse.world, parse.x, parse.y, parse.z);
        if (removed) {
            sender.sendMessage("OneBlock removed at " + parse.world.getName() + " " + parse.x + " " + parse.y + " " + parse.z);
        } else {
            sender.sendMessage("No OneBlock registered at those coordinates.");
        }
    }

    private ParseResult parse(CommandSender sender, String[] args) {
        boolean console = !(sender instanceof Player);
        if (console) {
            if (args.length != 5) {
                return ParseResult.error("Console usage: /oneblock <add|remove> <world> <x> <y> <z>");
            }
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                return ParseResult.error("World not found: " + args[1]);
            }
            try {
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                return ParseResult.success(world, x, y, z);
            } catch (NumberFormatException ex) {
                return ParseResult.error("Coordinates must be integers.");
            }
        }
        if (args.length != 4) {
            return ParseResult.error("Usage: /oneblock <add|remove> <x> <y> <z>");
        }
        Player player = (Player) sender;
        World world = player.getWorld();
        try {
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            int z = Integer.parseInt(args[3]);
            return ParseResult.success(world, x, y, z);
        } catch (NumberFormatException ex) {
            return ParseResult.error("Coordinates must be integers.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("oneblock.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("add");
            subs.add("remove");
            return subs;
        }
        if (args.length == 2 && !(sender instanceof Player)) {
            return Bukkit.getWorlds().stream().map(World::getName).toList();
        }
        return Collections.emptyList();
    }

    private static class ParseResult {
        final boolean valid;
        final String message;
        final World world;
        final int x, y, z;

        private ParseResult(boolean valid, String message, World world, int x, int y, int z) {
            this.valid = valid;
            this.message = message;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        static ParseResult success(World world, int x, int y, int z) {
            return new ParseResult(true, "", world, x, y, z);
        }

        static ParseResult error(String message) {
            return new ParseResult(false, message, null, 0, 0, 0);
        }
    }
}
