package me.millo.mcGit.listeners;

import me.millo.mcGit.McGit;
import me.millo.mcGit.git.GitCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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

}
