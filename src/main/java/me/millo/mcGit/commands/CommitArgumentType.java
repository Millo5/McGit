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
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommitArgumentType implements CustomArgumentType<Commit, String> {

    private boolean inBranch;

    public CommitArgumentType() {
        this(false);
    }

    public CommitArgumentType(boolean inBranch) {
        this.inBranch = inBranch;
    }

    @Override
    public @NotNull Commit parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String hash = getNativeType().parse(reader);
        try {
            return Commit.fromHash(new CommitHash(UUID.fromString(hash)));
        } catch (IOException e) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Invalid Commit Hash")),
                    new LiteralMessage(hash));
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();

        List<String> hashes = inBranch
                ? McGit.getGitCore().getBranchHandler().getBranch().getTrailAsStrings()
                : Commit.getFoundHashes();

        for (String hash : hashes) {
            if (remaining.isEmpty() || hash.startsWith(remaining)) {
                builder.suggest(hash);
            }
        }
        return builder.buildFuture();
    }

    public static Commit getCommit(CommandContext<?> context, String name) {
        return context.getArgument(name, Commit.class);
    }
}
