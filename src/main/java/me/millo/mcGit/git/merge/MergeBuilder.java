package me.millo.mcGit.git.merge;

import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.diff.BlockChange;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;

public class MergeBuilder {

    private final LinkedHashMap<String, BlockChange> changes = new LinkedHashMap<>();
    private final LinkedHashMap<String, BlockConflict> conflicts = new LinkedHashMap<>();

    private final Branch target;
    private final String author;

    public MergeBuilder(Branch target, String author) {
        this.target = target;
        this.author = author;
    }

    public Merge build() {
        Stack<BlockConflict> conflictStack = new Stack<>();
        conflictStack.addAll(conflicts.values());
        return new Merge(
                target,
                author,
                new ArrayList<>(changes.values()),
                conflictStack
        );
    }

    public void put(String key, BlockChange change) {
        changes.put(key, change);
    }

    public void conflict(String key, BlockConflict conflict) {
        conflicts.put(key, conflict);
    }
}
