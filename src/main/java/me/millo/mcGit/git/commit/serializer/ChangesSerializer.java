package me.millo.mcGit.git.commit.serializer;

import com.google.gson.JsonObject;
import me.millo.mcGit.git.commit.CommitChanges;

public abstract class ChangesSerializer {

    public abstract void serialize(JsonObject root, CommitChanges changes);

    public abstract CommitChanges deserialize(JsonObject root);

}
