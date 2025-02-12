package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifestealManager {
    private static LifestealManager instance;
    private final Map<UUID, Double> lifestealMap = new HashMap<>();

    public static LifestealManager getInstance() {
        if (instance == null) {
            instance = new LifestealManager();
        }
        return instance;
    }

    public void setLifesteal(Player player, double percentage) {
        lifestealMap.put(player.getUniqueId(), percentage);
    }

    public void removeLifesteal(Player player) {
        lifestealMap.remove(player.getUniqueId());
    }

    public double getLifesteal(Player player) {
        return lifestealMap.getOrDefault(player.getUniqueId(), 0.0);
    }
}
