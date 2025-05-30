package ru.metaone.libreffa.utils;

import ru.metaone.libreffa.LibreFFA;
import ru.metaone.libreffa.api.events.QuickRespawnEvent;
import ru.metaone.libreffa.arenas.ArenaManager;
import ru.metaone.libreffa.commands.PingCommand;
import ru.metaone.libreffa.settings.SettingsManager;
import ru.metaone.libreffa.stats.StatsManager;
import ru.metaone.libreffa.tasks.UpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.*;

import static ru.metaone.libreffa.LibreFFA.formatColors;
import static ru.metaone.libreffa.LibreFFA.prefix;
import static ru.metaone.libreffa.arenas.Arenas.getLastArena;
import static ru.metaone.libreffa.kits.Kits.getLastKit;
import static org.bukkit.Bukkit.getLogger;

public class MiscListener implements Listener {

    private final LibreFFA main;
    Scoreboard board;
    Objective objective;
    private boolean healthBarEnabled;
    private Set<String> disabledWorlds;
    private String healthBarDisplayNameFormat;

    public MiscListener(LibreFFA main) {
        this.main = main;
        loadConfigSettings();
        if (healthBarEnabled) {
            this.createHealthBar();
        }
    }

    private void loadConfigSettings() {
        ConfigurationSection config = main.getConfig().getConfigurationSection("healthbar");
        if (config != null) {
            healthBarEnabled = config.getBoolean("enabled", false);

            List<String> disabledWorldNames = config.getStringList("disabled-worlds");
            if (disabledWorldNames != null) {
                disabledWorlds = new HashSet<>(disabledWorldNames);
            }

            healthBarDisplayNameFormat = config.getString("display-name-format", "&c❤");
        }
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.PHYSICAL) return;
        if (!e.getClickedBlock().getType().name().contains("PLATE")) return;
        double horizontalForce = main.getConfig().getDouble("launchpad.force");
        Bukkit.getScheduler().runTask(main, () -> {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(horizontalForce).setY(0.5));
        });
    }

    @EventHandler
    public void QuickRespawn(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() ||
                !meta.getDisplayName().toLowerCase().contains("respawn")) {
            return;
        }

        String clickAction = LibreFFA.getInstance().getConfig().getString("quick-respawn.click-action", "BOTH");
        if (!clickAction.equalsIgnoreCase("BOTH") &&
                ((clickAction.equalsIgnoreCase("LEFT") && !e.getAction().name().contains("LEFT")) ||
                        (clickAction.equalsIgnoreCase("RIGHT") && !e.getAction().name().contains("RIGHT")))) {
            return;
        }

        String materialName = LibreFFA.getInstance().getConfig().getString("quick-respawn.material");
        if (materialName == null) {
            return;
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null || item.getType() != material) {
            return;
        }

        Player player = e.getPlayer();
        String playerName = player.getName();
        String kit;

        kit = getLastKit(player);

        String arena = getLastArena(player);
        QuickRespawnEvent onQuickR = new QuickRespawnEvent(player, item, arena, kit);
        Bukkit.getServer().getPluginManager().callEvent(onQuickR);

        if (onQuickR.isCancelled()) {
            return;
        }

        if (!kit.equals("none") && !arena.equals("none")) {
            String kitCmd = "ffa kits give " + playerName + " " + kit;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), kitCmd);
            ArenaManager.warp(null, player, arena);

            String messageType = LibreFFA.getInstance().getConfig().getString("quick-respawn.message-type", "action bar");
            String message = LibreFFA.getInstance().getConfig().getString("quick-respawn.message", "&aTeleported to your last location.");

            if (messageType.equalsIgnoreCase("chat")) {
                player.sendMessage(formatColors(message));
            } else if (messageType.equalsIgnoreCase("action bar")) {
                ActionBarUtil.sendActionBar(player, message);
            } else {
                getLogger().severe("Unknown message type specified in configuration " + messageType);
            }
        }
    }

    public static ItemStack createQuickRespawnItem() {
        String materialName = LibreFFA.getInstance().getConfig().getString("quick-respawn.material");
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            LibreFFA.getInstance().getLogger().severe("Invalid material specified for quick-respawn item " + materialName);
            return null;
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String itemName = LibreFFA.getInstance().getConfig().getString("quick-respawn.item-name", "§aQuick Respawn");
            List<String> lore = LibreFFA.getInstance().getConfig().getStringList("quick-respawn.lore");

            meta.setDisplayName(formatColors(itemName));

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(formatColors(line));
                }
                meta.setLore(coloredLore);
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static void giveQuickRespawn(Player player, ItemStack itemStack, int slot) {
        Inventory playerInventory = player.getInventory();
        playerInventory.setItem(slot, itemStack);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (e.getHitBlock() != null && e.getHitBlock().getType() != Material.AIR) {
                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (UpdateTask.isOutdated) {
            if (player.isOp()) {
                player.sendMessage(formatColors(" "));
                player.sendMessage(formatColors(prefix));
                player.sendMessage(formatColors("&7Hey, seems like FFA plugin is outdated,"));
                player.sendMessage(formatColors("&7please upgrade the plugin to the latest version v" + UpdateTask.latestVersion + "."));
                player.sendMessage(formatColors(" "));
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(LibreFFA.getInstance(), () -> {
            SettingsManager.ensurePlayerSettings(player);
            if (healthBarEnabled && !isWorldDisabled(player.getWorld())) {
                updateHealthBar(player);
            }
        });
        StatsManager.load(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        StatsManager.save(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if (healthBarEnabled && !isWorldDisabled(player.getWorld())) {
            updateHealthBar(player);
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    private void createHealthBar() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        String displayName = formatColors(healthBarDisplayNameFormat);
        this.objective = this.board.registerNewObjective("healthBar", "health");
        this.objective.setDisplayName(displayName);
        this.objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        Bukkit.getOnlinePlayers().forEach(this::updateHealthBar);
    }

    private void updateHealthBar(Player player) {
        Bukkit.getScheduler().runTask(main, () -> {
            if (isWorldDisabled(player.getWorld())) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            } else {
                player.setScoreboard(board);
            }
        });
    }

    private boolean isWorldDisabled(World world) {
        return disabledWorlds != null && disabledWorlds.contains(world.getName());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) {
            Player clickedPlayer = (Player) e.getRightClicked();
            Player player = e.getPlayer();
            int ping = PingCommand.getPing(clickedPlayer);
            String mesg = main.getConfig().getString("show-ping-on-right-click.message")
                    .replace("%ping%", String.valueOf(ping))
                    .replace("%clicked_player%", clickedPlayer.getName());
            if (main.getConfig().getBoolean("show-ping-on-right-click.enabled")) {
                ActionBarUtil.sendActionBar(player, mesg);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player attacker = player.getKiller();
        if (attacker != null) {
            if (SettingsManager.hasEnabledSetting(attacker, "autoGG")) {
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    attacker.chat("gg");
                }, 4L);
            }
        }
        if (main.getConfig().getBoolean("disableDeathDrops")) {
            e.getDrops().clear();
        }
        if (main.getConfig().getBoolean("heal_on_kill")) {
            heal(player);
        }
    }

    public static void heal(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setFireTicks(0);
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (e.isSneaking()) {
            if (SitUtil.isSitting(player)) {
                SitUtil.standup(player);
            }
        }
    }
}