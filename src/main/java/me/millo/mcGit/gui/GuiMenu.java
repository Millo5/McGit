package me.millo.mcGit.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, GuiAction> actions = new HashMap<>();

    protected GuiMenu(Component title, int size) {
        inventory = Bukkit.createInventory(this, size, title);
    }

    protected void setItem(int slot, GuiItem item) {
        inventory.setItem(slot, item.getItemStack());
        actions.put(slot, item.getAction());
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void click(int slot, Player player) {
        GuiAction action = actions.get(slot);
        if (action != null) action.run(player);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
