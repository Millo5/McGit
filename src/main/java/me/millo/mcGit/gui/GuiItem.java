package me.millo.mcGit.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiItem {

    private final ItemStack itemStack;
    private final GuiAction action;

    public GuiItem(Material material, Component name, List<Component> lore, GuiAction action) {
        this.itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        meta.lore(lore
                .stream()
                .map(line ->
                        line.decoration(TextDecoration.ITALIC, false))
                .toList());
        itemStack.setItemMeta(meta);
        this.action = action;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public GuiAction getAction() {
        return action;
    }
}
