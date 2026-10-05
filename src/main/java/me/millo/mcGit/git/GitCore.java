package me.millo.mcGit.git;

import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.git.branch.BranchOperations;
import me.millo.mcGit.git.diff.BlockModification;
import me.millo.mcGit.git.diff.WorldDiff;
import me.millo.mcGit.utility.Broadcast;
import me.millo.mcGit.utility.TextColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GitCore {

    private final WorldDiff currentDiff;
    private final BranchHandler branchHandler;
    private final BranchOperations branchOperations = new BranchOperations();

    private GitState state = GitState.IDLE;

    public GitCore() {
        this.branchHandler = new BranchHandler();

        currentDiff = new WorldDiff(this);
        new GitInitializer(this).run();
    }

    public void sendStatus(CommandSender sender) {
        for (Location location : currentDiff.getBlockModifications().keySet()) {
            BlockModification mod = currentDiff.getBlockModifications().get(location);

            var world = location.getWorld();
            if (mod.getNewBlock() == null) continue;

            Entity entity = world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
            BlockDisplay display = (BlockDisplay) entity;
            display.setBlock(mod.getNewBlock());
            display.setGlowing(true);
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(0, 0, 0 , 1),
                    new Vector3f(0.999f),
                    new Quaternionf(0, 0, 0 , 1)
            ));
        }

        TextComponent text = Component.text("Git Status").color(TextColors.PRIMARY);

        text = text.append(
                Component.text("\nCurrent Branch: ").color(TextColors.LIGHT),
                Component.text(branchHandler.getBranch().getName()).color(TextColors.PRIMARY)
        );

        if (currentDiff.getBlockModifications().isEmpty()) {
            text = text.append(
                    Component.text("\nNo active changes.").color(TextColors.LIGHT)
            );
        } else {
            int amount = currentDiff.getBlockModifications().size();
            text = text.append(
                    Component.text("\n"+amount).color(TextColors.PRIMARY),
                    Component.text(" changes to be committed.").color(TextColors.LIGHT)
            );
        }

        sender.sendMessage(text);
    }

    public WorldDiff getCurrentDiff() {
        return currentDiff;
    }

    public void setState(GitState state) {
        this.state = state;
    }

    public boolean stateEquals(GitState state) {
        return this.state == state;
    }

    public BranchHandler getBranchHandler() {
        return branchHandler;
    }

    public BranchOperations getBranchOperations() {
        return branchOperations;
    }

    public boolean isIdle() {
        return state == GitState.IDLE;
    }

    /**
     * Returns true when the state is idle.
     * Otherwise, returns false and broadcasts an error message.
     * @return
     */
    public boolean requireIdle() {
        if (isIdle()) return true;

        Broadcast.message("Git State is required to be idle. Current: " + state);

        return false;
    }
}
