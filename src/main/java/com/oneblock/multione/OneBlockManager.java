package com.oneblock.multione;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.SoundCategory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class OneBlockManager {
    private final Plugin plugin;
    private final Map<LocationKey, OneBlock> oneBlocks = new HashMap<>();
    private final File dataFile;
    private final File phaseConfigFile;
    private final Map<Integer, LinkedHashMap<Material, Double>> phaseChances = new HashMap<>();
    private BukkitTask tickingTask;
    private int saveTicker = 0;

    public OneBlockManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "oneblocks.yml");
        this.phaseConfigFile = new File(plugin.getDataFolder(), "phases.yml");
    }

    public void startTicking() {
        tickingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateBossBars();
            if (++saveTicker >= 120) {
                saveTicker = 0;
                save();
            }
        }, 10L, 10L);
    }

    public void shutdown() {
        if (tickingTask != null) {
            tickingTask.cancel();
        }
        save();
        oneBlocks.values().forEach(ob -> {
            if (ob.getBossBar() != null) {
                ob.getBossBar().removeAll();
            }
        });
        oneBlocks.clear();
    }

    public boolean addOneBlock(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        LocationKey key = new LocationKey(world.getUID(), x, y, z);
        if (oneBlocks.containsKey(key)) {
            return false;
        }
        OneBlock oneBlock = new OneBlock(world.getUID(), x, y, z, 1, 0, false);
        BossBar bar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        bar.setVisible(false);
        oneBlock.setBossBar(bar);
        oneBlocks.put(key, oneBlock);
        placeNextBlock(oneBlock);
        save();
        return true;
    }

    public boolean removeOneBlock(World world, int x, int y, int z) {
        if (world == null) {
            return false;
        }
        LocationKey key = new LocationKey(world.getUID(), x, y, z);
        OneBlock removed = oneBlocks.remove(key);
        if (removed != null) {
            if (removed.getBossBar() != null) {
                removed.getBossBar().removeAll();
            }
            save();
            return true;
        }
        return false;
    }

    public OneBlock getOneBlock(Location location) {
        LocationKey key = LocationKey.fromLocation(location);
        return oneBlocks.get(key);
    }

    public Collection<OneBlock> getAll() {
        return oneBlocks.values();
    }

    public void handleBreak(OneBlock ob) {
        if (ob.getPhase() >= Phase.PHASE_6.getNumber()) {
            return;
        }
        if (ob.isChestPlaced()) {
            ob.incrementBrokenInPhase();
            advancePhase(ob);
            save();
            return;
        }
        ob.incrementBrokenInPhase();
        int broken = ob.getBrokenInPhase();
        if (broken == Phase.BLOCKS_PER_PHASE - 1) {
            placeChest(ob);
            ob.setChestPlaced(true);
        } else {
            placeNextBlock(ob);
        }
        save();
    }

    private void advancePhase(OneBlock ob) {
        int newPhase = ob.getPhase() + 1;
        ob.setChestPlaced(false);
        ob.setBrokenInPhase(0);
        ob.setPhase(newPhase);
        if (newPhase >= Phase.PHASE_6.getNumber()) {
            placeEndPortal(ob);
            removeBossBar(ob);
        } else {
            placeNextBlock(ob);
            updateBossBar(ob);
        }
        announceProgress(ob, newPhase);
    }

    private void announceProgress(OneBlock ob, int phaseNumber) {
        Location location = ob.toLocation();
        if (location == null) {
            return;
        }
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= 100) {
                player.sendMessage("OneBlock progressed to Phase " + phaseNumber + "!");
                player.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private void placeNextBlock(OneBlock ob) {
        Phase phase = Phase.of(ob.getPhase());
        if (phase == Phase.PHASE_6) {
            placeEndPortal(ob);
            return;
        }
        Material next = selectNextMaterial(ob.getPhase());
        if (next != null) {
            setBlock(ob, next);
        }
        updateBossBar(ob);
    }

    private void placeChest(OneBlock ob) {
        Location location = ob.toLocation();
        if (location == null) {
            return;
        }
        setBlock(ob, Material.CHEST, () -> {
            Block block = location.getBlock();
            if (!(block.getState() instanceof Chest chest)) {
                return;
            }
            Inventory inventory = chest.getBlockInventory();
            inventory.clear();
            for (ItemStack item : Phase.of(ob.getPhase()).getChestLoot()) {
                inventory.addItem(item);
            }
        });
    }

    private void placeEndPortal(OneBlock ob) {
        setBlock(ob, Material.END_PORTAL);
    }

    private void setBlock(OneBlock ob, Material material) {
        setBlock(ob, material, null);
    }

    private void setBlock(OneBlock ob, Material material, Runnable afterPlacement) {
        Location location = ob.toLocation();
        if (location == null) {
            return;
        }
        Chunk chunk = location.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            location.getBlock().setType(material, false);
            if (afterPlacement != null) {
                afterPlacement.run();
            }
        });
    }

    private void updateBossBars() {
        for (OneBlock ob : oneBlocks.values()) {
            if (ob.getPhase() >= Phase.PHASE_6.getNumber()) {
                continue;
            }
            updateBossBar(ob);
            updateBossBarViewers(ob);
        }
    }

    private void updateBossBar(OneBlock ob) {
        BossBar bar = ob.getBossBar();
        if (bar == null) {
            return;
        }
        int phaseNumber = ob.getPhase();
        int broken = ob.getBrokenInPhase();
        double progress = Math.max(0.0, Math.min(1.0, broken / (double) Phase.BLOCKS_PER_PHASE));
        bar.setTitle("Phase " + phaseNumber + " – Progress: " + broken + " / " + Phase.BLOCKS_PER_PHASE);
        bar.setProgress(progress);
        bar.setVisible(!ob.isChestPlaced());
    }

    private void updateBossBarViewers(OneBlock ob) {
        BossBar bar = ob.getBossBar();
        if (bar == null) {
            return;
        }
        Location location = ob.toLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }
        Set<UUID> shouldHave = new HashSet<>();
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= 100) {
                shouldHave.add(player.getUniqueId());
                if (!bar.getPlayers().contains(player)) {
                    bar.addPlayer(player);
                }
            }
        }
        for (Player player : new ArrayList<>(bar.getPlayers())) {
            if (!shouldHave.contains(player.getUniqueId())) {
                bar.removePlayer(player);
            }
        }
    }

    public void load() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        List<Map<?, ?>> list = config.getMapList("oneblocks");
        for (Map<?, ?> entry : list) {
            try {
                UUID worldId = UUID.fromString(String.valueOf(entry.get("world")));
                int x = (int) entry.get("x");
                int y = (int) entry.get("y");
                int z = (int) entry.get("z");
                int phase = (int) entry.get("phase");
                int broken = (int) entry.get("broken");
                Object chestObj = entry.get("chestPlaced");
                boolean chestPlaced = chestObj instanceof Boolean b ? b : false;
                World world = Bukkit.getWorld(worldId);
                if (world == null) {
                    plugin.getLogger().warning("Skipping OneBlock in unknown world " + worldId);
                    continue;
                }
                OneBlock ob = new OneBlock(worldId, x, y, z, phase, broken, chestPlaced);
                BossBar bar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
                bar.setVisible(false);
                ob.setBossBar(bar);
                oneBlocks.put(new LocationKey(worldId, x, y, z), ob);
                if (phase >= Phase.PHASE_6.getNumber()) {
                    placeEndPortal(ob);
                    removeBossBar(ob);
                } else if (chestPlaced) {
                    placeChest(ob);
                } else {
                    placeNextBlock(ob);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load OneBlock entry", ex);
            }
        }
    }

    public void save() {
        plugin.getDataFolder().mkdirs();
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (OneBlock ob : oneBlocks.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("world", ob.getWorldId().toString());
            map.put("x", ob.getX());
            map.put("y", ob.getY());
            map.put("z", ob.getZ());
            map.put("phase", ob.getPhase());
            map.put("broken", ob.getBrokenInPhase());
            map.put("chestPlaced", ob.isChestPlaced());
            list.add(map);
        }
        config.set("oneblocks", list);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save oneblocks.yml", e);
        }
    }

    private void removeBossBar(OneBlock ob) {
        BossBar bar = ob.getBossBar();
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void loadPhaseConfig() {
        plugin.getDataFolder().mkdirs();
        if (!phaseConfigFile.exists()) {
            writeDefaultPhaseConfig();
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(phaseConfigFile);
        if (config.getConfigurationSection("phases") == null) {
            writeDefaultPhaseConfig();
            return;
        }
        phaseChances.clear();
        for (String phaseKey : Objects.requireNonNull(config.getConfigurationSection("phases")).getKeys(false)) {
            int phaseNumber;
            try {
                phaseNumber = Integer.parseInt(phaseKey);
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("Invalid phase number in phases.yml: " + phaseKey);
                continue;
            }
            LinkedHashMap<Material, Double> chances = new LinkedHashMap<>();
            for (String materialKey : Objects.requireNonNull(config.getConfigurationSection("phases." + phaseKey)).getKeys(false)) {
                Material material = Material.matchMaterial(materialKey);
                if (material == null) {
                    plugin.getLogger().warning("Unknown material in phases.yml: " + materialKey);
                    continue;
                }
                double chance = config.getDouble("phases." + phaseKey + "." + materialKey, 0.0);
                chances.put(material, chance);
            }
            phaseChances.put(phaseNumber, chances);
        }
    }

    public void reloadPhaseConfig() {
        loadPhaseConfig();
    }

    public boolean addBlockToPhase(int phaseNumber, Material material) {
        if (material == null) {
            return false;
        }
        LinkedHashMap<Material, Double> chances = phaseChances.computeIfAbsent(phaseNumber, ignored -> new LinkedHashMap<>());
        if (chances.containsKey(material)) {
            return false;
        }
        chances.put(material, 1.0);
        savePhaseConfig();
        return true;
    }

    private void savePhaseConfig() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<Integer, LinkedHashMap<Material, Double>> entry : phaseChances.entrySet()) {
            String baseKey = "phases." + entry.getKey();
            for (Map.Entry<Material, Double> chanceEntry : entry.getValue().entrySet()) {
                config.set(baseKey + "." + chanceEntry.getKey().name(), chanceEntry.getValue());
            }
        }
        try {
            config.save(phaseConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save phases.yml", e);
        }
    }

    private void writeDefaultPhaseConfig() {
        phaseChances.clear();
        for (Phase phase : Phase.values()) {
            if (phase == Phase.PHASE_6) {
                continue;
            }
            List<Material> pool = phase.getPool();
            if (pool.isEmpty()) {
                continue;
            }
            LinkedHashMap<Material, Double> chances = new LinkedHashMap<>();
            double chance = 100.0 / pool.size();
            for (Material material : pool) {
                chances.put(material, chance);
            }
            phaseChances.put(phase.getNumber(), chances);
        }
        savePhaseConfig();
    }

    private Material selectNextMaterial(int phaseNumber) {
        LinkedHashMap<Material, Double> chances = phaseChances.get(phaseNumber);
        if (chances == null || chances.isEmpty()) {
            List<Material> pool = Phase.of(phaseNumber).getPool();
            if (pool.isEmpty()) {
                return null;
            }
            return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        }
        double total = 0.0;
        List<Map.Entry<Material, Double>> entries = new ArrayList<>();
        for (Map.Entry<Material, Double> entry : chances.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(entry);
                total += entry.getValue();
            }
        }
        if (entries.isEmpty()) {
            return null;
        }
        double roll = ThreadLocalRandom.current().nextDouble(total);
        double cumulative = 0.0;
        for (Map.Entry<Material, Double> entry : entries) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }
        return entries.get(entries.size() - 1).getKey();
    }
}
