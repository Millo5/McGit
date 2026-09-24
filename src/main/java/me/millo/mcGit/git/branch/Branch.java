package me.millo.mcGit.git.branch;

import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.git.diff.BlockModification;
import org.bukkit.Location;

import java.io.IOException;

public class Branch {

    private final String name;
    private CommitHash head;

    public Branch(String name, CommitHash head) {
        this.name = name;

        if (head == null) {
            Commit initialCommit = new Commit("Initial Commit", new CommitHash[0], "McGit", new CommitChanges(new Location[0], new BlockModification[0]));
            try {
                initialCommit.save();
                head = initialCommit.getHash();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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
    }
}
