package me.millo.mcGit.git.commit.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.utility.ByteArray;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ImprovedChangesSerializer extends ChangesSerializer {

    private static final int LATERAL_BITS = 26; // 67_108_864, -30m to 30m
    private static final int VERTICAL_BITS = 9; // 512, -64 to 320: 384
    private static final int VERTICAL_OFFSET = 64;

    @Override
    public void serialize(JsonObject root, CommitChanges changes) {
//        ArrayList<String> blockPalette = new ArrayList<>();
//        HashMap<String, Integer> blockPaletteMap = getBlockPaletteMap(changes, blockPalette);

        LinkedHashMap<String, Integer> blockPalette = getBlockPaletteMap(changes);

        int paletteBitSize = Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(blockPalette.size() - 1));

        HashMap<String, ByteArray> worlds = new HashMap<>();
        HashMap<String, Integer> worldSizes = new HashMap<>();
        ArrayList<String> worldNames = new ArrayList<>();

        for (int i = 0; i < changes.locations().length; i++) {
            Location location = changes.locations()[i];
            BlockModification modification = changes.modifications()[i];

            int oldBlockIndex = blockPalette.get(modification.getOldBlock().getAsString());
            int newBlockIndex = blockPalette.get(modification.getNewBlock().getAsString());
            ByteArray world = worlds.computeIfAbsent(location.getWorld().getName(), (s) -> new ByteArray());
            worldSizes.putIfAbsent(location.getWorld().getName(), 0);
            worldSizes.computeIfPresent(location.getWorld().getName(), (s, count) -> count+1);

            world.add(oldBlockIndex, paletteBitSize);
            world.add(newBlockIndex, paletteBitSize);
            world.add(location.getBlockX(), LATERAL_BITS);
            world.add(location.getBlockZ(), LATERAL_BITS);
            world.add(location.getBlockY() + VERTICAL_OFFSET, VERTICAL_BITS);
        }

        JsonArray blocks = new JsonArray(blockPalette.size());
        for (String block : blockPalette.keySet()) {
            blocks.add(block);
        }

        ByteArray array = new ByteArray();
        array.add(paletteBitSize, Integer.SIZE);

        root.add("blocks", blocks);
        root.addProperty("changes", array.compress());
    }

    @Override
    public CommitChanges deserialize(JsonObject root) {
        JsonArray blocks = root.getAsJsonArray("blocks");

        ArrayList<String> blockPalette = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            blockPalette.add(blocks.get(i).getAsString());
        }

        String compressed = root.get("changes").getAsString();
        ByteArray array = ByteArray.decompress(compressed);

        int paletteBitSize = array.readInt();
        int modificationCount = array.readInt();

        Location[] locations = new Location[modificationCount];
        BlockModification[] modifications = new BlockModification[modificationCount];

        for (int i = 0; i < modificationCount; i++) {
            int oldBlockIndex = (int) array.read(paletteBitSize);
            int newBlockIndex = (int) array.read(paletteBitSize);

            long x = array.readSigned(LATERAL_BITS);
            long z = array.readSigned(LATERAL_BITS);
            long y = array.readSigned(VERTICAL_BITS) - VERTICAL_BITS;

            // TODO: store world
            locations[i] = new Location(null, 1, 2, 3);
            modifications[i] = new BlockModification(
                    Bukkit.createBlockData(blockPalette.get(oldBlockIndex)),
                    Bukkit.createBlockData(blockPalette.get(newBlockIndex))
            );
        }

        return new CommitChanges(locations, modifications);
    }


    private LinkedHashMap<String, Integer> getBlockPaletteMap(CommitChanges changes) {
        LinkedHashMap<String, Integer> blockPalette = new LinkedHashMap<>();

        for (BlockModification modification : changes.modifications()) {
            blockPalette.computeIfAbsent(modification.getOldBlock().getAsString(), (s) -> blockPalette.size());
            blockPalette.computeIfAbsent(modification.getNewBlock().getAsString(), (s) -> blockPalette.size());
        }
        return blockPalette;
    }

}
