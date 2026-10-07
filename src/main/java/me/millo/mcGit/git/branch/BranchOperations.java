package me.millo.mcGit.git.branch;

import me.millo.mcGit.exceptions.CommitNotFoundException;
import me.millo.mcGit.exceptions.McGitException;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.git.diff.BlockChange;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.git.merge.BlockConflict;
import me.millo.mcGit.git.merge.Merge;
import me.millo.mcGit.git.merge.MergeBuilder;
import me.millo.mcGit.utility.WorldUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.IOException;
import java.util.*;

public class BranchOperations {

    public void checkout(BranchHandler branches, Branch target) throws McGitException {
        Branch current = branches.getBranch();
        if (current == target || current.getName().equals(target.getName())) {
            branches.setBranch(target);
            return;
        }

        CommitHash base = findMutualParent(current, target);
        applyChanges(collapseChanges(base, current.getHeadHash()), false);
        applyChanges(collapseChanges(base, target.getHeadHash()), true);
        branches.setBranch(target);
        branches.save();
    }

    public Merge merge(BranchHandler branches, Branch target, String author) throws McGitException {
        Branch current = branches.getBranch();
        if (current.getName().equals(target.getName())) {
            throw new McGitException("Cannot merge a branch into itself.");
        }

        MergeBuilder builder = new MergeBuilder(target, author);

        CommitHash base = findMutualParent(current, target);
        LinkedHashMap<String, BlockChange> currentChanges = collapseChanges(base, current.getHeadHash());
        LinkedHashMap<String, BlockChange> targetChanges = collapseChanges(base, target.getHeadHash());

        for (Map.Entry<String, BlockChange> entry : targetChanges.entrySet()) {
            BlockChange targetChange = entry.getValue();
            BlockChange currentChange = currentChanges.get(entry.getKey());

            if (currentChange == null) {
                builder.put(entry.getKey(), targetChange);
                continue;
            }

            BlockData targetBlockData = targetChange.modification().getNewBlock();
            BlockData currentBlockData = currentChange.modification().getNewBlock();

            if (WorldUtil.blockString(currentBlockData)
                    .equals(WorldUtil.blockString(targetBlockData))) {
                continue;
            }

            builder.conflict(entry.getKey(), new BlockConflict(targetChange.location(), currentBlockData, targetBlockData));
        }

        return builder.build();
    }

    public CommitHash rebase(BranchHandler branches, Branch target, String author) throws McGitException, IOException {
        Branch current = branches.getBranch();
        if (current.getName().equals(target.getName())) {
            throw new McGitException("Cannot rebase a branch onto itself.");
        }

        CommitHash base = findMutualParent(current, target);
        LinkedHashMap<String, BlockChange> targetChanges = collapseChanges(base, target.getHeadHash());

        Commit commit = new Commit(
                "Rebase " + current.getName() + " onto " + target.getName(),
                new CommitHash[]{current.getHeadHash()},
                author,
                toCommitChanges(targetChanges)
        );
        commit.save();
        applyChanges(targetChanges, true);
        current.setHead(commit.getHash());
        branches.save();
        return commit.getHash();
    }

    public CommitHash findMutualParent(Branch current, Branch target) throws McGitException {
        HashSet<CommitHash> currentAncestors = collectFirstParentAncestors(current.getHeadHash());
        CommitHash hash = target.getHeadHash();

        while (true) {
            if (currentAncestors.contains(hash)) return hash;

            Commit commit = fromHash(hash);
            if (commit.getParents().length == 0) break;
            hash = commit.getParents()[0];
        }

        throw new McGitException("Branches do not have a mutual parent.");
    }

    private HashSet<CommitHash> collectFirstParentAncestors(CommitHash head) throws McGitException {
        HashSet<CommitHash> ancestors = new HashSet<>();
        CommitHash hash = head;

        while (true) {
            ancestors.add(hash);

            Commit commit = fromHash(hash);
            if (commit.getParents().length == 0) return ancestors;
            hash = commit.getParents()[0];
        }
    }

    private LinkedHashMap<String, BlockChange> collapseChanges(CommitHash base, CommitHash head) throws McGitException {
        ArrayList<CommitHash> trail = firstParentTrail(base, head);
        LinkedHashMap<String, BlockChange> collapsed = new LinkedHashMap<>();

        for (CommitHash hash : trail) {
            Commit commit = fromHash(hash);
            CommitChanges changes = commit.getChanges();

            for (int i = 0; i < changes.locations().length; i++) {
                Location location = changes.locations()[i];
                BlockModification modification = changes.modifications()[i];
                String key = locationKey(location);

                BlockChange existing = collapsed.get(key);
                if (existing == null) {
                    collapsed.put(key, new BlockChange(location, modification));
                    continue;
                }

                collapsed.put(key, new BlockChange(
                        location,
                        new BlockModification(existing.modification().getOldBlock(), modification.getNewBlock())
                ));
            }
        }

        return collapsed;
    }

    private ArrayList<CommitHash> firstParentTrail(CommitHash base, CommitHash head) throws McGitException {
        ArrayList<CommitHash> trail = new ArrayList<>();
        CommitHash current = head;

        while (!current.equals(base)) {
            trail.add(current);
            Commit commit = fromHash(current);
            if (commit.getParents().length == 0) {
                throw new McGitException("Commit " + base + " not found in first-parent trail.");
            }
            current = commit.getParents()[0];
        }

        Collections.reverse(trail);
        return trail;
    }

    private void applyChanges(LinkedHashMap<String, BlockChange> changes, boolean applyNewBlock) {
        for (BlockChange change : changes.values()) {
            BlockData block = applyNewBlock
                    ? change.modification().getNewBlock()
                    : change.modification().getOldBlock();

            if (block == null) {
                WorldUtil.setBlockDirectly(change.location(), Material.AIR.createBlockData());
                continue;
            }

            WorldUtil.setBlockDirectly(change.location(), block);
        }
    }

    private CommitChanges toCommitChanges(LinkedHashMap<String, BlockChange> changes) {
        Location[] locations = new Location[changes.size()];
        BlockModification[] modifications = new BlockModification[changes.size()];

        int i = 0;
        for (BlockChange change : changes.values()) {
            locations[i] = change.location();
            modifications[i] = change.modification();
            i++;
        }

        return new CommitChanges(locations, modifications);
    }

    private Commit fromHash(CommitHash hash) throws McGitException {
        try {
            return Commit.fromHash(hash);
        } catch (CommitNotFoundException e) {
            throw new McGitException(e.getMessage());
        }
    }

    private String locationKey(Location location) {
        String worldName = location.getWorld() == null ? "" : location.getWorld().getName();
        return worldName + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

}
