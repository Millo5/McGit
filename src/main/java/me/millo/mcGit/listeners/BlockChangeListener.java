package me.millo.mcGit.listeners;

import me.millo.mcGit.git.GitCore;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockChangeListener implements Listener {

    private final GitCore core;
    public BlockChangeListener(GitCore core) {
        this.core = core;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        if (!core.getState().isIdle()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                event.getBlockReplacedState().getBlockData(),
                event.getBlockPlaced().getBlockData());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!core.getState().isIdle()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getBlock().getLocation(),
                event.getBlock().getBlockData(),
                null);
    }

    @EventHandler
    public void blockExplode(BlockExplodeEvent event) {
        if (!core.getState().isIdle()) {
            event.setCancelled(true);
            return;
        }

        core.getCurrentDiff().setBlock(
                event.getExplodedBlockState().getLocation(),
                event.getExplodedBlockState().getBlockData(),
                null
        );
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {
        if (!core.getState().isIdle()) {
            event.setCancelled(true);
            return;
        }

        for (Block block : event.blockList()) {
            core.getCurrentDiff().setBlock(
                    block.getLocation(),
                    block.getBlockData(),
                    null
            );
        }
    }

    @EventHandler
    public void multiPlace(BlockMultiPlaceEvent event) {
        if (!core.getState().isIdle()) {
            event.setCancelled(true);
            return;
        }

        for (BlockState replacedBlockState : event.getReplacedBlockStates()) {
            core.getCurrentDiff().setBlock(
                    replacedBlockState.getLocation(),
                    replacedBlockState.getBlockData(),
                    replacedBlockState.getBlock().getBlockData()
            );
        }

    }

}
