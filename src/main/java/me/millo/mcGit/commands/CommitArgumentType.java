package me.millo.mcGit.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.millo.mcGit.git.commit.Commit;
import me.millo.mcGit.git.commit.CommitHash;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CommitArgumentType implements ArgumentType<Commit> {

    @Override
    public Commit parse(StringReader stringReader) throws CommandSyntaxException {
        String hash = stringReader.getString();
        try {
            return Commit.fromHash(new CommitHash(UUID.fromString(hash)));
        } catch (IOException e) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Invalid Commit Hash")),
                    new LiteralMessage(hash));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for (String hash : Commit.getFoundHashes()) {
            builder.suggest(hash);
        }
        return builder.buildFuture();
    }

    public static Commit getCommit(CommandContext<?> context, String name) {
        return context.getArgument(name, Commit.class);
    }
}
