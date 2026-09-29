package me.millo.mcGit.exceptions;

import me.millo.mcGit.git.commit.CommitHash;

public class CommitNotFoundException extends McGitException {
    public CommitNotFoundException(CommitHash commit) {
        super("Commit " + commit.toString() + " not found.");
    }
}
