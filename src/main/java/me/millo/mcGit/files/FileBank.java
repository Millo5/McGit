package me.millo.mcGit.files;

import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Path;

public class FileBank {

    private final static String ROOT = ".mcgit";
    private final static String COMMITS = "commits";
    private final static String BRANCHES = "branches.json";
    private final static String DIFF = "diff.json";

    public static Path getGitFolder() {
        return Bukkit.getPluginsFolder().toPath().resolve(ROOT);
    }

    public static File getBranchesFile() {
        return getGitFolder().resolve(BRANCHES).toFile();
    }

    public static Path getCommitFolder() {
        return getGitFolder().resolve(COMMITS);
    }

    public static File getDiffFile() {
        return getGitFolder().resolve(DIFF).toFile();
    }

}
