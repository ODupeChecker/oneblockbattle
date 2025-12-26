package com.oneblock.multione;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;

public class LocationKey {
    private final UUID worldId;
    private final int x;
    private final int y;
    private final int z;

    public LocationKey(UUID worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static LocationKey fromLocation(Location location) {
        World world = location.getWorld();
        return new LocationKey(world.getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationKey that = (LocationKey) o;
        return x == that.x && y == that.y && z == that.z && worldId.equals(that.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z);
    }
}
