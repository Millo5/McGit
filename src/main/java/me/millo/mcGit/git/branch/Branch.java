package me.millo.mcGit.git.branch;

import me.millo.mcGit.exceptions.CommitNotFoundException;
import me.millo.mcGit.exceptions.CommitNotInBranch;
import me.millo.mcGit.exceptions.McGitException;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Branch {
    private static final int MAX_PARENT_ITERATIONS = 50_000;

    private final String name;
    private CommitHash head;

    private ArrayList<CommitHash> trail;

    public Branch(String name, CommitHash head) {
        this.name = name;
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public CommitHash getHeadHash() {
        return head;
    }

    public void setHead(CommitHash head) {
        this.head = head;
        trail = null;
    }

    public ArrayList<CommitHash> getTrail(CommitHash target) throws McGitException {
        ArrayList<CommitHash> commits = new ArrayList<>();

        CommitHash current = head;

        for (int i = 0; i < MAX_PARENT_ITERATIONS; i++) {
            if (current.equals(target)) return commits;

            commits.add(current);

            Commit commit = Commit.fromHash(current);
            if (commit.getParents().length == 0) {
                throw new CommitNotInBranch(target, this);
            }
            current = commit.getParents()[0];
        }

        throw new RuntimeException("Maximum tries exceeded");
    }

    public ArrayList<CommitHash> getTrail() {
        if (trail != null && trail.getFirst().equals(head)) return trail;

        trail = new ArrayList<>();
        Stack<CommitHash> remaining = new Stack<>();
        remaining.add(head);

        while (!remaining.isEmpty()) {
            CommitHash current = remaining.pop();

            trail.add(current);
            try {
                Commit commit = Commit.fromHash(current);
                trail.addAll(Arrays.asList(commit.getParents()));
                remaining.addAll(Arrays.asList(commit.getParents()));
            } catch (CommitNotFoundException e) {
                e.broadcast();
            }
        }

        return trail;
    }

    public List<String> getTrailAsStrings() {
        return getTrail().stream().map(CommitHash::toString).toList();
    }
}
