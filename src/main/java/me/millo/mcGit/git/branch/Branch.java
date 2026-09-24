package me.millo.mcGit.git.branch;

import me.millo.mcGit.git.commit.CommitHash;

public class Branch {

    private final String name;
    private CommitHash head;

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
    }
}
