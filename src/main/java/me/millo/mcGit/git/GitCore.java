package me.millo.mcGit.git;

import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.git.diff.WorldDiff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GitCore {

    private final JavaPlugin plugin;

    private WorldDiff currentDiff;
    private Branch branch;

    private boolean lock = false;

    public GitCore(JavaPlugin plugin) {
        this.plugin = plugin;

        currentDiff = new WorldDiff();
        branch = new Branch("master", null);
    }

    public void sendStatus(CommandSender sender) {
        TextColor color = TextColor.color(195, 70, 90);
        TextColor color4 = TextColor.color(70, 195, 90);
        TextColor color2 = TextColor.color(80, 80, 80);
        TextColor color3 = TextColor.color(80, 150, 150);

        TextComponent text = Component.text("Git Status").color(color2);

        text = text.append(
                Component.text("\nCurrent Branch: ").color(color2),
                Component.text(branch.getName()).color(color3)
        );

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

    public WorldDiff getCurrentDiff() {
        return currentDiff;
    }

    public boolean isLocked() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
}
