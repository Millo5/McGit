package me.millo.mcGit.git.commit;

public class Commit {

    private final CommitHash hash;
    private final String name;
    private final CommitHash[] parents;
    private final long timestamp;
    private final String author;
    private final CommitChanges changes;

    public Commit(String name, CommitHash[] parents, String author, CommitChanges changes) {
        this.hash = new CommitHash();
        this.name = name;
        this.parents = parents;
        this.timestamp = System.currentTimeMillis();
        this.author = author;
        this.changes = changes;
    }

    public CommitHash getHash() {
        return hash;
    }
}
