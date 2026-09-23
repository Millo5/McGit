package me.millo.mcGit.commands;

import io.papermc.paper.command.brigadier.Commands;
import me.millo.mcGit.McGit;

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
                        .build()
        );

    }

}
