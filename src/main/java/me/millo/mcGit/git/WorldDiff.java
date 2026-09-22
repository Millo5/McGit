package me.millo.mcGit.git;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;

public class WorldDiff {
    private final HashMap<Location, BlockModification> blockModifications;

    public WorldDiff() {
        blockModifications = new HashMap<>();
    }

    public void setBlock(Location location, Block old, Block block) {
        BlockModification mod = blockModifications.get(location);
        if (mod != null) {
            mod.setNewBlock(block);
            return;
        }

        blockModifications.put(location, new BlockModification(old, block));
    }

}
