package me.millo.mcGit.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.millo.mcGit.McGit;
import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.branch.BranchHandler;
import me.millo.mcGit.utility.Broadcast;

import java.util.Optional;

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
                        .build()
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> branchSubCommand() {
        return Commands.literal("branch")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(CommandGit::branchCreate)))
                .then(Commands.literal("checkout")
                        .then(Commands.argument("name", StringArgumentType.word())
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
        String name = StringArgumentType.getString(ctx, "name");
        BranchHandler branches = McGit.getGitCore().getBranchHandler();

        Optional<Branch> found = branches.getBranchByName(name);
        if (found.isEmpty()) {
            ctx.getSource().getSender().sendMessage("A branch with this name does not exist!");
            return 1;
        }

        branches.setBranch(found.get());
        return 1;
    }
}
