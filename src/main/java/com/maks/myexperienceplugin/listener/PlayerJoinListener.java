package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.MyExperiencePlugin;
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

        plugin.playerLevels.put(playerId, 1);
        plugin.playerCurrentXP.put(playerId, 0.0);
        plugin.updatePlayerXPBar(event.getPlayer());

        // Load XP from DB
        plugin.getDatabaseManager().loadPlayerData(event.getPlayer());
        // Load class data from DB
        plugin.getClassManager().loadPlayerClassData(event.getPlayer());
        plugin.getSkillTreeManager().loadPlayerSkillData(event.getPlayer());
    }
}
