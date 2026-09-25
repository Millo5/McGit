package me.millo.mcGit.listeners;

import me.millo.mcGit.git.GitCore;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockChangeListener implements Listener {

    private final GitCore core;
    public BlockChangeListener(GitCore core) {
        this.core = core;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (core.isNotReady()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                null,
                event.getBlock());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (core.isNotReady()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                event.getBlock(),
                null);
    }

    @EventHandler
    public void blockExplode(BlockExplodeEvent event) {
        if (core.isNotReady()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                event.getBlock(),
                null
        );
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {
        if (core.isNotReady()) {
            event.setCancelled(true);
            return;
        }

        for (Block block : event.blockList()) {
            core.getCurrentDiff().setBlock(
                    block.getLocation(),
                    block,
                    null
            );
        }
    }

}
