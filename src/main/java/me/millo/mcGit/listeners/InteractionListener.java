package me.millo.mcGit.listeners;

import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class InteractionListener implements Listener {

    private static Map<UUID, Consumer<Interaction>> actions = new HashMap<>();

    @EventHandler
    public void onInteraction(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        Consumer<Interaction> runnable = actions.get(interaction.getUniqueId());
        if (runnable != null) {
            runnable.accept(interaction);
        }
    }

    public static void add(UUID uuid, Consumer<Interaction> runnable) {
        actions.put(uuid, runnable);
    }

    public static void remove(UUID uuid) {
        actions.remove(uuid);
    }
}
