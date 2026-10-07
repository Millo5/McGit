package me.millo.mcGit.git.diff;

import org.bukkit.Location;

public record BlockChange(Location location, BlockModification modification) {}
