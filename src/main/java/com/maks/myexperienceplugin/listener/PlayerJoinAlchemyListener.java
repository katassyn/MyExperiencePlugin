package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.AlchemyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listener to restore alchemy effects when a player rejoins the server
 */
public class PlayerJoinAlchemyListener implements Listener {
    private final AlchemyManager alchemyManager;
    private final JavaPlugin plugin;
    private static final int debuggingFlag = 0;

    public PlayerJoinAlchemyListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.alchemyManager = AlchemyManager.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        // Slight delay to ensure player is fully loaded before restoring effects
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[DEBUG] Checking for alchemy effects to restore for player " + player.getName());
            }

            alchemyManager.checkAndRestoreEffects(player);
        }, 20L); // 1 second delay
    }
}
