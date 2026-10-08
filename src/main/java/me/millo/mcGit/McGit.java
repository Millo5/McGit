package me.millo.mcGit;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.millo.mcGit.commands.CommandGit;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.git.commit.serializer.ChangesSerializer;
import me.millo.mcGit.git.commit.serializer.ImprovedChangesSerializer;
import me.millo.mcGit.gui.GitMenuListener;
import me.millo.mcGit.listeners.BlockChangeListener;
import me.millo.mcGit.listeners.InteractionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class McGit extends JavaPlugin {
    public static final ChangesSerializer SERIALIZER = new ImprovedChangesSerializer();
    private static GitCore gitCore;

    @Override
    public void onEnable() {
        System.out.println("McGit has been enabled.");

        gitCore = new GitCore();

        getServer().getPluginManager().registerEvents(new BlockChangeListener(gitCore), this);
        getServer().getPluginManager().registerEvents(new InteractionListener(), this);
        getServer().getPluginManager().registerEvents(new GitMenuListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            CommandGit.register(event.registrar());
        });

        getServer().getScheduler().scheduleSyncRepeatingTask(this, gitCore.getCurrentDiff()::save, 0, 20 * 60);

    }

    @Override
    public void onDisable() {
        System.out.println("McGit has been disabled.");

        gitCore.getCurrentDiff().save();
    }

    public static GitCore getGitCore() {
        return gitCore;
    }
}
