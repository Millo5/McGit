package me.millo.mcGit.git.diff;

import me.millo.mcGit.McGit;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.utility.Broadcast;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldDiff {

    private final HashMap<Location, BlockModification> blockModifications;
    private boolean displayed = false;
    private ArrayList<Entity> diffEntities;

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

    public HashMap<Location, BlockModification> getBlockModifications() {
        return blockModifications;
    }

    public void toggleDisplay() {
        GitCore core = McGit.getGitCore();
        if (core.isLocked()) return;

        WorldDiff currentDiff = core.getCurrentDiff();

        if (displayed) {
            displayed = false;
            core.setLock(false);
            Broadcast.message("Hiding diff.");

            diffEntities.forEach(Entity::remove);

            for (final Location location : currentDiff.getBlockModifications().keySet()) {
                BlockModification change = currentDiff.getBlockModifications().get(location);
                var world = location.getWorld();
                if (change.getNewBlock() == null) {
                    world.getBlockAt(location).setType(Material.AIR);
                    continue;
                }
                world.getBlockAt(location).setType(change.getNewBlock().getMaterial());
            }

            return;
        }

        core.setLock(true);
        Broadcast.message("Viewing diff...");

        diffEntities = new ArrayList<>();
        for (final Location location : currentDiff.getBlockModifications().keySet()) {
            BlockModification change = currentDiff.getBlockModifications().get(location);
            Broadcast.message(location);

            var world = location.getWorld();
            world.getBlockAt(location).setType(Material.AIR);

            if (change.getOldBlock() != null) {
                Entity entity = world.spawnEntity(location.clone(), EntityType.BLOCK_DISPLAY);
                diffEntities.add(entity);
                BlockDisplay display = (BlockDisplay) entity;
                display.setBlock(change.getOldBlock());
                display.setTransformation(new Transformation(
                        new Vector3f(0.25f, 0f, 0.25f),
                        new Quaternionf(0, 0, 0 , 1),
                        new Vector3f(0.5f),
                        new Quaternionf(0, 0, 0 , 1)
                ));
            }

            if (change.getNewBlock() != null) {
                Entity entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
                diffEntities.add(entity);
                BlockDisplay display = (BlockDisplay) entity;
                display.setBlock(change.getNewBlock());
                display.setTransformation(new Transformation(
                        new Vector3f(0.25f, 0.5f, 0.25f),
                        new Quaternionf(0, 0, 0 , 1),
                        new Vector3f(0.5f),
                        new Quaternionf(0, 0, 0 , 1)
                ));
            }
        }
    }
}
