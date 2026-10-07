package me.millo.mcGit.git.merge;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.List;

public record BlockConflict(Location location, List<BlockData> desiredBlocks) {
    public BlockConflict(Location location, BlockData... blocks) {
        this(location, List.of(blocks));
    }
}
