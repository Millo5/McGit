package me.millo.mcGit.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public class WorldUtil {

    public static void setBlockDirectly(Location location, BlockData blockData) {
        BlockState state = ((CraftBlockData) blockData).getState();
        CraftWorld world = (CraftWorld) location.getWorld();
        world.getHandle().setBlock(
                new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                state,
                Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_ALL);

    }

}
