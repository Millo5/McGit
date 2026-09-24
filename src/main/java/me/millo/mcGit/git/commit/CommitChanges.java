package me.millo.mcGit.git.commit;

import me.millo.mcGit.git.diff.BlockModification;
import org.bukkit.Location;

public record CommitChanges(Location[] locations, BlockModification[] modifications) {

}
