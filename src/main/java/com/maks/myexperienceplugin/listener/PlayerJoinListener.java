package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Minimal version: we do NOT automatically open GUIs or do /class commands here,
 * because PeriodicClassReminder handles that now.
 */
public class PlayerJoinListener implements Listener {

    private final MyExperiencePlugin plugin;

    public PlayerJoinListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();

        plugin.playerLevels.put(playerId, 1);
        plugin.playerCurrentXP.put(playerId, 0.0);
        plugin.updatePlayerXPBar(player);

        // Load XP from DB
        plugin.getDatabaseManager().loadPlayerData(player);
        // Load class data from DB
        plugin.getClassManager().loadPlayerClassData(player);
        plugin.getSkillTreeManager().loadPlayerSkillData(player);

        // Check for unspent skill points after a short delay (to ensure data is loaded)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            SkillTreeManager manager = plugin.getSkillTreeManager();
            if (manager == null) {
                return;
            }

            int unusedBasic = manager.getUnusedBasicSkillPoints(playerId);
            if (unusedBasic > 0) {
                player.sendMessage("\u00a7eYou have \u00a7c" + unusedBasic +
                        " \u00a7eskill point" + (unusedBasic > 1 ? "s" : "") +
                        " to spend! Use the \u00a7aSkill Tree \u00a7emenu to allocate them.");
            }

            int unusedAsc = manager.getUnusedAscendancySkillPoints(playerId);
            if (unusedAsc > 0) {
                player.sendMessage("\u00a7eYou have \u00a7c" + unusedAsc +
                        " \u00a7eascendancy skill point" + (unusedAsc > 1 ? "s" : "") +
                        " to spend! Use the \u00a7aAscendancy Skill Tree \u00a7emenu to allocate them.");
            }
        }, 40L); // 40 ticks = 2 seconds delay
    }
}
