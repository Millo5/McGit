package me.millo.mcGit.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.millo.mcGit.McGit;
import me.millo.mcGit.git.branch.Branch;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BranchArgumentType implements CustomArgumentType<Branch, String> {

    @Override
    public @NotNull Branch parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String name = getNativeType().parse(reader);
        try {
            return McGit.getGitCore().getBranchHandler().getBranchByName(name).orElseThrow();
        } catch (Exception e) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Invalid Branch")),
                    new LiteralMessage(name));
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Branch branch : McGit.getGitCore().getBranchHandler().getFoundBranches()) {
            if (remaining.isEmpty() || branch.getName().startsWith(remaining)) {
                builder.suggest(branch.getName());
            }
        }
        return builder.buildFuture();
    }

    public static Branch getBranch(CommandContext<?> context, String name) {
        return context.getArgument(name, Branch.class);
    }
}
