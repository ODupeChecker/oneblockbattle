package com.oneblock.multione;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;

import java.util.UUID;

public class OneBlock {
    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;
    private int phase;
    private int brokenInPhase;
    private boolean chestPlaced;
    private BossBar bossBar;

    public OneBlock(UUID worldId, int x, int y, int z, int phase, int brokenInPhase, boolean chestPlaced) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.phase = phase;
        this.brokenInPhase = brokenInPhase;
        this.chestPlaced = chestPlaced;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getBrokenInPhase() {
        return brokenInPhase;
    }

    public void setBrokenInPhase(int brokenInPhase) {
        this.brokenInPhase = brokenInPhase;
    }

    public void incrementBrokenInPhase() {
        this.brokenInPhase++;
    }

    public boolean isChestPlaced() {
        return chestPlaced;
    }

    public void setChestPlaced(boolean chestPlaced) {
        this.chestPlaced = chestPlaced;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldId);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }
}
