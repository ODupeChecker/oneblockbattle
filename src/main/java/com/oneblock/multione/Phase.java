package com.oneblock.multione;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Phase {
    PHASE_1(1, Arrays.asList(
            Material.DIRT,
            Material.STONE,
            Material.OAK_LOG
    ), Arrays.asList(
            new ItemStack(Material.WATER_BUCKET, 1),
            new ItemStack(Material.OAK_SAPLING, 1)
    )),
    PHASE_2(2, Arrays.asList(
            Material.OAK_LOG,
            Material.IRON_ORE,
            Material.COAL_ORE
    ), Arrays.asList(
            new ItemStack(Material.DIAMOND_SWORD, 1),
            new ItemStack(Material.GOLDEN_APPLE, 1)
    )),
    PHASE_3(3, Arrays.asList(
            Material.OAK_LOG,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    ), Arrays.asList(
            new ItemStack(Material.GOLDEN_APPLE, 2),
            new ItemStack(Material.OAK_LOG, 64)
    )),
    PHASE_4(4, Arrays.asList(
            Material.OAK_LOG,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_REDSTONE_ORE
    ), Arrays.asList(
            new ItemStack(Material.GOLDEN_APPLE, 3)
    )),
    PHASE_5(5, Arrays.asList(
            Material.OAK_LOG,
            Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE
    ), Arrays.asList(
            new ItemStack(Material.GOLDEN_APPLE, 1)
    )),
    PHASE_6(6, Collections.emptyList(), Collections.emptyList());

    public static final int BLOCKS_PER_PHASE = 550;

    private final int number;
    private final List<Material> pool;
    private final List<ItemStack> chestLoot;

    Phase(int number, List<Material> pool, List<ItemStack> chestLoot) {
        this.number = number;
        this.pool = pool;
        this.chestLoot = chestLoot;
    }

    public int getNumber() {
        return number;
    }

    public List<Material> getPool() {
        return pool;
    }

    public List<ItemStack> getChestLoot() {
        return new ArrayList<>(chestLoot);
    }

    public static Phase of(int number) {
        for (Phase phase : values()) {
            if (phase.number == number) {
                return phase;
            }
        }
        return PHASE_6;
    }
}
