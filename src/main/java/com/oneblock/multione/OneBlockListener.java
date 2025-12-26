package com.oneblock.multione;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OneBlockListener implements Listener {

    private final OneBlockManager manager;

    public OneBlockListener(OneBlockManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        OneBlock ob = manager.getOneBlock(event.getBlock().getLocation());
        if (ob == null) {
            return;
        }
        event.setDropItems(false);
        manager.handleBreak(ob);
    }
}
