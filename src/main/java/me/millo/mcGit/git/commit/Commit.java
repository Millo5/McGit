package me.millo.mcGit.git.commit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.millo.mcGit.files.FileBank;
import me.millo.mcGit.git.commit.serializer.SimpleChangesSerializer;
import me.millo.mcGit.utility.Broadcast;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public void save() throws IOException {
        Path target = FileBank.getCommitFolder().resolve(hash.toString());

        if (target.toFile().exists()) {
            Broadcast.message("Commit " + hash + " already exists!");
        }


        Files.createDirectories(target.getParent());
        target.toFile().createNewFile();

        JsonObject root = new JsonObject();
        root.addProperty("hash", hash.toString());
        root.addProperty("name", name);
        root.addProperty("author", author);
        root.addProperty("timestamp", timestamp);

        JsonArray parents = new JsonArray();
        for (CommitHash parent : this.parents) {
            parents.add(parent.toString());
        }
        root.add("parents", parents);

        new SimpleChangesSerializer().serialize(root, this.changes);

        try (Writer writer = Files.newBufferedWriter(target)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, writer);
        }
    }
}
