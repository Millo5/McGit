package me.millo.mcGit.git.diff;

import me.millo.mcGit.utility.WorldUtil;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockModification {
    private final BlockData oldBlock;
    private BlockData newBlock;

    public BlockModification(Block old, Block now) {
        oldBlock = old == null ? null : old.getBlockData();
        newBlock = now == null ? null : now.getBlockData();
    }

    public BlockModification(BlockData blockData, BlockData blockData1) {
        oldBlock = blockData;
        newBlock = blockData1;
    }

    public BlockData getOldBlock() {
        return oldBlock;
    }

    public BlockData getNewBlock() {
        return newBlock;
    }

    public String getOldBlockString() {
        return WorldUtil.blockString(oldBlock);
    }

    public String getNewBlockString() {
        return WorldUtil.blockString(newBlock);
    }

    public void setNewBlock(BlockData newBlock) {
        this.newBlock = newBlock;
    }
}
