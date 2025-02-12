package com.maks.myexperienceplugin.alchemy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhysisExpManager {
    private static PhysisExpManager instance;
    private final Map<UUID, Double> expBonuses = new HashMap<>();

    private PhysisExpManager() {}

    public static PhysisExpManager getInstance() {
        if (instance == null) {
            instance = new PhysisExpManager();
        }
        return instance;
    }

    public void setExpBonus(Player player, double bonusPercentage) {
        expBonuses.put(player.getUniqueId(), bonusPercentage);
    }

    public void removeExpBonus(Player player) {
        expBonuses.remove(player.getUniqueId());
    }

    public double getExpBonus(Player player) {
        return expBonuses.getOrDefault(player.getUniqueId(), 0.0);
    }

    // Metoda czyszcząca bonusy przy wylogowaniu gracza
    public void clearPlayerBonus(UUID playerId) {
        expBonuses.remove(playerId);
    }

    // Metoda sprawdzająca, czy gracz ma aktywny bonus
    public boolean hasActiveBonus(Player player) {
        return expBonuses.containsKey(player.getUniqueId());
    }
}