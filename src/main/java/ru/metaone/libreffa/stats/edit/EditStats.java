package ru.metaone.libreffa.stats.edit;

import ru.metaone.libreffa.stats.StatsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static ru.metaone.libreffa.LibreFFA.formatColors;

public class EditStats {

    public static void editKills(Player player, int newKills) {
        StatsManager.editKills(player.getUniqueId(), newKills);
    }

    public static void editDeaths(Player player, int newDeaths) {
        StatsManager.editDeaths(player.getUniqueId(), newDeaths);
    }

    public static void editStreak(Player player, int newStreak) {
        StatsManager.editStreak(player.getUniqueId(), newStreak);
    }

    public static void editHighestStreak(Player player, int newHighestStreak) {
        StatsManager.editHighestStreak(player.getUniqueId(), newHighestStreak);
    }

    public static void sendInvalidCommandMessage(@NotNull CommandSender sender) {
        sender.sendMessage(formatColors("\n"));
        sender.sendMessage(formatColors("&b&lFFA &8| &7Invalid Command"));
        sender.sendMessage(formatColors("\n"));
        sender.sendMessage(formatColors("&b• &7/ffa editstats <player> <statstype> <value>"));
        sender.sendMessage(formatColors("\n"));
        sender.sendMessage(formatColors("&b&lExample"));
        sender.sendMessage(formatColors("&b• &7/ffa editstats Darkxx kills 10"));
        sender.sendMessage(formatColors("\n"));
    }
}


