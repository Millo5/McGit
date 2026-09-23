package me.millo.mcGit.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Broadcast {

    public static void message(String header, Object... messages) {
        message(header);
        for (Object message : messages) {
            message("   " + message);
        }
    }

    public static void message(Object message) {
        System.out.println("Broadcast: " + message);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.sendMessage(
                    Component.text("[GIT] ").color(TextColor.color(195, 50, 90))
                            .append(Component.text(message.toString()).color(TextColor.color(80, 80, 80))));
        }
    }
}
