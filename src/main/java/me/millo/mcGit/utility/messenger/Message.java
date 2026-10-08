package me.millo.mcGit.utility.messenger;

import me.millo.mcGit.utility.TextColors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class Message {

    private final String message;
    private final Component prefix;
    private final TextColor color;

    private static final Component errorPrefix = Component.text("X ").color(TextColors.RED);
    private static final Component warnPrefix = Component.text("!! ").color(TextColors.SECONDARY);
    private static final Component infoPrefix = Component.text("(i) ").color(TextColors.RED);

    public Message(String message, Component prefix, TextColor color) {
        this.message = message;
        this.prefix = prefix;
        this.color = color;
    }

    public static Message error(String message) {
        return new Message(message, errorPrefix, TextColors.DARK);
    }

    public static Message info(String message) {
        return new Message(message, infoPrefix, TextColors.LIGHT);
    }

    public Component build(Component base) {
        return base.append(prefix, Component.text(message).color(color));
    }

    public Component build(Component base, Object[] arguments) {
        return base.append(prefix, Component.text(message.formatted(arguments)).color(color));
    }
}
