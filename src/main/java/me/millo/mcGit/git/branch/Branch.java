package me.millo.mcGit.git.branch;

import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Branch {

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

    public ArrayList<CommitHash> getTrail(CommitHash target) {
        ArrayList<CommitHash> commits = new ArrayList<>();

        CommitHash current = head;

        for (int i = 0; i < 50; i++) {
            if (current.equals(target)) return commits;

            commits.add(current);

            try {
                Commit commit = Commit.fromHash(current);
                if (commit.getParents().length == 0) {
                    throw new RuntimeException("Commit not found in parent history.");
                }
                current = commit.getParents()[0];
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO: specific errors
            }
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return trail;
    }

    public List<String> getTrailAsStrings() {
        return getTrail().stream().map(CommitHash::toString).toList();
    }
}
