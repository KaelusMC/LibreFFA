package ru.metaone.libreffa;

import ru.metaone.libreffa.bstats.Metrics;
import ru.metaone.libreffa.combat.CombatTagger;
import ru.metaone.libreffa.commands.SettingsCommand;
import ru.metaone.libreffa.commands.MessageCommand;
import ru.metaone.libreffa.commands.PingCommand;
import ru.metaone.libreffa.commands.RulesCommand;
import ru.metaone.libreffa.commands.SuicideCommand;
import ru.metaone.libreffa.commands.SitCommand;
import ru.metaone.libreffa.commands.HealCommand;
import ru.metaone.libreffa.commands.NickCommand;
import ru.metaone.libreffa.commands.BroadcastCommand;
import ru.metaone.libreffa.commands.StatsCommand;
import ru.metaone.libreffa.commands.ReplyCommand;
import ru.metaone.libreffa.commands.CoolArenaCommand;
import ru.metaone.libreffa.commands.settings.*;
import ru.metaone.libreffa.deathmessages.DeathMessagesManager;
import ru.metaone.libreffa.commands.FlyCommand;
import ru.metaone.libreffa.kits.KitManager;
import ru.metaone.libreffa.lobby.VoidListener;
import ru.metaone.libreffa.lobby.SpawnCommands;
import ru.metaone.libreffa.lobby.SpawnManager;
import ru.metaone.libreffa.regeneration.RegenerationImpl;
import ru.metaone.libreffa.regeneration.command.RegenerationCommand;
import ru.metaone.libreffa.settings.OldDamageTilt;
import ru.metaone.libreffa.spawnitems.Items;
import ru.metaone.libreffa.stats.Stats;
import ru.metaone.libreffa.expansion.Placeholders;
import ru.metaone.libreffa.stats.StatsManager;
import ru.metaone.libreffa.tasks.ClipboardCleaner;
import ru.metaone.libreffa.utils.MiscListener;
import ru.metaone.libreffa.utils.gui.GuiManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LibreFFA extends JavaPlugin {

    private static LibreFFA instance;
    private DatabaseManager dbm;
    public static String prefix;
    private FileConfiguration config;
    private Stats stats;
    private SpawnManager spawnManager;
    private DeathMessagesManager deathMessagesManager;
    private MessageCommand messageCommand;
    private static File kitsFolder;
    private CombatTagger combatTagger;

    @Override
    public void onEnable() {
        instance = this;
        PlaceholderAPI();
        GuiManager.register(this);
        saveDefaultConfig();
        config = getConfig();
        prefix = config.getString("prefix", "&b&lFFA &7|&r");
        DatabaseManager.connect();
        kitsFolder = KitManager.createKitsFolder();
        Register();
        Commands();

        try {
            File configFile = new File(LibreFFA.getInstance().getDataFolder(), "menus/settings_menu.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                LibreFFA.getInstance().saveResource("menus/settings_menu.yml", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7Saving data..."));

        try {
            StatsManager.saveAll();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&cFailed to save stats " + e.getMessage()));
            e.printStackTrace();
        }

        try {
            DatabaseManager.disconnect();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&cFailed to disconnect from database " + e.getMessage()));
            e.printStackTrace();
        }

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        try {
            RegenerationImpl.saveAll();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&cFailed to save regeneration data " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void PlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        } else {
            Bukkit.getConsoleSender().sendMessage(formatColors(" "));
            Bukkit.getConsoleSender().sendMessage(formatColors("&b&lFFA"));
            Bukkit.getConsoleSender().sendMessage(formatColors(" "));
            Bukkit.getConsoleSender().sendMessage(formatColors("&#FF0000PlaceholderAPI not found."));
            Bukkit.getConsoleSender().sendMessage(formatColors("&#FF0000This plugin has a dependency on PlaceholderAPI."));
            Bukkit.getConsoleSender().sendMessage(formatColors("&#FF0000Please install PlaceholderAPI to use this plugin."));
            Bukkit.getConsoleSender().sendMessage(formatColors(" "));
            Bukkit.getConsoleSender().sendMessage(formatColors("&7Made with &c❤️ &7 by the Xyris Team!"));
            Bukkit.getConsoleSender().sendMessage(formatColors(" "));

            Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.getPluginManager().disablePlugin(this);
            }, 20L);
        }
    }

    private void Register() {
        // SpawnItems
        new Items(this);
        // Database
        dbm = new DatabaseManager();
        // FFA Commands
        getCommand("ffa").setExecutor(new Commands(this));
        getCommand("ffa").setTabCompleter(new Commands(this));
        // Stats
        stats = new Stats(config);
        getServer().getPluginManager().registerEvents(stats, this);
        // MiscListener - Utils
        MiscListener misc = new MiscListener(this);
        getServer().getPluginManager().registerEvents(misc, this);
        // Spawn
        spawnManager = new SpawnManager();
        getServer().getPluginManager().registerEvents(spawnManager, this);
        getCommand("setspawn").setExecutor(new SpawnCommands(spawnManager, this));
        getCommand("spawn").setExecutor(new SpawnCommands(spawnManager, this));
        getServer().getPluginManager().registerEvents(new VoidListener(this), this);

        // Death Messages
        deathMessagesManager = new DeathMessagesManager(this);

        // Combat Tagger
        int combatTimer = getConfig().getInt("combat-tagger.combat-timer");
        combatTagger = new CombatTagger(this, combatTimer);
        getServer().getPluginManager().registerEvents(combatTagger, this);

        // BStats
        Metrics metrics = new Metrics(this, 26027);

        // ProtocolLib
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            new OldDamageTilt(this);
        } else {
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7ProtocolLib not found. Some features may be disabled. [OldDamageTilt setting], [none]"));
        }
        // Regeneration
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            getCommand("regeneration").setExecutor(new RegenerationCommand());
            getCommand("regeneration").setTabCompleter(new RegenerationCommand());
            RegenerationImpl.loadAll();
        } else {
            Bukkit.getConsoleSender().sendMessage(formatColors(prefix + "&7FastAsyncWorldEdit not found, Regeneration will not work."));
        }
        // Updater
        //UpdateTask.run();

        // Clipboard Cleaner
        long delay = 0L;
        long period = 10800L * 20L;
        new ClipboardCleaner().runTaskTimerAsynchronously(this, delay, period);
    }

    private void Commands() {
        getCommand("coolarena").setExecutor(new CoolArenaCommand());
        getCommand("coolarena").setTabCompleter(new CoolArenaCommand());
        getCommand("suicide").setExecutor(new SuicideCommand());
        getCommand("broadcast").setExecutor(new BroadcastCommand(getConfig()));
        getCommand("rules").setExecutor(new RulesCommand(getConfig()));
        getCommand("sit").setExecutor(new SitCommand());
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("heal").setExecutor(new HealCommand());
        messageCommand = new MessageCommand(config);
        getCommand("message").setExecutor(messageCommand);
        getCommand("reply").setExecutor(new ReplyCommand(messageCommand));
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("nickname").setExecutor(new NickCommand(this));
        getCommand("settings").setExecutor(new SettingsCommand());

        getCommand("toggleautogg").setExecutor(new ToggleAutoGGCommand());
        getCommand("toggledirectionaldamagetilt").setExecutor(new ToggleDirectionalDamageTiltCommand());
        getCommand("togglementionsound").setExecutor(new ToggleMentionSoundCommand());
        getCommand("toggleprivatemessages").setExecutor(new TogglePrivateMessagesCommand());
        getCommand("togglequickrespawn").setExecutor(new ToggleQuickRespawnCommand());
    }

    public static LibreFFA getInstance() {
        return instance;
    }

    public static File getKitsFolder() {
        return kitsFolder;
    }

    /**
     * This method formats color codes in a given message string.
     * Color codes are represented using '&' followed by hexadecimal color codes or RGB values.
     *
     * @param message the input message containing color codes
     * @return the formatted message with color codes replaced by their respective colors
     */
    public static String formatColors(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String colorCode = matcher.group();
            ChatColor color = ChatColor.of(colorCode.substring(1));
            message = message.replace(colorCode, color.toString());
        }
        return message;
    }
}