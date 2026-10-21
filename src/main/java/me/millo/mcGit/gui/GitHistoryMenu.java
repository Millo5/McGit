package me.millo.mcGit.gui;

import me.millo.mcGit.McGit;
import me.millo.mcGit.exceptions.CommitNotFoundException;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.utility.TextColors;
import me.millo.mcGit.utility.messenger.Messenger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GitHistoryMenu extends GuiMenu {

    private static final int MAX_COMMITS = 27;

    public GitHistoryMenu() {
        super(Component.text("Branch History").color(TextColors.PRIMARY), 27);

        ArrayList<Commit> commits = loadFirstParentHistory();
        for (int i = 0; i < commits.size() && i < MAX_COMMITS; i++) {
            Commit commit = commits.get(i);
            setItem(i, new GuiItem(
                    i == 0 ? Material.LIME_DYE : Material.PAPER,
                    Component.text(commit.getMessage()).color(TextColors.PRIMARY),
                    List.of(
                            Component.text(commit.getHash().toString()).color(TextColors.DARK),
                            Component.text("Click to view this commit.").color(TextColors.LIGHT)
                    ),
                    player -> {
                        player.closeInventory();
                        player.performCommand("git view " + commit.getHash());
                    }
            ));
        }
    }

    private ArrayList<Commit> loadFirstParentHistory() {
        ArrayList<Commit> commits = new ArrayList<>();
        CommitHash hash = McGit.getGitCore().getBranchHandler().getBranch().getHeadHash();

        while (hash != null && commits.size() < MAX_COMMITS) {
            try {
                Commit commit = Commit.fromHash(hash);
                commits.add(commit);

                CommitHash[] parents = commit.getParents();
                hash = parents.length == 0 ? null : parents[0];
            } catch (CommitNotFoundException e) {
                e.send(Messenger.createOps());
                break;
            }
        }

        return commits;
    }
}
