package me.millo.mcGit.utility.messenger;

public class Messages {

    public static Message CONFLICT_NOT_RESOLVED = Message.error("Current conflict is not resolved.");
    public static Message COMMIT_ALREADY_EXISTS = Message.error("Commit {0} already exists!");

    public static Message DIFF_SAVED = Message.info("Diff saved");
    public static Message DIFF_SAVED_FAILED = Message.error("Failed to save diff {0}");

}
