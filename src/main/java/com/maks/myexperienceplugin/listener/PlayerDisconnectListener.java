package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.alchemy.AlchemyManager;
import com.maks.myexperienceplugin.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {
    private final PartyManager partyManager;
    private static final int debuggingFlag = 0;

    public PlayerDisconnectListener(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Handle party removal
        partyManager.removePlayerFromParty(event.getPlayer());

        // Handle alchemy effects - now they persist, but we need to cancel scheduled tasks
        AlchemyManager.getInstance().handlePlayerDisconnect(event.getPlayer());

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[DEBUG] Handled disconnect for player: " + event.getPlayer().getName());
        }
    }
}
