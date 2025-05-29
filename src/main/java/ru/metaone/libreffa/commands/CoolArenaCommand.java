package ru.metaone.libreffa.commands;

import ru.metaone.libreffa.LibreFFA;
import ru.metaone.libreffa.arenas.coolarenas.CoolArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static ru.metaone.libreffa.LibreFFA.formatColors;

public class CoolArenaCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ffa.coolarena.use")) {
            String noPermission = LibreFFA.getInstance().getConfig().getString("messages.no-permission", "&cNo Permission.");
            sender.sendMessage(formatColors(noPermission));
            return true;
        }

        if (args.length != 1) {
            String usageCoolArena = LibreFFA.getInstance().getConfig().getString("usage.coolarena", "&cUsage, /coolarena <arenaName>");
            player.sendMessage(formatColors(usageCoolArena));
            return true;
        }

        String arenaName = args[0];

        CoolArenaManager.warp(sender, player.getName(), arenaName);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(CoolArenaManager.listCa());
        }

        return completions;
    }
}
