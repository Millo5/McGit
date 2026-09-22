package me.millo.mcGit.listeners;

import me.millo.mcGit.McGit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockChangeListener implements Listener {

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (McGit.getGitCore().isLocked()) {
            event.setCancelled(true);
            return;
        }

        McGit.getGitCore().getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                null,
                event.getBlock());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (McGit.getGitCore().isLocked()) {
            event.setCancelled(true);
            return;
        }

        McGit.getGitCore().getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                event.getBlock(),
                null);
    }

}
