package me.millo.mcGit.git.commit.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.diff.BlockModification;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SimpleChangesSerializer extends ChangesSerializer {

    @Override
    public void serialize(JsonObject root, CommitChanges changes) {
        JsonObject json = new JsonObject();

        JsonArray locations = new JsonArray();
        JsonArray blocks = new JsonArray();
        JsonArray blocks2 = new JsonArray();

        for (int i = 0; i < changes.locations().length; i++) {
            JsonArray loc = new JsonArray();
            loc.add(changes.locations()[i].getWorld().getName());
            loc.add(changes.locations()[i].getBlockX());
            loc.add(changes.locations()[i].getBlockY());
            loc.add(changes.locations()[i].getBlockZ());
            locations.add(loc);

            var block = changes.modifications()[i].getOldBlock();
            var block2 = changes.modifications()[i].getNewBlock();
            blocks.add(block == null ? "air" : block.getAsString(true));
            blocks2.add(block2 == null ? "air" : block2.getAsString(true));
        }

        json.add("locs", locations);
        json.add("old", blocks);
        json.add("new", blocks2);

        root.add("changes", json);
    }

    @Override
    public CommitChanges deserialize(JsonObject root) {
        JsonObject changes = root.getAsJsonObject("changes");

        JsonArray locations = changes.getAsJsonArray("locs");
        JsonArray blocks = changes.getAsJsonArray("old");
        JsonArray blocks2 = changes.getAsJsonArray("new");

        BlockModification[] mods = new BlockModification[locations.size()];
        Location[] locs = new Location[locations.size()];

        for (int i = 0; i < locations.size(); i++) {
            JsonArray loc = locations.get(i).getAsJsonArray();
            World world = Bukkit.getWorld(loc.get(0).getAsString());

            Location location = new Location(
                    world,
                    loc.get(1).getAsInt(),
                    loc.get(2).getAsInt(),
                    loc.get(3).getAsInt()
            );

            locs[i] = location;
            mods[i] = new BlockModification(
                    Bukkit.createBlockData(blocks.get(i).getAsString()),
                    Bukkit.createBlockData(blocks2.get(i).getAsString())
            );
        }

        return new CommitChanges(locs, mods);
    }
}
