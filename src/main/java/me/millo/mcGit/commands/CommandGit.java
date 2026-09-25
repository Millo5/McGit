package me.millo.mcGit.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.millo.mcGit.McGit;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;
import me.millo.mcGit.utility.Broadcast;

import java.io.IOException;

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
                        .then(Commands.literal("apply")
                                .then(Commands.argument("hash", new CommitArgumentType())
                                        .executes(ctx -> {
                                            Commit commit = CommitArgumentType.getCommit(ctx, "hash");
                                            commit.apply();
                                            return 1;
                                        })))
                        .then(Commands.literal("revert")
                                .then(Commands.argument("hash", new CommitArgumentType())
                                        .executes(ctx -> {
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

    private static int branchCreate(CommandContext<CommandSourceStack> ctx) {
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
        Branch branch = BranchArgumentType.getBranch(ctx, "branch");
        BranchHandler branches = McGit.getGitCore().getBranchHandler();
        branches.setBranch(branch);
        return 1;
    }

    private static int commit(CommandContext<CommandSourceStack> ctx) {
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
        commitLog(head, 0);
        return 1;
    }

    private static void commitLog(CommitHash hash, int depth) {
        try {
            Commit commit = Commit.fromHash(hash);
            String depthStr = "  ".repeat(depth);
            Broadcast.message(depthStr + commit.getMessage(), depthStr + hash);

            if (commit.getParents().length > 1) depth++;
            for (CommitHash parent : commit.getParents()) {
                commitLog(parent, depth);
            }
        } catch (IOException e) {
            Broadcast.message(hash.toString(), "COULD NOT FIND COMMIT");
        }
    }

    private static int rollback(CommandContext<CommandSourceStack> ctx) {
        Commit commit = CommitArgumentType.getCommit(ctx, "commit");
        Branch branch = McGit.getGitCore().getBranchHandler().getBranch();

        if (!branch.getTrail().contains(commit.getHash())) {
            Broadcast.message("Commit not in branch history");
            return 0;
        }

        for (CommitHash commitHash : branch.getTrail()) {
            if (commitHash.equals(commit.getHash())) break;
            try {
                Commit c = Commit.fromHash(commitHash);
                c.revert();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        branch.setHead(commit.getHash());

        return 1;
    }
}
