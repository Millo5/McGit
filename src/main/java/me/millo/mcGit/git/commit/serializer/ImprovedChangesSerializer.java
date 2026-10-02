package me.millo.mcGit.git.commit.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.millo.mcGit.git.commit.CommitChanges;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.utility.ByteArray;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ImprovedChangesSerializer extends ChangesSerializer {

    private static final int LATERAL_BITS = 26; // -33,554,432 to 33,554,431
    private static final int VERTICAL_BITS = 9; // 512
    private static final int VERTICAL_OFFSET = 64;

    @Override
    public void serialize(JsonObject root, CommitChanges changes) {
        LinkedHashMap<String, Integer> blockPalette = getBlockPaletteMap(changes);

        LinkedHashMap<String, WorldChanges> worldChanges = new LinkedHashMap<>();

        for (int i = 0; i < changes.locations().length; i++) {
            Location location = changes.locations()[i];
            String worldName = location.getWorld().getName();

            WorldChanges world = worldChanges.computeIfAbsent(
                    worldName,
                    name -> new WorldChanges(worldChanges.size(), new ArrayList<>())
            );

            world.changes().add(new Change(location, changes.modifications()[i]));
        }

        int paletteBitSize = getBitSize(blockPalette.size());
        int worldBitSize = getBitSize(worldChanges.size());

        ByteArray array = new ByteArray();

        // Header
        array.add(paletteBitSize, Integer.SIZE);
        array.add(worldBitSize, Integer.SIZE);

        // Changes grouped by world.
        for (WorldChanges world : worldChanges.values()) {
            array.add(world.index(), worldBitSize);
            array.add(world.changes().size(), Integer.SIZE);

            for (Change change : world.changes()) {
                Location location = change.location();
                BlockModification modification = change.modification();

                int oldBlockIndex = blockPalette.get(
                        modification.getOldBlock().getAsString()
                );

                int newBlockIndex = blockPalette.get(
                        modification.getNewBlock().getAsString()
                );

                array.add(oldBlockIndex, paletteBitSize);
                array.add(newBlockIndex, paletteBitSize);

                array.add(location.getBlockX(), LATERAL_BITS);
                array.add(location.getBlockZ(), LATERAL_BITS);
                array.add(location.getBlockY() + VERTICAL_OFFSET, VERTICAL_BITS);
            }
        }

        JsonArray blocks = new JsonArray(blockPalette.size());
        for (String block : blockPalette.keySet()) {
            blocks.add(block);
        }

        JsonArray worlds = new JsonArray(worldChanges.size());
        for (String world : worldChanges.keySet()) {
            worlds.add(world);
        }

        root.add("blocks", blocks);
        root.add("worlds", worlds);
        root.addProperty("changes", array.compress());
    }

    @Override
    public CommitChanges deserialize(JsonObject root) {
        JsonArray blocks = root.getAsJsonArray("blocks");
        JsonArray worlds = root.getAsJsonArray("worlds");

        ArrayList<String> blockPalette = new ArrayList<>(blocks.size());
        for (int i = 0; i < blocks.size(); i++) {
            blockPalette.add(blocks.get(i).getAsString());
        }

        ArrayList<String> worldPalette = new ArrayList<>(worlds.size());
        for (int i = 0; i < worlds.size(); i++) {
            worldPalette.add(worlds.get(i).getAsString());
        }

        ByteArray array = ByteArray.decompress(root.get("changes").getAsString());

        int paletteBitSize = array.readInt();
        int worldBitSize = array.readInt();

        ArrayList<Location> locations = new ArrayList<>();
        ArrayList<BlockModification> modifications = new ArrayList<>();

        while (array.hasRemaining()) {
            int worldIndex = (int) array.read(worldBitSize);
            int changeCount = array.readInt();

            String worldName = worldPalette.get(worldIndex);

            for (int i = 0; i < changeCount; i++) {
                int oldBlockIndex = (int) array.read(paletteBitSize);
                int newBlockIndex = (int) array.read(paletteBitSize);

                long x = array.readSigned(LATERAL_BITS);
                long z = array.readSigned(LATERAL_BITS);
                long y = array.readSigned(VERTICAL_BITS) - VERTICAL_OFFSET;

                locations.add(new Location(Bukkit.getWorld(worldName), x, y, z));

                modifications.add(new BlockModification(
                        Bukkit.createBlockData(blockPalette.get(oldBlockIndex)),
                        Bukkit.createBlockData(blockPalette.get(newBlockIndex))
                ));
            }
        }

        return new CommitChanges(
                locations.toArray(Location[]::new),
                modifications.toArray(BlockModification[]::new)
        );
    }

    private LinkedHashMap<String, Integer> getBlockPaletteMap(CommitChanges changes) {
        LinkedHashMap<String, Integer> blockPalette = new LinkedHashMap<>();

        for (BlockModification modification : changes.modifications()) {
            blockPalette.computeIfAbsent(modification.getOldBlock().getAsString(), s -> blockPalette.size());
            blockPalette.computeIfAbsent(modification.getNewBlock().getAsString(), s -> blockPalette.size());
        }
        return blockPalette;
    }

    private int getBitSize(int size) {
        return Math.max(1, Integer.SIZE - Integer.numberOfLeadingZeros(size - 1));
    }

    private record Change(Location location, BlockModification modification) {}

    private record WorldChanges(int index, ArrayList<Change> changes) {}
}