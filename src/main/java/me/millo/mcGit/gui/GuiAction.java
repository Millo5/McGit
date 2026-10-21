package me.millo.mcGit.gui;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface GuiAction {
    void run(Player player);
}
