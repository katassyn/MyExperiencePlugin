package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Base class for all skill effect handlers. Time unit: ticks (20 ticks = 1s). */
public abstract class BaseSkillEffectsHandler {
    protected final MyExperiencePlugin plugin;
    protected int debuggingFlag = 0;
    
    // Unified effect tracking system - shared across all handlers
    protected static final PlayerEffectState effectState = new PlayerEffectState();
    
    // Performance monitoring system - shared across all handlers
    protected static PerformanceMonitor performanceMonitor;

    protected BaseSkillEffectsHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
        
        // Initialize performance monitor if not already done
        if (performanceMonitor == null) {
            performanceMonitor = new PerformanceMonitor(plugin);
        }
    }
    
    // ===== UNIFIED UTILITY METHODS =====
    
    /**
     * Unified proc chance calculation with Elemental Affinity bonus support
     * @param player The player performing the action
     * @param baseChance Base chance (0.0 to 1.0)
     * @param skillName Name for debugging
     * @param elementalistAffinityId ID of Elementalist Affinity skill (0 to disable)
     * @return true if proc succeeded
     */
    protected boolean rollProc(Player player, double baseChance, String skillName, int elementalistAffinityId) {
        UUID playerId = player.getUniqueId();
        double finalChance = baseChance;
        
        // Add Elementalist Affinity bonus (+15% proc chance)
        if (elementalistAffinityId > 0 && isPurchased(playerId, elementalistAffinityId)) {
            finalChance += 0.15;
        }
        
        boolean success = Math.random() < finalChance;
        
        // Debug output
        if (debuggingFlag == 1 && skillName != null) {
            String msg = String.format("[PROC] %s: %.1f%% -> %.1f%% = %s", 
                skillName, baseChance * 100, finalChance * 100, success ? "SUCCESS" : "FAIL");
            ChatNotificationUtils.send(player, ChatColor.GRAY + msg);
        }
        
        return success;
    }
    
    /**
     * Simplified proc roll without affinity bonus
     */
    protected boolean rollProc(Player player, double baseChance, String skillName) {
        return rollProc(player, baseChance, skillName, 0);
    }
    
    /**
     * Silent proc roll (no debug output)
     */
    protected boolean rollProc(Player player, double baseChance) {
        return rollProc(player, baseChance, null, 0);
    }
    
    /**
     * Standardized chance roll with debug logging
     * @param chance Chance value (0.0 to 1.0 OR 0-100 based on usePercentage)
     * @param player Player for debug messages
     * @param mechanicName Name of the mechanic being rolled
     * @param usePercentage If true, chance is 0-100, otherwise 0.0-1.0
     * @return Whether the roll succeeded
     */
    protected boolean rollChanceWithDebug(double chance, Player player, String mechanicName, boolean usePercentage) {
        double normalizedChance = usePercentage ? chance / 100.0 : chance;
        boolean success = Math.random() < normalizedChance;
        
        if (debuggingFlag == 1 && player != null && mechanicName != null) {
            String msg = String.format("[%s] %.1f%% chance = %s", 
                mechanicName, normalizedChance * 100, success ? "SUCCESS" : "FAIL");
            ChatNotificationUtils.send(player, ChatColor.GRAY + msg);
        }
        
        return success;
    }
    
    /**
     * Standard percentage-based chance roll (0-100)
     */
    protected boolean rollPercentageChance(double percentChance, Player player, String mechanicName) {
        return rollChanceWithDebug(percentChance, player, mechanicName, true);
    }
    
    /**
     * Standard decimal-based chance roll (0.0-1.0)
     */
    protected boolean rollDecimalChance(double decimalChance, Player player, String mechanicName) {
        return rollChanceWithDebug(decimalChance, player, mechanicName, false);
    }
    
    /**
     * Silent chance roll with no debug output (0-100 percentage)
     */
    protected boolean rollPercentageChanceSilent(double percentChance) {
        return Math.random() * 100 < percentChance;
    }
    
    /**
     * Unified spell damage calculation with SpellWeaver bonuses
     * @param baseDamage Raw damage before modifiers
     * @param caster The spell caster  
     * @param stats Player stats containing bonuses
     * @return Final damage after all modifiers
     */
    protected double calculateSpellDamage(double baseDamage, Player caster, SkillEffectsHandler.PlayerSkillStats stats) {
        return performanceMonitor.trackExecution("calculateSpellDamage", () -> {
            double damage = baseDamage + stats.getSpellDamageBonus();
            damage *= stats.getSpellDamageMultiplier();
            
            // Check for critical strike
            if (rollCritical(caster, stats)) {
                double critMultiplier = getCriticalMultiplier(caster);
                damage *= critMultiplier;
                showCriticalEffect(caster, critMultiplier);
            }
            
            return Math.max(0, damage);
        });
    }
    
    /**
     * Check if spell critical strike occurs
     */
    private boolean rollCritical(Player caster, SkillEffectsHandler.PlayerSkillStats stats) {
        return Math.random() * 100 < stats.getSpellCriticalChance();
    }
    
    /**
     * Get critical damage multiplier (can be overridden for class-specific bonuses)
     */
    protected double getCriticalMultiplier(Player caster) {
        return 2.0; // Default 2x damage
    }
    
    /**
     * Show critical strike effect to player
     */
    private void showCriticalEffect(Player caster, double multiplier) {
        String msg = String.format("Spell Critical! x%.1f dmg", multiplier);
        ActionBarUtils.sendActionBar(caster, ChatColor.LIGHT_PURPLE + msg);
    }
    
    /**
     * Check if player has purchased a skill (cached for performance)
     */
    protected boolean isPurchased(UUID playerId, int skillId) {
        return performanceMonitor.trackExecution("isPurchased", () -> {
            Set<Integer> purchasedSkills = performanceMonitor.getCachedSkillPurchases(playerId);
            return purchasedSkills.contains(skillId);
        });
    }
    
    /**
     * Get skill purchase count
     */
    protected int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }
    
    /**
     * Bulk skill check helper - check multiple skills at once for better performance
     * @param playerId Player UUID
     * @param skillIds Array of skill IDs to check
     * @return Set containing only the purchased skill IDs
     */
    protected Set<Integer> getBulkPurchasedSkills(UUID playerId, int... skillIds) {
        Set<Integer> purchased = performanceMonitor.getCachedSkillPurchases(playerId);
        Set<Integer> result = new HashSet<>();
        
        for (int skillId : skillIds) {
            if (purchased.contains(skillId)) {
                result.add(skillId);
            }
        }
        
        return result;
    }
    
    /**
     * Check if player has ANY of the specified skills (OR condition)
     */
    protected boolean hasAnySkill(UUID playerId, int... skillIds) {
        Set<Integer> purchased = performanceMonitor.getCachedSkillPurchases(playerId);
        for (int skillId : skillIds) {
            if (purchased.contains(skillId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if player has ALL of the specified skills (AND condition)  
     */
    protected boolean hasAllSkills(UUID playerId, int... skillIds) {
        Set<Integer> purchased = performanceMonitor.getCachedSkillPurchases(playerId);
        for (int skillId : skillIds) {
            if (!purchased.contains(skillId)) {
                return false;
            }
        }
        return true;
    }
    
    // ===== UNIFIED EFFECT TRACKING HELPERS =====
    
    /**
     * Schedule periodic cleanup of effect state and other expired data
     */
    protected void schedulePeriodicCleanup() {
        // Clean up effect state every 5 minutes
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            effectState::cleanup, 6000L, 6000L);
            
        // Additional cleanup task for subclass-specific data every 2 minutes
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            this::cleanupExpiredData, 2400L, 2400L);
    }
    
    /**
     * Override this method in subclasses to clean up expired data specific to that handler
     * Called every 2 minutes automatically
     */
    protected void cleanupExpiredData() {
        // Base implementation does nothing - subclasses can override
        // Example: Remove expired cooldowns, clear invalid entity references, etc.
    }
    
    /**
     * Clean up expired cooldowns from any cooldown map
     * @param cooldownMap The map to clean (UUID -> expiry time in ms)
     */
    protected void cleanupExpiredCooldowns(Map<UUID, Long> cooldownMap) {
        if (cooldownMap == null || cooldownMap.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        cooldownMap.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
    
    /**
     * Clean up expired timed effects from any timed effect map  
     * @param effectMap The map to clean (UUID -> expiry time in ms)
     */
    protected void cleanupExpiredTimedEffects(Map<UUID, Long> effectMap) {
        cleanupExpiredCooldowns(effectMap); // Same logic
    }
    
    /**
     * Clean up invalid entity references by checking if entities still exist
     * @param entityMap Map containing entity UUIDs as values
     */
    protected <T> void cleanupInvalidEntityReferences(Map<T, UUID> entityMap) {
        if (entityMap == null || entityMap.isEmpty()) {
            return;
        }
        
        entityMap.entrySet().removeIf(entry -> {
            UUID entityId = entry.getValue();
            if (entityId == null) {
                return true; // Remove null entries
            }
            
            // Check if entity still exists in any world
            return plugin.getServer().getWorlds().stream()
                    .noneMatch(world -> world.getEntities().stream()
                            .anyMatch(entity -> entity.getUniqueId().equals(entityId)));
        });
    }
    
    /**
     * Apply elemental effect to enemy
     */
    protected void applyElementalEffect(Player caster, UUID targetId, PlayerEffectState.EffectType type, long durationMs) {
        effectState.addEnemyEffect(caster.getUniqueId(), targetId, type, durationMs, 1, 1.0);
        
        if (debuggingFlag == 1) {
            ActionBarUtils.sendActionBar(caster, ChatColor.YELLOW + type.getDisplayName() + " applied!");
        }
    }
    
    /**
     * Apply buff to player
     */
    protected void applyPlayerBuff(Player player, PlayerEffectState.EffectType type, long durationMs, double strength) {
        effectState.addSelfEffect(player.getUniqueId(), type, durationMs, 1, strength);
        
        if (debuggingFlag == 1) {
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + type.getDisplayName() + " activated!");
        }
    }
    
    /**
     * Check if enemy has specific elemental effect
     */
    protected boolean enemyHasElementalEffect(UUID casterId, UUID targetId, PlayerEffectState.EffectType type) {
        return effectState.enemyHasEffect(casterId, targetId, type);
    }
    
    /**
     * Check if player has buff active
     */
    protected boolean playerHasBuff(UUID playerId, PlayerEffectState.EffectType type) {
        return effectState.playerHasEffect(playerId, type);
    }
    
    /**
     * Set skill cooldown
     */
    protected void setCooldown(UUID playerId, String skillKey, long cooldownMs) {
        effectState.setCooldown(playerId, skillKey, cooldownMs);
    }
    
    /**
     * Check if skill is on cooldown
     */
    protected boolean isOnCooldown(UUID playerId, String skillKey) {
        return effectState.isOnCooldown(playerId, skillKey);
    }
    
    /**
     * Clear all data for player (call on logout)
     */
    public void clearAllPlayerData(UUID playerId) {
        effectState.clearPlayerData(playerId);
        performanceMonitor.invalidateSkillCache(playerId);
    }
    
    /**
     * Get performance monitor instance for advanced usage
     */
    public static PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
    
    // ===== DEFENSIVE UTILITY METHODS =====
    
    /**
     * Safely get player's max health with null checks and fallback (cached)
     * @param player The player to get max health for
     * @return Max health value, or 20.0 as fallback
     */
    protected static double getMaxHealthSafely(Player player) {
        if (player == null) {
            return 20.0; // Default MC player health
        }
        
        UUID playerId = player.getUniqueId();
        
        // Check cache first
        if (performanceMonitor != null) {
            Double cached = performanceMonitor.getCachedAttribute(playerId, "maxHealth");
            if (cached != null) {
                return cached;
            }
        }
        
        // Cache miss - get from attribute
        double maxHealth = 20.0; // fallback
        try {
            AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                maxHealth = healthAttr.getValue();
            }
        } catch (Exception e) {
            // Log but don't crash - use fallback
        }
        
        // Cache the result
        if (performanceMonitor != null) {
            performanceMonitor.cacheAttribute(playerId, "maxHealth", maxHealth);
        }
        
        return maxHealth;
    }
    
    /**
     * Safely get player's current health with null checks
     * @param player The player to get health for
     * @return Current health, or max health as fallback
     */
    protected static double getCurrentHealthSafely(Player player) {
        if (player == null) {
            return 20.0;
        }
        
        try {
            double currentHealth = player.getHealth();
            // Ensure we don't return invalid values
            return Math.max(0.0, currentHealth);
        } catch (Exception e) {
            // Return max health as fallback
            return getMaxHealthSafely(player);
        }
    }
    
    /**
     * Safely set player health with validation
     * @param player The player to set health for
     * @param newHealth The new health value
     * @return true if successful, false if failed
     */
    protected static boolean setHealthSafely(Player player, double newHealth) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        try {
            double maxHealth = getMaxHealthSafely(player);
            double safeHealth = Math.max(0.0, Math.min(newHealth, maxHealth));
            
            player.setHealth(safeHealth);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calculate health percentage safely
     * @param player The player to check
     * @return Health percentage (0.0 to 100.0), or 100.0 if error
     */
    protected static double getHealthPercentageSafely(Player player) {
        if (player == null || !player.isOnline()) {
            return 100.0;
        }
        
        try {
            double maxHealth = getMaxHealthSafely(player);
            double currentHealth = getCurrentHealthSafely(player);
            
            if (maxHealth <= 0) {
                return 100.0; // Avoid division by zero
            }
            
            return Math.max(0.0, Math.min(100.0, (currentHealth / maxHealth) * 100.0));
        } catch (Exception e) {
            return 100.0; // Safe fallback
        }
    }
    
    /**
     * Safely get attribute value with null checks and fallback
     * @param player The player
     * @param attribute The attribute to get
     * @param fallback Fallback value if attribute is null
     * @return Attribute value or fallback
     */
    protected static double getAttributeSafely(Player player, Attribute attribute, double fallback) {
        if (player == null || attribute == null) {
            return fallback;
        }
        
        try {
            AttributeInstance attr = player.getAttribute(attribute);
            return attr != null ? attr.getValue() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Add passive bonuses from purchased nodes into stats (called during stats rebuild). */
    public abstract void applySkillEffects(
            SkillEffectsHandler.PlayerSkillStats stats,
            int skillId,
            int purchaseCount,
            Player player
    );

    /** Called when player receives damage. */
    public abstract void handleEntityDamage(
            EntityDamageEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );

    /** Called when player deals damage. */
    public abstract void handleEntityDamageByEntity(
            EntityDamageByEntityEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );

    /** Called when an entity dies (for on-kill mechanics). */
    public abstract void handleEntityDeath(
            EntityDeathEvent event,
            Player player,
            SkillEffectsHandler.PlayerSkillStats stats
    );
}
