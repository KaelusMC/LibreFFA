package ru.metaone.libreffa.deathmessages;

import ru.metaone.libreffa.LibreFFA;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.metaone.libreffa.LibreFFA.formatColors;

public class DeathMessagesManager implements Listener {
    private List<String> deathMessages;
    private List<String> disabledWorlds;
    private boolean disabled;
    private FileConfiguration config;
    private LibreFFA main;

    public DeathMessagesManager(LibreFFA main) {
        this.main = main;
        deathMessages = new ArrayList<>();
        disabledWorlds = new ArrayList<>();
        disabled = false;
        loadConfig(main);
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    private void loadConfig(JavaPlugin plugin) {
        config = plugin.getConfig();
        disabled = config.getBoolean("deathMessages.disabled");
        if (config.isList("deathMessages.messages")) {
            deathMessages = config.getStringList("deathMessages.messages");
        } else {
            deathMessages.add("&c%victim% &7was defeated by &a%attacker% &8(%attacker_health% ❤)");
            deathMessages.add("&7You can add more here!");
            config.set("deathMessages.messages", deathMessages);
        }

        if (config.isList("deathMessages.disabledWorlds")) {
            disabledWorlds = config.getStringList("deathMessages.disabledWorlds");
        } else {
            disabledWorlds.add("example_world");
            config.set("deathMessages.disabledWorlds", disabledWorlds);
        }

        plugin.saveConfig();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        String worldName = victim.getWorld().getName();

        if (disabled) {
            return;
        }

        if (disabledWorlds.contains(worldName)) {
            return;
        }

        Player attacker = event.getEntity().getKiller();
        String attackerName = (attacker != null) ? attacker.getName() : "Unknown";
        String victimName = (victim != null) ? victim.getName() : "Unknown";
        double attackerHealth = (attacker != null) ? attacker.getHealth() : 0.00;
        double victimHealth = (victim != null) ? victim.getHealth() : 0.00;
        Bukkit.getScheduler().runTaskAsynchronously(LibreFFA.getInstance(), () -> {
            Bukkit.broadcastMessage(formatColors(Placeholders(attackerName, victimName, attackerHealth, victimHealth)));
        });
    }

    private String Placeholders(String attackerName, String victimName, double attackerHealth, double victimHealth) {
        String deathMessage = getRandomDeathMessage();
        deathMessage = deathMessage.replace("%attacker%", attackerName);
        deathMessage = deathMessage.replace("%victim%", victimName);
        deathMessage = deathMessage.replace("%attacker_health%", String.format("%.2f", attackerHealth));
        deathMessage = deathMessage.replace("%victim_health%", String.format("%.2f", victimHealth));
        return deathMessage;
    }

    private String getRandomDeathMessage() {
        Random random = new Random();
        int index = random.nextInt(deathMessages.size());
        return deathMessages.get(index);
    }
}
