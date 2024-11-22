package com.maks.myexperienceplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private final MyExperiencePlugin plugin;

    public PlayerJoinListener(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        // Ustaw dane domyślne przed załadowaniem z bazy
        plugin.playerLevels.put(playerId, 1); // Domyślny poziom
        plugin.playerCurrentXP.put(playerId, 0.0); // Domyślne XP
        plugin.updatePlayerXPBar(event.getPlayer()); // Aktualizacja paska XP

        // Załaduj dane z bazy
        plugin.getDatabaseManager().loadPlayerData(event.getPlayer());
    }
}
