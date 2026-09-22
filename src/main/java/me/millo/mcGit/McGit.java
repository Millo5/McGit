package me.millo.mcGit;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.millo.mcGit.commands.CommandGit;
import me.millo.mcGit.git.GitCore;
import me.millo.mcGit.listeners.BlockChangeListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class McGit extends JavaPlugin {

    private static GitCore gitCore;

    @Override
    public void onEnable() {
        System.out.println("McGit has been enabled.");

        gitCore = new GitCore(this);

        getServer().getPluginManager().registerEvents(new BlockChangeListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            CommandGit.register(event.registrar());
        });

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static GitCore getGitCore() {
        return gitCore;
    }
}
