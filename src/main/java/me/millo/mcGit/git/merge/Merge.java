package me.millo.mcGit.git.merge;

import me.millo.mcGit.McGit;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.git.diff.BlockChange;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.listeners.InteractionListener;
import me.millo.mcGit.utility.WorldUtil;
import me.millo.mcGit.utility.messenger.Messages;
import me.millo.mcGit.utility.messenger.Messenger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;

public class Merge {

    private final Branch target;
    private final Branch current;
    private final String author;

    private final ArrayList<BlockChange> changes;
    private final Stack<BlockConflict> conflicts;

    private final Map<Location, BlockModification> conflictChoices = new HashMap<>();
    private final ArrayList<Entity> displayEntities = new ArrayList<>();

    public Merge(Branch target, String author, ArrayList<BlockChange> change, Stack<BlockConflict> conflicts) {
        this.target = target;
        this.author = author;
        this.changes = change;
        this.conflicts = conflicts;
        current = McGit.getGitCore().getBranchHandler().getBranch();
    }

    public boolean resolved() {
        return conflicts.isEmpty();
    }

    public CommitHash commit(Messenger messenger) throws IOException {
        if (!resolved()) throw new IllegalStateException("Merge must be resolved before commiting.");

        hideCurrentConflict();

        for (Map.Entry<Location, BlockModification> entry : conflictChoices.entrySet()) {
            changes.add(new BlockChange(entry.getKey(), entry.getValue()));
        }

        Location[] locations = new Location[changes.size()];
        BlockModification[] modifications = new BlockModification[changes.size()];

        int i = 0;
        for (BlockChange change : changes) {
            locations[i] = change.location();
            modifications[i++] = change.modification();
        }

        CommitChanges changes = new CommitChanges(locations, modifications);
        Commit commit = new Commit(
                "Merge branch '" + target.getName() + "' into '" + current.getName() + "'",
                new CommitHash[] {current.getHeadHash(), target.getHeadHash()},
                author,
                changes
        );
        commit.save();
        commit.apply();
        current.setHead(commit.getHash());
        McGit.getGitCore().getBranchHandler().save();

        messenger.send(Messages.MERGE_COMPLETE);
        messenger.sendInfo("   Commit: " + commit.getHash());
        return commit.getHash();
    }

    public BlockConflict peekConflict() {
        return conflicts.peek();
    }

    public boolean resolved(Location location) {
        return conflictChoices.containsKey(location);
    }

    public void popConflict() {
        conflicts.pop();
    }

    public void showCurrentConflict() {
        hideCurrentConflict();
        BlockConflict conflict = conflicts.peek();

        Location location = conflict.location();
        WorldUtil.setBlockDirectly(location, Material.AIR.createBlockData());

        BlockModification resolvedBlock = conflictChoices.get(location);

        int i = 0;
        float size = 1f / conflict.desiredBlocks().size();
        for (BlockData desiredBlock : conflict.desiredBlocks()) {
            float y = (i++) * size;

            boolean isResolved = resolvedBlock != null && resolvedBlock.getNewBlock().equals(desiredBlock);

            Entity entity = location.getWorld().spawnEntity(location.clone(), EntityType.BLOCK_DISPLAY);
            displayEntities.add(entity);
            BlockDisplay display = (BlockDisplay) entity;
            display.setBlock(desiredBlock);

            float scale = isResolved ? size : size * 0.6f;
            display.setTransformation(new Transformation(
                    new Vector3f(0.5f - scale / 2f, y, 0.5f - scale / 2f),
                    new Quaternionf(0, 0, 0 , 1),
                    new Vector3f(scale),
                    new Quaternionf(0, 0, 0 , 1)
            ));

            entity = location.getWorld().spawnEntity(location.clone()
                    .add(0.5f, y, 0.5f), EntityType.INTERACTION);
            displayEntities.add(entity);
            Interaction interaction = (Interaction) entity;
            interaction.setInteractionWidth(size);
            interaction.setInteractionHeight(size);
            interaction.setResponsive(true);

            InteractionListener.add(interaction.getUniqueId(), (ent) -> {
                conflictChoices.put(location, new BlockModification(conflict.desiredBlocks().getFirst(), desiredBlock));
                showCurrentConflict();
            });
        }
    }

    private void hideCurrentConflict() {
        displayEntities.forEach(entity -> {
            InteractionListener.remove(entity.getUniqueId());
            entity.remove();
        });
        displayEntities.clear();
    }

    public int size() {
        return conflicts.size();
    }
}
