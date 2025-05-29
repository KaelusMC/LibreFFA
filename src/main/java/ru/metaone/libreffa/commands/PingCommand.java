package ru.metaone.libreffa.commands;

import ru.metaone.libreffa.LibreFFA;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static ru.metaone.libreffa.LibreFFA.formatColors;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            sendPingInfo(player, player);
            return true;
        }
        if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
                return true;
            }
            sendPingInfo(player, targetPlayer);
            return true;
        }
        String usagePing = LibreFFA.getInstance().getConfig().getString("usage.ping", "&cUsage, /ping [playerName]");
        player.sendMessage(formatColors(usagePing));
        return true;
    }

    private void sendPingInfo(Player sender, Player target) {
        int ping = getPing(target);

        List<String> messages = LibreFFA.getInstance().getConfig().getStringList("messages.ping.format");
        String otherPlayer = target.getName();

        for (String message : messages) {
            message = message.replace("%player%", sender.equals(target) ? "Your" : otherPlayer + "'s");
            message = message.replace("%ping%", Integer.toString(ping));

            sender.sendMessage(formatColors(message));
        }
    }

    public static int getPing(Player player) {
        return player.getPing();
    }
}