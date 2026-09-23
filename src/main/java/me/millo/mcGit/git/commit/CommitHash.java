package me.millo.mcGit.git.commit;

import java.util.Objects;
import java.util.UUID;

public class CommitHash {
    private final UUID uuid;

    public CommitHash() {
        this(UUID.randomUUID());
    }

    public CommitHash(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitHash that = (CommitHash) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
