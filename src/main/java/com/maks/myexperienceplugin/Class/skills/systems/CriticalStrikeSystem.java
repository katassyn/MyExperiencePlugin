package com.maks.myexperienceplugin.Class.skills.systems;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dedicated critical strike tracking system for all classes
 */
public class CriticalStrikeSystem {
    private final MyExperiencePlugin plugin;

    // Base critical chance per player
    private final Map<UUID, Double> baseCritChance = new ConcurrentHashMap<>();

    // Critical damage multiplier per player
    private final Map<UUID, Double> critDamageMultiplier = new ConcurrentHashMap<>();

    // Temporary critical bonuses
    private final Map<UUID, Double> tempCritChance = new ConcurrentHashMap<>();
    private final Map<UUID, Double> tempCritMultiplier = new ConcurrentHashMap<>();

    public CriticalStrikeSystem(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get total critical chance for a player
     */
    public double getCriticalChance(Player player) {
        UUID playerId = player.getUniqueId();
        double baseChance = baseCritChance.getOrDefault(playerId, 0.0);
        double tempChance = tempCritChance.getOrDefault(playerId, 0.0);
        return Math.min(100.0, baseChance + tempChance); // Cap at 100%
    }

    /**
     * Get critical damage multiplier for a player
     */
    public double getCriticalDamageMultiplier(Player player) {
        UUID playerId = player.getUniqueId();
        double baseMultiplier = critDamageMultiplier.getOrDefault(playerId, 2.0); // Default 2x
        double tempMultiplier = tempCritMultiplier.getOrDefault(playerId, 0.0);
        return baseMultiplier + tempMultiplier;
    }

    /**
     * Set base critical chance
     */
    public void setBaseCritChance(Player player, double chance) {
        baseCritChance.put(player.getUniqueId(), chance);
    }

    /**
     * Add to base critical chance
     */
    public void addBaseCritChance(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        double current = baseCritChance.getOrDefault(playerId, 0.0);
        baseCritChance.put(playerId, current + amount);
    }

    /**
     * Set critical damage multiplier
     */
    public void setCritDamageMultiplier(Player player, double multiplier) {
        critDamageMultiplier.put(player.getUniqueId(), multiplier);
    }

    /**
     * Add to critical damage multiplier
     */
    public void addCritDamageMultiplier(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        double current = critDamageMultiplier.getOrDefault(playerId, 2.0);
        critDamageMultiplier.put(playerId, current + amount);
    }

    /**
     * Add temporary critical chance
     */
    public void addTempCritChance(Player player, double amount, long durationTicks) {
        UUID playerId = player.getUniqueId();
        double current = tempCritChance.getOrDefault(playerId, 0.0);
        tempCritChance.put(playerId, current + amount);

        // Schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                removeTempCritChance(player, amount);
            }
        }, durationTicks);
    }

    /**
     * Remove temporary critical chance
     */
    private void removeTempCritChance(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        double current = tempCritChance.getOrDefault(playerId, 0.0);
        tempCritChance.put(playerId, Math.max(0, current - amount));
    }

    /**
     * Add temporary critical damage multiplier
     */
    public void addTempCritMultiplier(Player player, double amount, long durationTicks) {
        UUID playerId = player.getUniqueId();
        double current = tempCritMultiplier.getOrDefault(playerId, 0.0);
        tempCritMultiplier.put(playerId, current + amount);

        // Schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                removeTempCritMultiplier(player, amount);
            }
        }, durationTicks);
    }

    /**
     * Remove temporary critical damage multiplier
     */
    private void removeTempCritMultiplier(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        double current = tempCritMultiplier.getOrDefault(playerId, 0.0);
        tempCritMultiplier.put(playerId, Math.max(0, current - amount));
    }

    /**
     * Check if a critical hit occurs based on player's critical chance
     * @return true if critical hit, false otherwise
     */
    public boolean rollForCritical(Player player) {
        double chance = getCriticalChance(player);
        return Math.random() * 100 < chance;
    }

    /**
     * Clear all data for a player
     */
    public void clearPlayerData(UUID playerId) {
        baseCritChance.remove(playerId);
        critDamageMultiplier.remove(playerId);
        tempCritChance.remove(playerId);
        tempCritMultiplier.remove(playerId);
    }
}
