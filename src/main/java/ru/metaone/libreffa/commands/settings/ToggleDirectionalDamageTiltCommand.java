package ru.metaone.libreffa.commands.settings;

import ru.metaone.libreffa.Main;
import ru.metaone.libreffa.settings.SettingsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleDirectionalDamageTiltCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        SettingsManager.toggleSetting(player, "OldDamageTilt");

        String toggleMessage = Main.getInstance().getConfig().getString("messages.setting_toggle");

        String status = SettingsManager.getUnformattedSettingStatus(player, "OldDamageTilt");
        String msg = toggleMessage
                .replace("{setting_name}", "Directional Damage Tilt")
                .replace("{setting_status}", status);

        player.sendMessage(Main.formatColors(msg));

        return true;
    }
}
