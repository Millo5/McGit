package me.millo.mcGit.utility.messenger;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.millo.mcGit.utility.TextColors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class Messenger {

    private final CommandSender[] targets;

    private Messenger(CommandSender[] sender) {
        targets = sender;
    }

    public static Messenger create(CommandContext<CommandSourceStack> ctx) {
        return new Messenger(new CommandSender[]{ctx.getSource().getSender()});
    }

    public static Messenger createAll() {
        return new Messenger(Bukkit.getServer().getOnlinePlayers().stream()
                .map(player -> (CommandSender) player)
                .toArray(CommandSender[]::new)
        );
    }

    public static Messenger createOps() {
        return new Messenger(Bukkit.getOperators().stream()
                .filter(OfflinePlayer::isOnline)
                .map(offlinePlayer -> (CommandSender) offlinePlayer.getPlayer())
                .toArray(CommandSender[]::new)
        );
    }

    public void send(Message message) {
        send(message.build(prefix()));
    }

    public void send(Message message, Object... arguments) {
        send(message.build(prefix(), arguments));
    }

    public void sendInfo(String message) {
        send(Message.info(message));
    }

    public void sendError(String message) {
        send(Message.error(message));
    }

    public void send(Component message) {
        System.out.println(message.toString());

        for (CommandSender target : targets) {
            target.sendMessage(message);
        }
    }

    private Component prefix() {
        return Component.text("[GIT] ").color(TextColors.PRIMARY);
    }
}
