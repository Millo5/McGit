package me.millo.mcGit.git.branch;

import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;

public class Branch {

    private final String name;
    private final CommitHash head;

    public Branch(String name, CommitHash head) {
        this.name = name;

        if (head == null) {
            Commit initialCommit = new Commit("Initial Commit", new CommitHash[0], "McGit", new CommitChanges());
            head = initialCommit.getHash();
        }

        this.head = head;
    }

    public String getName() {
        return name;
    }

    public CommitHash getHeadHash() {
        return head;
    }
}
