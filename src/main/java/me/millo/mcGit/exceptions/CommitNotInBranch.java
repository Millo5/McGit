package me.millo.mcGit.exceptions;

import me.millo.mcGit.git.branch.Branch;
import me.millo.mcGit.git.commit.CommitHash;

public class CommitNotInBranch extends McGitException {

    public CommitNotInBranch(CommitHash hash, Branch branch) {
        super("Commit " + hash + " not found in branch " + branch.getName());
    }

}
