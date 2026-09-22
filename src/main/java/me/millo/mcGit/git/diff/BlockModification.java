package me.millo.mcGit.git.diff;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockModification {
    private final BlockData oldBlock;
    private BlockData newBlock;

    public BlockModification(Block old, Block now) {
        oldBlock = old == null ? null : old.getBlockData();
        newBlock = now == null ? null : now.getBlockData();
    }

    public BlockData getOldBlock() {
        return oldBlock;
    }

    public BlockData getNewBlock() {
        return newBlock;
    }

    public void setNewBlock(Block newBlock) {
        this.newBlock = newBlock.getBlockData();
    }
}
