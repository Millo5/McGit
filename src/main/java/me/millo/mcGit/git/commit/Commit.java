package me.millo.mcGit.git.commit;

import com.google.gson.*;
import me.millo.mcGit.files.FileBank;
import me.millo.mcGit.git.commit.serializer.ChangesSerializer;
import me.millo.mcGit.git.commit.serializer.SimpleChangesSerializer;
import me.millo.mcGit.utility.Broadcast;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

public class Commit {

    private static final ChangesSerializer SERIALIZER = new SimpleChangesSerializer();
    private static final ArrayList<String> foundHashesCache = new ArrayList<>();
    private static boolean cacheDirty = false;

    private final CommitHash hash;
    private final String message;
    private final CommitHash[] parents;
    private final long timestamp;
    private final String author;
    private final CommitChanges changes;

    public Commit(String message, CommitHash[] parents, String author, CommitChanges changes) {
        this(new CommitHash(), message, parents, System.currentTimeMillis(), author, changes);
    }

    public Commit(CommitHash hash, String message, CommitHash[] parents, long timestamp, String author, CommitChanges changes) {
        this.hash = hash;
        this.message = message;
        this.parents = parents;
        this.timestamp = timestamp;
        this.author = author;
        this.changes = changes;

        dirtyCache();
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
        root.addProperty("message", message);
        root.addProperty("author", author);
        root.addProperty("timestamp", timestamp);

        JsonArray parents = new JsonArray();
        for (CommitHash parent : this.parents) {
            parents.add(parent.toString());
        }
        root.add("parents", parents);

        SERIALIZER.serialize(root, this.changes);

        try (Writer writer = Files.newBufferedWriter(target)) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create()
                    .toJson(root, writer);
        }

        dirtyCache();
    }

    public static Commit fromHash(CommitHash hash) throws IOException {
        Path target = FileBank.getCommitFolder().resolve(hash.toString());

        if (!target.toFile().exists()) {
            Broadcast.message("Commit " + hash + " does not exist!");
            throw new RuntimeException();
        }

        try (FileReader reader = new FileReader(target.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray parentsArray = json.getAsJsonArray("parents");
            CommitHash[] parents = new CommitHash[parentsArray.size()];
            int i = 0;
            for (JsonElement element : parentsArray) {
                parents[i++] = new CommitHash(UUID.fromString(element.getAsString()));
            }


            CommitChanges changes = SERIALIZER.deserialize(json);
            return new Commit(
                    new CommitHash(UUID.fromString(json.get("hash").getAsString())),
                    json.get("message").getAsString(),
                    parents,
                    json.get("timestamp").getAsLong(),
                    json.get("author").getAsString(),
                    changes
            );
        }
    }

    public void revert() {
        for (int i = 0; i < changes.locations().length; i++) {
            changes.locations()[i].getBlock().setBlockData(changes.modifications()[i].getOldBlock(), false);
        }
    }

    public void apply() {
        for (int i = 0; i < changes.locations().length; i++) {
            changes.locations()[i].getBlock().setBlockData(changes.modifications()[i].getNewBlock(), false);
        }
    }

    public String getMessage() {
        return message;
    }

    public CommitHash[] getParents() {
        return parents;
    }

    public void dirtyCache() {
        cacheDirty = true;
    }

    public boolean parentsContain(CommitHash hash) throws IOException {
        for (CommitHash parent : parents) {
            if (parent == hash) return true;
            return Commit.fromHash(parent).parentsContain(hash);
        }
        return false;
    }

    public static ArrayList<String> getFoundHashes() {
        if (!cacheDirty) return foundHashesCache;
        cacheDirty = false;

        foundHashesCache.clear();
        try (Stream<Path> files = Files.list(FileBank.getCommitFolder())){
            for (Path path : files.toList()) {
                foundHashesCache.add(path.toFile().getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return foundHashesCache;
    }
}
