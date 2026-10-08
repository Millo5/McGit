package me.millo.mcGit.gui;

import me.millo.mcGit.McGit;
import me.millo.mcGit.git.GitState;
import me.millo.mcGit.utility.TextColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class GitMainMenu extends GuiMenu {

    public GitMainMenu() {
        super(Component.text("McGit").color(TextColors.PRIMARY), 27);

        boolean hasDiff = !McGit.getGitCore().getCurrentDiff().getBlockModifications().isEmpty();
        if (hasDiff) {
            setItem(13, new GuiItem(
                    Material.WRITABLE_BOOK,
                    Component.text("Commit Changes").color(TextColors.PRIMARY),
                    List.of(
                            Component.text("Click to prepare /git commit.").color(TextColors.LIGHT),
                            Component.text("Add your message, then send it.").color(TextColors.DARK)
                    ),
                    this::prepareCommit
            ));
            return;
        }

        if (McGit.getGitCore().getState().equals(GitState.VIEW)) {
            setItem(13, new GuiItem(
                    Material.COMPASS,
                    Component.text("Return To Head").color(TextColors.PRIMARY),
                    List.of(Component.text("Available while viewing an old commit.").color(TextColors.LIGHT)),
                    this::returnToHead
            ));
            return;
        }

        setItem(13, new GuiItem(
                Material.CLOCK,
                Component.text("Branch History").color(TextColors.PRIMARY),
                List.of(Component.text("View commits on this branch.").color(TextColors.LIGHT)),
                player -> new GitHistoryMenu().open(player)
        ));
    }

    private void prepareCommit(Player player) {
        player.closeInventory();
        player.sendMessage(
                Component.text("Click to finish the commit: ").color(TextColors.LIGHT)
                        .append(Component.text("/git commit <message>").color(TextColors.SECONDARY)
                                .hoverEvent(HoverEvent.showText(Component.text("Suggest commit command")))
                                .clickEvent(ClickEvent.suggestCommand("/git commit ")))
        );
    }

    private void returnToHead(Player player) {
        player.closeInventory();
        if (!McGit.getGitCore().getState().equals(GitState.VIEW)) {
            player.sendMessage(Component.text("Already at branch head.").color(TextColors.LIGHT));
            return;
        }

        player.performCommand("git view");
    }
}
