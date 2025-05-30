package ru.metaone.libreffa.lobby;

import ru.metaone.libreffa.LibreFFA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static ru.metaone.libreffa.LibreFFA.formatColors;


public class SpawnCommands implements CommandExecutor {

    private final SpawnManager spawnManager;
    private final LibreFFA main;

    public SpawnCommands(SpawnManager spawnManager, LibreFFA main) {
        this.spawnManager = spawnManager;
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (label.equalsIgnoreCase("setspawn") || label.equalsIgnoreCase("ffa:setspawn")){
            if (!player.isOp()) {
                player.sendMessage(formatColors("&cYou don't have permission to use this command."));
                return true;
            }
            spawnManager.setSpawn(player);
            return true;
        } else if (label.equalsIgnoreCase("spawn") || label.equalsIgnoreCase("ffa:spawn")) {
            SpawnManager.teleportToSpawn(player);
            String message = LibreFFA.getInstance().getConfig().getString("messages.teleported-to-spawn", "&7Teleported you to the spawn!");
            player.sendMessage(formatColors(message));
            return true;
        }
        return false;
    }
}