package com.maks.myexperienceplugin.Class.skills.effects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified system for tracking player effects across all ascendancy classes.
 * Replaces the dozens of individual Map<UUID, *> with a centralized solution.
 */
public class PlayerEffectState {
    
    /**
     * All possible effect types across all ascendancy classes
     */
    public enum EffectType {
        // Elementalist Effects
        BURNING("Burning", true, true),
        FROZEN("Frozen", true, true),
        CHARGED("Charged", true, true),
        STONED("Stoned", true, true),
        FIRE_SHIELD("Fire Shield", false, false),
        ICE_BARRIER("Ice Barrier", false, false),
        ELEMENTAL_SURGE("Elemental Surge", false, false),
        STONE_KILL_BUFF("Stone Kill Buff", false, false),
        
        // Chronomancer Effects  
        SLOWED("Slowed", true, true),
        TIME_BUBBLE("Time Bubble", true, false),
        SPEED_SURGE("Speed Surge", false, true),
        EMPOWERED_HIT("Empowered Hit", false, false),
        TIME_FRACTURE("Time Fracture", true, false),
        TEMPORAL_ECHO("Temporal Echo", false, false),
        
        // Arcane Protector Effects
        ARCANE_SHIELD("Arcane Shield", false, false),
        PERIODIC_BARRIER("Periodic Barrier", false, false),
        ARCANE_AEGIS("Arcane Aegis", false, false),
        ARCANE_ARMOR("Arcane Armor", false, false),
        WEAKENED("Weakened", true, false),
        LAST_STAND("Last Stand", false, false),
        CHEAT_DEATH("Cheat Death", false, false);
        
        private final String displayName;
        private final boolean appliesToEnemies;
        private final boolean canStack;
        
        EffectType(String displayName, boolean appliesToEnemies, boolean canStack) {
            this.displayName = displayName;
            this.appliesToEnemies = appliesToEnemies;
            this.canStack = canStack;
        }
        
        public String getDisplayName() { return displayName; }
        public boolean appliesToEnemies() { return appliesToEnemies; }
        public boolean canStack() { return canStack; }
    }
    
    /**
     * Effect data container
     */
    public static class Effect {
        private final EffectType type;
        private final long expiryTime;
        private final int stacks;
        private final double strength;
        private final Map<String, Object> metadata;
        
        public Effect(EffectType type, long durationMs, int stacks, double strength) {
            this.type = type;
            this.expiryTime = System.currentTimeMillis() + durationMs;
            this.stacks = stacks;
            this.strength = strength;
            this.metadata = new ConcurrentHashMap<>();
        }
        
        public EffectType getType() { return type; }
        public long getExpiryTime() { return expiryTime; }
        public int getStacks() { return stacks; }
        public double getStrength() { return strength; }
        public boolean isActive() { return System.currentTimeMillis() < expiryTime; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public Effect withStacks(int newStacks) {
            return new Effect(type, expiryTime - System.currentTimeMillis(), newStacks, strength);
        }
        
        public Effect withStrength(double newStrength) {
            return new Effect(type, expiryTime - System.currentTimeMillis(), stacks, newStrength);
        }
    }
    
    // Player UUID -> Effect Type -> Target UUID -> Effect (for effects on enemies)
    private final Map<UUID, Map<EffectType, Map<UUID, Effect>>> playerToEnemyEffects = new ConcurrentHashMap<>();
    
    // Player UUID -> Effect Type -> Effect (for effects on self)
    private final Map<UUID, Map<EffectType, Effect>> playerToSelfEffects = new ConcurrentHashMap<>();
    
    // Cooldown tracking: Player UUID -> Skill ID -> Expiry Time
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    
    /**
     * Add an effect on an enemy
     */
    public void addEnemyEffect(UUID playerId, UUID targetId, EffectType type, long durationMs, int stacks, double strength) {
        if (!type.appliesToEnemies()) {
            throw new IllegalArgumentException("Effect type " + type + " cannot be applied to enemies");
        }
        
        Effect effect = new Effect(type, durationMs, stacks, strength);
        playerToEnemyEffects
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(type, k -> new ConcurrentHashMap<>())
            .put(targetId, effect);
    }
    
    /**
     * Add an effect on the player themselves
     */
    public void addSelfEffect(UUID playerId, EffectType type, long durationMs, int stacks, double strength) {
        Effect effect = new Effect(type, durationMs, stacks, strength);
        playerToSelfEffects
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(type, effect);
    }
    
    /**
     * Check if enemy has an effect
     */
    public boolean enemyHasEffect(UUID playerId, UUID targetId, EffectType type) {
        Map<EffectType, Map<UUID, Effect>> playerEffects = playerToEnemyEffects.get(playerId);
        if (playerEffects == null) return false;
        
        Map<UUID, Effect> typeEffects = playerEffects.get(type);
        if (typeEffects == null) return false;
        
        Effect effect = typeEffects.get(targetId);
        return effect != null && effect.isActive();
    }
    
    /**
     * Check if player has a self effect
     */
    public boolean playerHasEffect(UUID playerId, EffectType type) {
        Map<EffectType, Effect> playerEffects = playerToSelfEffects.get(playerId);
        if (playerEffects == null) return false;
        
        Effect effect = playerEffects.get(type);
        return effect != null && effect.isActive();
    }
    
    /**
     * Get effect stacks on enemy
     */
    public int getEnemyEffectStacks(UUID playerId, UUID targetId, EffectType type) {
        if (!enemyHasEffect(playerId, targetId, type)) return 0;
        
        return playerToEnemyEffects.get(playerId).get(type).get(targetId).getStacks();
    }
    
    /**
     * Get effect stacks on player
     */
    public int getSelfEffectStacks(UUID playerId, EffectType type) {
        if (!playerHasEffect(playerId, type)) return 0;
        
        return playerToSelfEffects.get(playerId).get(type).getStacks();
    }
    
    /**
     * Get effect strength on enemy
     */
    public double getEnemyEffectStrength(UUID playerId, UUID targetId, EffectType type) {
        if (!enemyHasEffect(playerId, targetId, type)) return 0.0;
        
        return playerToEnemyEffects.get(playerId).get(type).get(targetId).getStrength();
    }
    
    /**
     * Get effect strength on player
     */
    public double getSelfEffectStrength(UUID playerId, EffectType type) {
        if (!playerHasEffect(playerId, type)) return 0.0;
        
        return playerToSelfEffects.get(playerId).get(type).getStrength();
    }
    
    /**
     * Count active effects on enemies of specific types
     */
    public int countActiveEnemyEffects(UUID playerId, EffectType... types) {
        Set<EffectType> typeSet = new HashSet<>(Arrays.asList(types));
        Map<EffectType, Map<UUID, Effect>> playerEffects = playerToEnemyEffects.get(playerId);
        if (playerEffects == null) return 0;
        
        int count = 0;
        for (Map.Entry<EffectType, Map<UUID, Effect>> entry : playerEffects.entrySet()) {
            if (typeSet.isEmpty() || typeSet.contains(entry.getKey())) {
                count += (int) entry.getValue().values().stream()
                    .filter(Effect::isActive)
                    .count();
            }
        }
        return count;
    }
    
    /**
     * Set cooldown for a skill
     */
    public void setCooldown(UUID playerId, String skillKey, long cooldownMs) {
        cooldowns
            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .put(skillKey, System.currentTimeMillis() + cooldownMs);
    }
    
    /**
     * Check if skill is on cooldown
     */
    public boolean isOnCooldown(UUID playerId, String skillKey) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        
        Long expiry = playerCooldowns.get(skillKey);
        return expiry != null && expiry > System.currentTimeMillis();
    }
    
    /**
     * Get remaining cooldown time in milliseconds
     */
    public long getRemainingCooldown(UUID playerId, String skillKey) {
        if (!isOnCooldown(playerId, skillKey)) return 0;
        
        Long expiry = cooldowns.get(playerId).get(skillKey);
        return expiry - System.currentTimeMillis();
    }
    
    /**
     * Cleanup expired effects and cooldowns
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        
        // Cleanup enemy effects
        playerToEnemyEffects.values().forEach(typeMap -> {
            typeMap.values().forEach(targetMap -> {
                targetMap.entrySet().removeIf(entry -> !entry.getValue().isActive());
            });
            typeMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        });
        playerToEnemyEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        // Cleanup self effects
        playerToSelfEffects.values().forEach(typeMap -> {
            typeMap.entrySet().removeIf(entry -> !entry.getValue().isActive());
        });
        playerToSelfEffects.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        // Cleanup cooldowns
        cooldowns.values().forEach(skillMap -> {
            skillMap.entrySet().removeIf(entry -> entry.getValue() <= now);
        });
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Clear all data for a player (on logout/disconnect)
     */
    public void clearPlayerData(UUID playerId) {
        playerToEnemyEffects.remove(playerId);
        playerToSelfEffects.remove(playerId);
        cooldowns.remove(playerId);
    }
    
    /**
     * Get all active effects for debugging
     */
    public Map<String, Object> getDebugInfo(UUID playerId) {
        Map<String, Object> debug = new HashMap<>();
        
        Map<EffectType, Map<UUID, Effect>> enemyEffects = playerToEnemyEffects.get(playerId);
        if (enemyEffects != null) {
            Map<String, Integer> activeEnemyEffects = new HashMap<>();
            for (Map.Entry<EffectType, Map<UUID, Effect>> entry : enemyEffects.entrySet()) {
                int count = (int) entry.getValue().values().stream()
                    .filter(Effect::isActive)
                    .count();
                if (count > 0) {
                    activeEnemyEffects.put(entry.getKey().getDisplayName(), count);
                }
            }
            debug.put("enemyEffects", activeEnemyEffects);
        }
        
        Map<EffectType, Effect> selfEffects = playerToSelfEffects.get(playerId);
        if (selfEffects != null) {
            List<String> activeSelfEffects = new ArrayList<>();
            for (Effect effect : selfEffects.values()) {
                if (effect.isActive()) {
                    activeSelfEffects.add(effect.getType().getDisplayName());
                }
            }
            debug.put("selfEffects", activeSelfEffects);
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) {
            Map<String, Long> activeCooldowns = new HashMap<>();
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
                if (entry.getValue() > now) {
                    activeCooldowns.put(entry.getKey(), entry.getValue() - now);
                }
            }
            debug.put("cooldowns", activeCooldowns);
        }
        
        return debug;
    }
}