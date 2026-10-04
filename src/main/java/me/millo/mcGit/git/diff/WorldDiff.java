package me.millo.mcGit.git.diff;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.millo.mcGit.McGit;
import me.millo.mcGit.files.FileBank;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.git.GitState;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class WorldDiff {

    private final HashMap<Location, BlockModification> blockModifications;
    private ArrayList<Entity> diffEntities;
    private final GitCore core;

    private boolean changesSinceLastSave = false;

    public WorldDiff(GitCore core) {
        this.core = core;

        blockModifications = new HashMap<>();
        load();
    }

    public void setBlock(Location location, Block old, Block block) {
        BlockModification mod = blockModifications.get(location);
        changesSinceLastSave = true;
        if (mod != null) {
            mod.setNewBlock(block);
            return;
        }

        blockModifications.put(location, new BlockModification(old, block));
    }

    public void commit(String name, String author) throws IOException {
        if (core.getCurrentDiff().getBlockModifications().isEmpty()) {
            Broadcast.message("There are no changes to commit.");
            return;
        }

        Branch branch = core.getBranchHandler().getBranch();
        CommitChanges changes = asCommitChanges();

        Commit commit = new Commit(
                name,
                new CommitHash[]{branch.getHeadHash()},
                author,
                changes
        );

        commit.save();
        branch.setHead(commit.getHash());
        core.getBranchHandler().save();

        blockModifications.clear();
        changesSinceLastSave = true;
    }

    private CommitChanges asCommitChanges() {
        Location[] locations = new Location[blockModifications.size()];
        BlockModification[] modifications = new BlockModification[blockModifications.size()];
        int i = 0;
        for (final Location location : blockModifications.keySet()) {
            BlockModification change = blockModifications.get(location);
            locations[i] = location;
            modifications[i++] = change;
        }

        return new CommitChanges(locations, modifications);
    }

    public void save() {
        if (!changesSinceLastSave) {
            Broadcast.message("No diff changes to save.");
            return;
        }
        changesSinceLastSave = false;

        JsonObject root = new JsonObject();

        CommitChanges changes = asCommitChanges();
        McGit.SERIALIZER.serialize(root, changes);

        try {
            File file = FileBank.getDiffFile();

            Files.writeString(
                    file.toPath(),
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(root)
            );
            Broadcast.message("Saved diff");
        } catch (IOException e) {
            Broadcast.message("Failed to save diff", e);
        }
    }

    public void load() {
        File file = FileBank.getDiffFile();

        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            CommitChanges changes = McGit.SERIALIZER.deserialize(json);

            blockModifications.clear();
            for (int i = 0; i < changes.locations().length; i++) {
                Location location = changes.locations()[i];
                BlockModification mod = changes.modifications()[i];

                blockModifications.put(location, mod);
            }
        } catch (IOException e) {
            Broadcast.message("Failed to load diff", e);
        }
    }

    public void toggleDisplay() {
        WorldDiff currentDiff = core.getCurrentDiff();

        if (core.stateEquals(GitState.DISPLAY)) {
            core.setState(GitState.IDLE);
            Broadcast.message("Hiding diff.");

            diffEntities.forEach(Entity::remove);

            for (final Location location : currentDiff.getBlockModifications().keySet()) {
                BlockModification change = currentDiff.getBlockModifications().get(location);
                var world = location.getWorld();
                if (change.getNewBlock() == null) {
                    world.getBlockAt(location).setType(Material.AIR, false);
                    continue;
                }
                world.getBlockAt(location).setBlockData(change.getNewBlock(), false);
            }

            return;
        }

        if (!core.requireIdle()) return;
        core.setState(GitState.DISPLAY);
        Broadcast.message("Viewing diff...");

        diffEntities = new ArrayList<>();
        for (final Location location : currentDiff.getBlockModifications().keySet()) {
            BlockModification change = currentDiff.getBlockModifications().get(location);

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


    public HashMap<Location, BlockModification> getBlockModifications() {
        return blockModifications;
    }

}
