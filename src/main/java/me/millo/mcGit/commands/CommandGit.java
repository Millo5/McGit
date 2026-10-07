package me.millo.mcGit.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.millo.mcGit.McGit;
import me.millo.mcGit.exceptions.CommitNotFoundException;
import me.millo.mcGit.exceptions.McGitException;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.git.merge.Merge;
import me.millo.mcGit.utility.Broadcast;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandGit {

    public static void register(Commands dispatcher) {

        dispatcher.register(
                Commands.literal("git")
                        .executes(ctx -> {
                            ctx.getSource().getSender().sendMessage("Basic git command");
                            return 1;
                        })
                        .then(Commands.literal("diff")
                                .executes(ctx -> {
                                    McGit.getGitCore().getCurrentDiff().toggleDisplay();
                                    return 1;
                                }))
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    McGit.getGitCore().sendStatus(ctx.getSource().getSender());
                                    return 1;
                                }))
                        .then(branchSubCommand())
                        .then(Commands.literal("commit")
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                    .executes(CommandGit::commit)))
                        .then(Commands.literal("log")
                                .executes(CommandGit::log))
                        .then(Commands.literal("merge")
                                .then(Commands.argument("branch", new BranchArgumentType())
                                        .executes(CommandGit::merge)))
                        .then(Commands.literal("rebase")
                                .then(Commands.argument("branch", new BranchArgumentType())
                                        .executes(CommandGit::rebase)))
                        .then(conflictSubCommand())
                        .then(Commands.literal("apply")
                                .then(Commands.argument("hash", new CommitArgumentType())
                                        .executes(ctx -> {
                                            if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;
                                            Commit commit = CommitArgumentType.getCommit(ctx, "hash");
                                            commit.apply();
                                            return 1;
                                        })))
                        .then(Commands.literal("revert")
                                .then(Commands.argument("hash", new CommitArgumentType())
                                        .executes(ctx -> {
                                            if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;
                                            Commit commit = CommitArgumentType.getCommit(ctx, "hash");
                                            commit.revert();
                                            return 1;
                                        })))
                        .then(Commands.literal("rollback")
                                .then(Commands.argument("commit", new CommitArgumentType(true))
                                        .executes(CommandGit::rollback)))
                        .build()
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> branchSubCommand() {
        return Commands.literal("branch")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(CommandGit::branchCreate)))
                .then(Commands.literal("checkout")
                        .then(Commands.argument("branch", new BranchArgumentType())
                            .executes(CommandGit::branchCheckout)))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            for (Branch branch : McGit.getGitCore().getBranchHandler().getFoundBranches()) {
                                ctx.getSource().getSender().sendMessage(branch.getName());
                            }
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> conflictSubCommand() {
        return Commands.literal("conflict")
                .then(Commands.literal("resolve")
                        .executes(ctx -> {
                            Merge merge = McGit.getGitCore().getState().getActiveMerge();

                            if (!merge.resolved()) {
                                if (!merge.resolved(merge.peekConflict().location())) {
                                    Broadcast.message("Current conflict is not resolved.");
                                    return 1;
                                }
                                merge.popConflict();
                            }

                            if (merge.resolved()) {
                                try {
                                    merge.commit();
                                } catch (IOException e) {
                                    Broadcast.message(e);
                                }
                                McGit.getGitCore().getState().setIdle();
                                return 1;
                            }

                            if (ctx.getSource().getSender() instanceof Player player) {
                                player.teleport(merge.peekConflict().location());
                                merge.showCurrentConflict();
                            }
                            return 1;
                        }))
                .then(Commands.literal("tp")
                        .executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                Merge merge = McGit.getGitCore().getState().getActiveMerge();
                                player.teleport(merge.peekConflict().location());
                                merge.showCurrentConflict();
                            }
                            return 1;
                        }))
                .then(Commands.literal("abort"));
    }

    private static int branchCreate(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;

        String name = StringArgumentType.getString(ctx, "name");
        BranchHandler branches = McGit.getGitCore().getBranchHandler();

        if (branches.getBranchByName(name).isPresent()) {
            ctx.getSource().getSender().sendMessage("A branch with this name already exists!");
            return 1;
        }

        branches.split(name);
        return 1;
    }

    private static int branchCheckout(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;
        if (!requireCleanDiff()) return 1;

        Branch branch = BranchArgumentType.getBranch(ctx, "branch");
        GitCore core = McGit.getGitCore();
        BranchHandler branches = core.getBranchHandler();
        try {
            core.getBranchOperations().checkout(branches, branch);
        } catch (McGitException e) {
            e.broadcast();
        }
        return 1;
    }

    private static int merge(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;
        if (!requireCleanDiff()) return 1;

        GitCore core = McGit.getGitCore();
        Branch target = BranchArgumentType.getBranch(ctx, "branch");
        try {
            Merge merge = core.getBranchOperations().merge(
                    core.getBranchHandler(),
                    target,
                    ctx.getSource().getSender().getName()
            );

            if (merge.resolved()) {
                merge.commit();
                return 1;
            }

            Broadcast.message("There are merge conflicts! Use /git conflict tp and /git conflict resolve");

            McGit.getGitCore().getState().setActiveMerge(merge);
        } catch (McGitException e) {
            e.broadcast();
        } catch (IOException e) {
            Broadcast.message("Failed to save merge commit.", e);
        }
        return 1;
    }

    private static int rebase(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;
        if (!requireCleanDiff()) return 1;

        GitCore core = McGit.getGitCore();
        Branch target = BranchArgumentType.getBranch(ctx, "branch");
        try {
            CommitHash rebaseHash = core.getBranchOperations().rebase(
                    core.getBranchHandler(),
                    target,
                    ctx.getSource().getSender().getName()
            );
            Broadcast.message("Rebase complete.", "Commit: " + rebaseHash);
        } catch (McGitException e) {
            e.broadcast();
        } catch (IOException e) {
            Broadcast.message("Failed to save rebase commit.", e);
        }
        return 1;
    }

    private static boolean requireCleanDiff() {
        if (McGit.getGitCore().getCurrentDiff().getBlockModifications().isEmpty()) return true;

        Broadcast.message("Commit or discard active changes before changing branch history.");
        return false;
    }

    private static int commit(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;

        String message = StringArgumentType.getString(ctx, "message");
        GitCore core = McGit.getGitCore();
        try {
            core.getCurrentDiff().commit(message, ctx.getSource().getSender().getName());
        } catch (IOException e) {
            ctx.getSource().getSender().sendMessage("Failed to save commit.");
        }
        return 1;
    }

    private static int log(CommandContext<CommandSourceStack> ctx) {
        CommitHash head = McGit.getGitCore().getBranchHandler().getBranch().getHeadHash();
        Broadcast.message("Commit log for " + McGit.getGitCore().getBranchHandler().getBranch().getName());
        commitLog(head, "", new HashSet<>());
        return 1;
    }

    private static void commitLog(CommitHash hash, String prefix, Set<CommitHash> parentTrail) {
        ArrayList<Commit> history = new ArrayList<>();
        try {
            Commit commit;
            do {
                if (parentTrail.contains(hash)) break;
                commit = Commit.fromHash(hash);
                history.add(commit);

                CommitHash[] parents = commit.getParents();
                if (parents.length >= 1) {
                    hash = parents[0];
                } else break;
            } while (commit.getParents().length > 0);
        } catch (CommitNotFoundException e) {
            e.broadcast();
        }

        for (int i = 0; i < history.size(); i++) {
            String branch = i == history.size() - 1 ? "└ " : "├ ";
            String branch2 = i == history.size() - 1 ? "     " : "│   ";
            Commit commit2 = history.get(i);
            Broadcast.message(prefix + branch + commit2.getMessage());
            Broadcast.message(prefix + branch2 + commit2.getHash());
            if (commit2.getParents().length > 1) {
                for (int j = 1; j < commit2.getParents().length; j++) {
                    Set<CommitHash> trail = history.stream().map(Commit::getHash).collect(Collectors.toSet());
                    trail.addAll(parentTrail);
                    commitLog(commit2.getParents()[j], prefix + branch2, trail);
                }
            }
        }
    }

    private static int rollback(CommandContext<CommandSourceStack> ctx) {
        if (McGit.getGitCore().getState().isBusyAndNotify()) return 1;

        Commit commit = CommitArgumentType.getCommit(ctx, "commit");
        Branch branch = McGit.getGitCore().getBranchHandler().getBranch();

        Broadcast.message("Rolling " + branch.getName() + " back to " + commit);

        ArrayList<CommitHash> commits = null;
        try {
            commits = branch.getTrail(commit.getHash());
        } catch (McGitException e) {
            e.broadcast();
            return 1;
        }

        for (CommitHash commitHash : commits) {
            try {
                Commit c = Commit.fromHash(commitHash);
                Broadcast.message(" | -" + c);
                c.revert();
            } catch (CommitNotFoundException e) {
                e.broadcast();
                return 1;
            }
        }

        branch.setHead(commit.getHash());
        return 1;
    }
}
