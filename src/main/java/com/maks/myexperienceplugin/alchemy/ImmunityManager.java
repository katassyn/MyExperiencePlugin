package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ImmunityManager {
    private static ImmunityManager instance;
    private final Set<UUID> immunePlayers = new HashSet<>();

    public static ImmunityManager getInstance() {
        if (instance == null) {
            instance = new ImmunityManager();
        }
        return instance;
    }

    public void setImmune(Player player, boolean immune) {
        if (immune) {
            immunePlayers.add(player.getUniqueId());
        } else {
            immunePlayers.remove(player.getUniqueId());
        }
    }

    public boolean isImmune(Player player) {
        return immunePlayers.contains(player.getUniqueId());
    }
}
