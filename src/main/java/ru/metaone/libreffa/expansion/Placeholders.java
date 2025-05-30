package ru.metaone.libreffa.expansion;

import ru.metaone.libreffa.combat.Combat;
import ru.metaone.libreffa.combat.CombatTagger;
import ru.metaone.libreffa.arenas.Arenas;
import ru.metaone.libreffa.commands.NickCommand;
import ru.metaone.libreffa.kits.Kits;
import ru.metaone.libreffa.settings.SettingsManager;
import ru.metaone.libreffa.stats.StatsManager;
import ru.metaone.libreffa.utils.WorldGuardUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "ffa";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Darkxx";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            if (identifier.startsWith("players_")) {
                String regionName = identifier.replace("players_", "");
                int playerCount = WorldGuardUtils.getPlayerCountInRegion(regionName);
                return String.valueOf(playerCount);
            } else if (identifier.startsWith("isbusy_")) {
                String regionName = identifier.replace("isbusy_", "");
                return WorldGuardUtils.checkArenaStatus(regionName);
            }
        } else if (identifier.startsWith("players_") || identifier.startsWith("isbusy_")) {
            return "";
        }

        return switch (identifier.toLowerCase()) {
            case "kills" -> String.valueOf(StatsManager.getCurrentKills(player.getUniqueId()));
            case "deaths" -> String.valueOf(StatsManager.getCurrentDeaths(player.getUniqueId()));
            case "kdr" -> String.valueOf(StatsManager.calculateKDR(player.getUniqueId()));
            case "streak" -> String.valueOf(StatsManager.getCurrentStreak(player.getUniqueId()));
            case "maxstreak" -> String.valueOf(StatsManager.getHighestStreak(player.getUniqueId()));
            case "lastkit" -> Kits.getLastKit(player);
            case "lastarena" -> Arenas.getLastArena(player);
            case "combat_timer" -> {
                List<Combat> combatLogs = CombatTagger.getCombatLogs(player);
                yield String.valueOf(Combat.getPlayerCombatTimer(player, combatLogs));
            }
            case "nickname" -> NickCommand.getNickname(player);
            case "settings_olddamagetilt" -> SettingsManager.getSettingStatus(player, "OldDamageTilt");
            case "settings_privatemessages" -> SettingsManager.getSettingStatus(player, "privateMessages");
            case "settings_autogg" -> SettingsManager.getSettingStatus(player, "autoGG");
            case "settings_mentionsound" -> SettingsManager.getSettingStatus(player, "mentionSound");
            case "settings_quickrespawn" -> SettingsManager.getSettingStatus(player, "toggleQuickRespawn");
            default -> null;
        };
    }
}