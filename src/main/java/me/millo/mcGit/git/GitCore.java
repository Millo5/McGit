package me.millo.mcGit.git;

import org.bukkit.plugin.java.JavaPlugin;

public class GitCore {

    private final JavaPlugin plugin;

    private WorldDiff currentDiff;

    public GitCore(JavaPlugin plugin) {
        this.plugin = plugin;

        currentDiff = new WorldDiff();
    }

    public WorldDiff getCurrentDiff() {
        return currentDiff;
    }
}
