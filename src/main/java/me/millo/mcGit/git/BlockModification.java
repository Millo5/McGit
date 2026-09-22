package me.millo.mcGit.git;

import org.bukkit.block.Block;

public class BlockModification {
    private final Block oldBlock;
    private Block newBlock;

    public BlockModification(Block old, Block now) {
        oldBlock = old;
        newBlock = now;
    }

    public Block getOldBlock() {
        return oldBlock;
    }

    public Block getNewBlock() {
        return newBlock;
    }

    public void setNewBlock(Block newBlock) {
        this.newBlock = newBlock;
    }
}
