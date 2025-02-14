package com.maks.myexperienceplugin.listener;

import com.maks.myexperienceplugin.party.PartyManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnectListener implements Listener {
    private final PartyManager partyManager;

    public PlayerDisconnectListener(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        partyManager.removePlayerFromParty(event.getPlayer());
    }
}
