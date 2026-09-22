package me.millo.mcGit.git;

import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.git.diff.WorldDiff;
import me.millo.mcGit.utility.Broadcast;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class GitCore {

    private final JavaPlugin plugin;
    private WorldDiff currentDiff;

    private ArrayList<Entity> diffEntities;

    private boolean lock = false;

    public GitCore(JavaPlugin plugin) {
        this.plugin = plugin;

        currentDiff = new WorldDiff();
    }

    public WorldDiff getCurrentDiff() {
        return currentDiff;
    }

    public boolean isLocked() {
        return lock;
    }

    public void displayDiff() {
        if (lock) {
            lock = false;
            Broadcast.message("Hiding diff.");

            diffEntities.forEach(Entity::remove);

            for (final Location location : currentDiff.getBlockModifications().keySet()) {
                BlockModification change = currentDiff.getBlockModifications().get(location);
                var world = location.getWorld();
                if (change.getNewBlock() == null) {
                    world.getBlockAt(location).setType(Material.AIR);
                    continue;
                }
                world.getBlockAt(location).setType(change.getNewBlock().getMaterial());
            }

            return;
        }

        lock = true;
        Broadcast.message("Viewing diff...");

        diffEntities = new ArrayList<>();
        for (final Location location : currentDiff.getBlockModifications().keySet()) {
            BlockModification change = currentDiff.getBlockModifications().get(location);
            Broadcast.message(location);

            var world = location.getWorld();
            world.getBlockAt(location).setType(Material.AIR);

            if (change.getOldBlock() != null) {
                Entity entity = world.spawnEntity(location.clone(), EntityType.BLOCK_DISPLAY);
                diffEntities.add(entity);
                BlockDisplay disp = (BlockDisplay) entity;
                disp.setBlock(change.getOldBlock());
                disp.setTransformation(new Transformation(
                        new Vector3f(0.25f, 0f, 0.25f),
                        new Quaternionf(0, 0, 0 , 1),
                        new Vector3f(0.5f),
                        new Quaternionf(0, 0, 0 , 1)
                ));
            }

            if (change.getNewBlock() != null) {
                Entity entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
                diffEntities.add(entity);
                BlockDisplay disp = (BlockDisplay) entity;
                disp.setBlock(change.getNewBlock());
                disp.setTransformation(new Transformation(
                        new Vector3f(0.25f, 0.5f, 0.25f),
                        new Quaternionf(0, 0, 0 , 1),
                        new Vector3f(0.5f),
                        new Quaternionf(0, 0, 0 , 1)
                ));
            }
        }
    }

    public void sendStatus(CommandSender sender) {
        TextColor color = TextColor.color(125, 20, 50);
        TextColor color4 = TextColor.color(20, 125, 50);
        TextColor color2 = TextColor.color(50, 50, 50);
        TextColor color3 = TextColor.color(50, 150, 150);

        TextComponent text = Component.text("Git Status").color(color2);

        if (currentDiff.getBlockModifications().isEmpty()) {
            text = text.append(
                    Component.text("\nNo active changes.").color(color2)
            );
        } else {
            int amount = currentDiff.getBlockModifications().size();
            text = text.append(
                    Component.text("\n"+amount).color(color3),
                    Component.text(" changes to be committed:").color(color2)
            );

            int i = 0;
            for (Location location : currentDiff.getBlockModifications().keySet()) {
                if (i++ > 20) break;
                BlockModification mod = currentDiff.getBlockModifications().get(location);

                String locStr = " at " + location.getX() + " " + location.getY() + " " + location.getZ();

                if (mod.getOldBlock() == null) {
                    text = text.append(
                            Component.text("\n  added: " + mod.getNewBlock().getMaterial().name() + locStr).color(color4)
                    );
                    continue;
                }
                if (mod.getNewBlock() == null) {
                    text = text.append(
                            Component.text("\n  removed: " + mod.getOldBlock().getMaterial().name() + locStr).color(color)
                    );
                    continue;
                }
                
                text = text.append(
                        Component.text("\n  replaced: " + mod.getOldBlock().getMaterial().name() + " with " + mod.getNewBlock().getMaterial().name() + locStr).color(color4)
                );
            }
        }

        sender.sendMessage(text);
    }
}
