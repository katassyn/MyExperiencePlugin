package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TonicExpManager {
    private static TonicExpManager instance;
    private final Map<UUID, Double> bonusMap = new HashMap<>();

    public static TonicExpManager getInstance() {
        if (instance == null) {
            instance = new TonicExpManager();
        }
        return instance;
    }

    public void setBonus(Player player, double bonusPercentage) {
        bonusMap.put(player.getUniqueId(), bonusPercentage);
    }

    public void removeBonus(Player player) {
        bonusMap.remove(player.getUniqueId());
    }

    public double getBonus(Player player) {
        return bonusMap.getOrDefault(player.getUniqueId(), 0.0);
    }
}
