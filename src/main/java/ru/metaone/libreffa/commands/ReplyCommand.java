package ru.metaone.libreffa.commands;

import ru.metaone.libreffa.LibreFFA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static ru.metaone.libreffa.LibreFFA.formatColors;

public class ReplyCommand implements CommandExecutor {

    private final MessageCommand messageCommand;

    public ReplyCommand(MessageCommand messageCommand) {
        this.messageCommand = messageCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!messageCommand.lastMessaged.containsKey(player)) {
            String noRecentMessage = LibreFFA.getInstance().getConfig().getString("messages.no-resent-message" , "&cYou have not messaged anyone recently.");

            player.sendMessage(formatColors(noRecentMessage));
            return true;
        }

        Player recipient = messageCommand.lastMessaged.get(player);

        if (args.length < 1) {
            String usageReply = LibreFFA.getInstance().getConfig().getString("usage.reply", "&c/reply <message>");
            player.sendMessage(formatColors(usageReply));
            return true;
        }

        String message = String.join(" ", args);

        messageCommand.sendMessage(player, recipient, message);
        return true;
    }
}