package me.millo.mcGit.git;

import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.git.branch.BranchOperations;
import me.millo.mcGit.git.diff.WorldDiff;
import me.millo.mcGit.utility.TextColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

public class GitCore {

    private final WorldDiff currentDiff;
    private final BranchHandler branchHandler;
    private final BranchOperations branchOperations = new BranchOperations();

    private final GitStateHandler stateHandler = new GitStateHandler();

    public GitCore() {
        this.branchHandler = new BranchHandler();

        currentDiff = new WorldDiff(this);
        new GitInitializer(this).run();
    }

    public void sendStatus(CommandSender sender) {
        TextComponent text = Component.text("Git Status").color(TextColors.PRIMARY);

        text = text.append(
                Component.text("\nCurrent Branch: ").color(TextColors.LIGHT),
                Component.text(branchHandler.getBranch().getName()).color(TextColors.PRIMARY)
        );

        if (stateHandler.isIdle() || stateHandler.equals(GitState.DISPLAY)) {
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
        }
        if (stateHandler.equals(GitState.MERGE_CONFLICT)) {
            text = text.append(
                    Component.text("\nResolving Merge Conflict").color(TextColors.LIGHT),
                    Component.text("\n"+stateHandler.getActiveMerge().size()).color(TextColors.PRIMARY),
                    Component.text(" conflicts to resolve").color(TextColors.LIGHT),
                    Component.text("\n\nUse ").color(TextColors.LIGHT),
                    Component.text("/git conflict tp").color(TextColors.SECONDARY)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to execute")))
                            .clickEvent(ClickEvent.runCommand("/git conflict tp")),
                    Component.text(" to teleport to the current conflict.").color(TextColors.LIGHT),
                    Component.text("\nUse ").color(TextColors.LIGHT),
                    Component.text("/git conflict resolve").color(TextColors.SECONDARY)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to execute")))
                            .clickEvent(ClickEvent.runCommand("/git conflict resolve")),
                    Component.text(" to go to the next conflict.").color(TextColors.LIGHT)
            );
        }

        sender.sendMessage(text);
    }

    public WorldDiff getCurrentDiff() {
        return currentDiff;
    }

    public BranchHandler getBranchHandler() {
        return branchHandler;
    }

    public BranchOperations getBranchOperations() {
        return branchOperations;
    }

    public GitStateHandler getState() {
        return stateHandler;
    }
}
