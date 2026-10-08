package me.millo.mcGit.utility.messenger;

public class Messages {

    public static final Message BASIC_COMMAND = Message.info("Basic git command");
    public static final Message BRANCH_ALREADY_EXISTS = Message.error("A branch with this name already exists!");
    public static final Message BRANCH_CHANGED = Message.info("Now on branch: {0}");
    public static final Message BRANCH_HEAD = Message.info("   Head: {0}");
    public static final Message BRANCH_SAVE_FAILED = Message.error("Failed to save branches {0}");
    public static final Message BRANCH_RETURN_HEAD = Message.info("Returning to head branch.");

    public static final Message COMMIT_ALREADY_EXISTS = Message.error("Commit {0} already exists!");
    public static final Message COMMIT_DOES_NOT_EXIST = Message.error("Commit {0} does not exist!");
    public static final Message COMMIT_LOAD_FAILED = Message.error("Failed to load commit {0}");
    public static final Message COMMIT_SAVE_FAILED = Message.error("Failed to save commit.");
    public static final Message NO_CHANGES_TO_COMMIT = Message.info("There are no changes to commit.");

    public static final Message CONFLICT_NOT_RESOLVED = Message.error("Current conflict is not resolved.");
    public static final Message MERGE_CONFLICTS = Message.warn("There are merge conflicts! Use /git conflict tp and /git conflict resolve");
    public static final Message MERGE_COMPLETE = Message.info("Merge complete.");
    public static final Message MERGE_SAVE_FAILED = Message.error("Failed to save merge commit. {0}");

    public static final Message REBASE_COMPLETE = Message.info("Rebase complete.");
    public static final Message REBASE_SAVE_FAILED = Message.error("Failed to save rebase commit. {0}");

    public static final Message REQUIRE_CLEAN_DIFF = Message.warn("Commit or discard active changes before changing branch history.");
    public static final Message GIT_STATE_BUSY = Message.warn("Git State is required to be idle. Current: {0}");

    public static final Message DIFF_SAVED = Message.info("Diff saved");
    public static final Message DIFF_SAVED_FAILED = Message.error("Failed to save diff {0}");
    public static final Message DIFF_NO_CHANGES_TO_SAVE = Message.info("No diff changes to save.");
    public static final Message DIFF_LOAD_FAILED = Message.error("Failed to load diff {0}");
    public static final Message DIFF_HIDDEN = Message.info("Hiding diff.");
    public static final Message DIFF_VIEWING = Message.info("Viewing diff...");

}
