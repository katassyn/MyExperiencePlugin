package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.events.SkillPurchasedEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkillEffectsHandler implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final Random random = new Random();
    private final Map<UUID, Integer> hitCounters = new HashMap<>();
    private final Map<UUID, UUID> lastTargetMap = new HashMap<>();
    // Debugging flag - set to 0 after testing
    private final int debuggingFlag = 1;

    // Cached player stats
    private final Map<UUID, PlayerSkillStats> playerStatsCache = new ConcurrentHashMap<>();

    // Constants for attribute modifier names
    private static final String ATTR_MAX_HEALTH = "skill.maxhealth";
    private static final String ATTR_MOVEMENT_SPEED = "skill.movementspeed";
    private static final String ATTR_ATTACK_DAMAGE = "skill.attackdamage";
    private static final String ATTR_ARMOR = "skill.armor";
    private static final String ATTR_LUCK = "skill.luck";

    // Flag to prevent stacking multiple damage bonus messages
    private final Map<UUID, Long> lastDamageMessageTime = new HashMap<>();
    private static final long DAMAGE_MESSAGE_COOLDOWN = 1000; // 1 second cooldown

    // Add a field to track if the listener is currently handling an event
    private boolean isHandlingSkillEvent = false;

    public SkillEffectsHandler(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Completely recalculate all stats on join
        recalculateAllStats(player);

        if (debuggingFlag == 1) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Applied fresh stats for " + player.getName());
                PlayerSkillStats stats = playerStatsCache.get(player.getUniqueId());
                if (stats != null) {
                    plugin.getLogger().info("MaxHealthBonus: " + stats.getMaxHealthBonus());
                    plugin.getLogger().info("BonusDamage: " + stats.getBonusDamage() + " (should be exactly 5 for Ranger)");
                    plugin.getLogger().info("GoldPerKill: " + stats.getGoldPerKill() + " (should be exactly 3 for Ranger)");
                    plugin.getLogger().info("Current max health: " + player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                }
            }, 20L);
        }
    }

    /**
     * Listen for skill purchase events to update stats
     */
    @EventHandler
    public void onSkillPurchased(SkillPurchasedEvent event) {
        if (isHandlingSkillEvent) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Recursive skill event detected! Ignoring to prevent loop.");
            }
            return;
        }

        isHandlingSkillEvent = true;

        try {
            Player player = event.getPlayer();
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SkillPurchasedEvent received for player " + player.getName() +
                        ", skillId: " + event.getSkillId());
            }

            // Completely recalculate stats when a skill is purchased
            recalculateAllStats(player);
        } finally {
            isHandlingSkillEvent = false;
        }
    }

    /**
     * Complete stats recalculation, cleaning everything first
     */
    private void recalculateAllStats(Player player) {
        // First, clear all cached data and modifiers
        clearAllSkillModifiers(player);

        // Then calculate fresh stats
        calculatePlayerStats(player);

        // Then apply them
        applyPlayerStats(player);
    }

    /**
     * Clears all skill-related attribute modifiers from the player
     */
    private void clearAllSkillModifiers(Player player) {
        clearAttributeModifiers(player, Attribute.GENERIC_MAX_HEALTH, ATTR_MAX_HEALTH);
        clearAttributeModifiers(player, Attribute.GENERIC_MOVEMENT_SPEED, ATTR_MOVEMENT_SPEED);
        clearAttributeModifiers(player, Attribute.GENERIC_ATTACK_DAMAGE, ATTR_ATTACK_DAMAGE);
        clearAttributeModifiers(player, Attribute.GENERIC_ARMOR, ATTR_ARMOR);
        clearAttributeModifiers(player, Attribute.GENERIC_LUCK, ATTR_LUCK);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared skill modifiers for " + player.getName());
        }
    }

    /**
     * Clears modifiers with the given name prefix from the specified attribute
     */
    private void clearAttributeModifiers(Player player, Attribute attribute, String namePrefix) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr != null) {
            // Create a copy of modifiers to avoid ConcurrentModificationException
            List<AttributeModifier> toRemove = new ArrayList<>();
            attr.getModifiers().forEach(mod -> {
                if (mod.getName().startsWith(namePrefix)) {
                    toRemove.add(mod);
                }
            });

            // Now remove them
            toRemove.forEach(attr::removeModifier);
        }
    }

    /**
     * Applies the calculated stats to the player via attribute modifiers
     */
    private void applyPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkillStats stats = playerStatsCache.get(uuid);
        if (stats == null) return;

        // Apply max health bonus
        if (stats.getMaxHealthBonus() > 0) {
            AttributeModifier healthMod = new AttributeModifier(
                    ATTR_MAX_HEALTH,
                    stats.getMaxHealthBonus(),
                    AttributeModifier.Operation.ADD_NUMBER
            );

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied +" + stats.getMaxHealthBonus() + " max health to " + player.getName());
            }
        }

        // Apply movement speed bonus (as percentage)
        if (stats.getMovementSpeedBonus() != 0) {
            double speedBonus = stats.getMovementSpeedBonus() / 100.0;
            AttributeModifier speedMod = new AttributeModifier(
                    ATTR_MOVEMENT_SPEED,
                    speedBonus,
                    AttributeModifier.Operation.ADD_SCALAR
            );

            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(speedMod);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied " + stats.getMovementSpeedBonus() + "% movement speed to " + player.getName());
            }
        }

        // Apply other attributes as needed...
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply evade chance
        if (stats.getEvadeChance() > 0 && random.nextDouble() * 100 < stats.getEvadeChance()) {
            event.setCancelled(true);
            player.sendMessage("§a§oYou evaded the attack!");
            return;
        }

        // Apply shield block chance
        if (stats.getShieldBlockChance() > 0 && random.nextDouble() * 100 < stats.getShieldBlockChance()) {
            event.setDamage(event.getDamage() * 0.5); // 50% damage reduction
            player.sendMessage("§a§oYour shield blocked half the damage!");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply bonus damage
        if (stats.getBonusDamage() > 0) {
            event.setDamage(event.getDamage() + stats.getBonusDamage());

            // Only show the message if cooldown has passed
            long currentTime = System.currentTimeMillis();
            if (!lastDamageMessageTime.containsKey(playerId) ||
                    currentTime - lastDamageMessageTime.get(playerId) > DAMAGE_MESSAGE_COOLDOWN) {
                if (debuggingFlag == 1) {
                    player.sendMessage(ChatColor.DARK_GRAY + "Skill bonus damage: +" + stats.getBonusDamage());
                }
                lastDamageMessageTime.put(playerId, currentTime);
            }
        }

        // Apply damage multiplier
        if (stats.getDamageMultiplier() != 1.0) {
            double newDamage = event.getDamage() * stats.getDamageMultiplier();
            event.setDamage(newDamage);
            if (debuggingFlag == 1 &&
                    (!lastDamageMessageTime.containsKey(playerId) ||
                            System.currentTimeMillis() - lastDamageMessageTime.get(playerId) > DAMAGE_MESSAGE_COOLDOWN)) {
                player.sendMessage(ChatColor.DARK_GRAY + "Damage multiplier: x" + String.format("%.2f", stats.getDamageMultiplier()));
                lastDamageMessageTime.put(playerId, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            UUID playerId = player.getUniqueId();
            PlayerSkillStats stats = getPlayerStats(player);

            // Apply gold per kill bonus - once
            if (stats.getGoldPerKill() > 0) {
                plugin.moneyRewardHandler.depositMoney(player, stats.getGoldPerKill());

                // Only show the message if cooldown has passed
                long currentTime = System.currentTimeMillis();
                if (!lastDamageMessageTime.containsKey(playerId) ||
                        currentTime - lastDamageMessageTime.get(playerId) > DAMAGE_MESSAGE_COOLDOWN) {
                    player.sendMessage("§6+" + stats.getGoldPerKill() + "$ from trophy hunter skill!");
                    lastDamageMessageTime.put(playerId, currentTime);
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DEBUG: Mob killed: " + event.getEntity().getType().name() +
                            ", Gold reward: " + stats.getGoldPerKill());
                }
            }
        }
    }

    // Complete rewrite of calculatePlayerStats to ensure nothing is duplicated
    public void calculatePlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        // Initialize a completely new stats object
        PlayerSkillStats stats = new PlayerSkillStats();

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            playerStatsCache.put(uuid, stats);
            return;
        }

        // Create sets to track IDs we've already processed
        Set<Integer> processedBaseSkills = new HashSet<>();
        Set<Integer> processedAscendancySkills = new HashSet<>();

        // Get all purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);

        // First process base class skills
        if ("Ranger".equalsIgnoreCase(playerClass)) {
            // Apply Ranger base skills - hardcoded with exact values
            for (int skillId : purchasedSkills) {
                if (skillId >= 1 && skillId <= 14 && !processedBaseSkills.contains(skillId)) {
                    processedBaseSkills.add(skillId);
                    int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);
                    applyRangerSkillEffects(stats, skillId, purchaseCount);
                }
            }
        } else if ("DragonKnight".equalsIgnoreCase(playerClass)) {
            // Apply DragonKnight base skills
            for (int skillId : purchasedSkills) {
                if (skillId >= 1 && skillId <= 14 && !processedBaseSkills.contains(skillId)) {
                    processedBaseSkills.add(skillId);
                    int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);
                    applyDragonKnightSkillEffects(stats, skillId, purchaseCount);
                }
            }
        }

        // Then process ascendancy skills
        if (!ascendancy.isEmpty()) {
            if ("Beastmaster".equalsIgnoreCase(ascendancy)) {
                for (int skillId : purchasedSkills) {
                    if (skillId >= 100000 && skillId < 200000 && !processedAscendancySkills.contains(skillId)) {
                        processedAscendancySkills.add(skillId);
                        int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);
                        applyBeastmasterSkillEffects(stats, skillId, purchaseCount);
                    }
                }
            } else if ("Berserker".equalsIgnoreCase(ascendancy)) {
                for (int skillId : purchasedSkills) {
                    if (skillId >= 200000 && skillId < 300000 && !processedAscendancySkills.contains(skillId)) {
                        processedAscendancySkills.add(skillId);
                        int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);
                        applyBerserkerSkillEffects(stats, skillId, purchaseCount);
                    }
                }
            }
        }

        // Store the final stats
        playerStatsCache.put(uuid, stats);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Calculated stats for player " + player.getName() + ": " +
                    "HP+" + stats.getMaxHealthBonus() + ", " +
                    "DMG+" + stats.getBonusDamage() + ", " +
                    "MULT×" + stats.getDamageMultiplier() + ", " +
                    "Gold/Kill: " + stats.getGoldPerKill());
        }
    }

    /**
     * Explicit implementation for Ranger skills with exact values
     */
    private void applyRangerSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +1% movement speed
                stats.setMovementSpeedBonus(1 * purchaseCount);
                break;
            case 2: // Nature's Recovery - Gain Regeneration I
                // This will be handled separately in a potion effect system
                stats.setHasRegenerationEffect(true); // Add this to PlayerSkillStats
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 2: Set regeneration effect flag");
                }
                break;
            case 3: // +5 damage - FIXED VALUE
                stats.setBonusDamage(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 3: Set bonus damage to EXACTLY 5");
                }
                break;
            case 4: // +2% evade chance
                stats.setEvadeChance(2 * purchaseCount);
                break;
            case 5: // +1 HP
                stats.setMaxHealthBonus(1 * purchaseCount);
                break;
            case 6: // +3$ per killed mob - FIXED VALUE
                stats.setGoldPerKill(3);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 6: Set gold per kill to EXACTLY 3");
                }
                break;
            case 8: // +1% evade chance (1/2)
                stats.addEvadeChance(1 * purchaseCount);
                break;
            case 9: // +1% luck (1/2)
                stats.setLuckBonus(1 * purchaseCount);
                break;
            case 10: // each 3 hits deals +10 dmg - handled by a more complex system
                stats.setHasTripleStrike(true); // Add this to PlayerSkillStats
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 10: Set triple strike flag");
                }
                break;
            case 11: // +1% dmg (1/3)
                stats.setDamageMultiplier(1.0 + (0.01 * purchaseCount));
                break;
            case 13: // +1% def (1/2)
                stats.setDefenseBonus(1 * purchaseCount);
                break;
            case 14: // +4% evade chance, -2% dmg
                stats.addEvadeChance(4 * purchaseCount);
                stats.multiplyDamageMultiplier(1.0 - (0.02 * purchaseCount));
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown Ranger skill ID: " + skillId);
                }
                break;
        }
    }

    private void applyDragonKnightSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +3% def
                stats.setDefenseBonus(3 * purchaseCount);
                break;
            case 3: // +1% dmg
                stats.setDamageMultiplier(1.0 + (0.01 * purchaseCount));
                break;
            case 5: // +1% ms (1/2)
                stats.setMovementSpeedBonus(1 * purchaseCount);
                break;
            case 8: // +2hp (1/2)
                stats.setMaxHealthBonus(2 * purchaseCount);
                break;
            case 9: // +1% luck (1/2)
                stats.setLuckBonus(1 * purchaseCount);
                break;
            case 10: // +7 dmg (1/2) - FIXED VALUE
                stats.setBonusDamage(7);
                break;
            case 11: // +5% dmg, -2% ms
                stats.setDamageMultiplier(1.0 + (0.05 * purchaseCount));
                stats.addMovementSpeedBonus(-2 * purchaseCount);
                break;
            case 13: // +10 dmg - FIXED VALUE
                stats.setBonusDamage(10);
                break;
            case 14: // +5% shield block chance
                stats.setShieldBlockChance(5 * purchaseCount);
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown DragonKnight skill ID: " + skillId);
                }
                break;
        }
    }

    private void applyBeastmasterSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        // Remove the offset to get the original skill number
        int originalId = skillId - 100000;

        // Example implementation - would be expanded for all skills
        switch (originalId) {
            case 9: // Pack Damage: All summons gain +5% damage
                // This would affect companion damage, not player damage
                break;
            case 14: // Pack Damage Plus: All summons gain +10% damage
                // This would affect companion damage, not player damage
                break;
            case 15: // Bear Guardian: When Bears hp<50% you and all summons gain +10% def
                stats.addDefenseBonus(10 * purchaseCount);
                break;
            // ... other Beastmaster skills
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Beastmaster skill " + originalId + " has no implemented effect");
                }
                break;
        }
    }

    private void applyBerserkerSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        // Remove the offset to get the original skill number
        int originalId = skillId - 200000;

        // Example implementation - would be expanded for all skills
        switch (originalId) {
            case 1: // Cannot wear chestplate but gain +200% dmg
                stats.multiplyDamageMultiplier(1.0 + (2.0 * purchaseCount));
                break;
            case 2: // Each 10% hp lost gives +10% dmg
                // This is a dynamic effect and would be handled in combat
                break;
            // ... other Berserker skills
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Berserker skill " + originalId + " has no implemented effect");
                }
                break;
        }
    }

    public PlayerSkillStats getPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerStatsCache.containsKey(uuid)) {
            calculatePlayerStats(player);
        }
        return playerStatsCache.get(uuid);
    }

    // Method to recalculate stats when skills change
    public void refreshPlayerStats(Player player) {
        recalculateAllStats(player);
    }

    // Inner class to store player skill-based stats
    public static class PlayerSkillStats {
        private double bonusDamage = 0;
        private double damageMultiplier = 1.0;
        private double evadeChance = 0;
        private double shieldBlockChance = 0;
        private double defenseBonus = 0;
        private double maxHealthBonus = 0;
        private double movementSpeedBonus = 0;
        private double luckBonus = 0;
        private double goldPerKill = 0;
        private boolean hasRegenerationEffect = false;
        private boolean hasTripleStrike = false;
        // Getters
        public double getBonusDamage() {
            return bonusDamage;
        }

        public double getDamageMultiplier() {
            return damageMultiplier;
        }

        public double getEvadeChance() {
            return evadeChance;
        }

        public double getShieldBlockChance() {
            return shieldBlockChance;
        }

        public double getDefenseBonus() {
            return defenseBonus;
        }

        public double getMaxHealthBonus() {
            return maxHealthBonus;
        }

        public double getMovementSpeedBonus() {
            return movementSpeedBonus;
        }

        public double getLuckBonus() {
            return luckBonus;
        }

        public double getGoldPerKill() {
            return goldPerKill;
        }

        // Setters - use these instead of adders for most properties
        public void setBonusDamage(double amount) {
            this.bonusDamage = amount;
        }

        public void setDamageMultiplier(double amount) {
            this.damageMultiplier = amount;
        }

        public void multiplyDamageMultiplier(double factor) {
            this.damageMultiplier *= factor;
        }

        public void setEvadeChance(double amount) {
            this.evadeChance = amount;
        }

        public void setShieldBlockChance(double amount) {
            this.shieldBlockChance = amount;
        }

        public void setDefenseBonus(double amount) {
            this.defenseBonus = amount;
        }

        public void setMaxHealthBonus(double amount) {
            this.maxHealthBonus = amount;
        }

        public void setMovementSpeedBonus(double amount) {
            this.movementSpeedBonus = amount;
        }

        public void setLuckBonus(double amount) {
            this.luckBonus = amount;
        }

        public void setGoldPerKill(double amount) {
            this.goldPerKill = amount;
        }

        // Adders - use these for incremental changes
        public void addBonusDamage(double amount) {
            this.bonusDamage += amount;
        }

        public void addDamageMultiplier(double amount) {
            this.damageMultiplier += amount;
        }

        public void addEvadeChance(double amount) {
            this.evadeChance += amount;
        }

        public void addShieldBlockChance(double amount) {
            this.shieldBlockChance += amount;
        }

        public void addDefenseBonus(double amount) {
            this.defenseBonus += amount;
        }

        public void addMaxHealth(double amount) {
            this.maxHealthBonus += amount;
        }

        public void addMovementSpeedBonus(double amount) {
            this.movementSpeedBonus += amount;
        }

        public void addLuckBonus(double amount) {
            this.luckBonus += amount;
        }

        public void addGoldPerKill(double amount) {
            this.goldPerKill += amount;
        }
        public boolean hasRegenerationEffect() {
            return hasRegenerationEffect;
        }

        public void setHasRegenerationEffect(boolean hasRegenerationEffect) {
            this.hasRegenerationEffect = hasRegenerationEffect;
        }

        public boolean hasTripleStrike() {
            return hasTripleStrike;
        }

        public void setHasTripleStrike(boolean hasTripleStrike) {
            this.hasTripleStrike = hasTripleStrike;
        }
    }

    public void applyRegenerationEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerSkillStats stats = getPlayerStats(player);

            if (stats.hasRegenerationEffect()) {
                // Apply Regeneration I effect (Duration: 3 seconds, amplifier 0 = level 1)
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, true, false));

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied Regeneration I to " + player.getName() + " from Nature's Recovery skill");
                }
            }
        }
    }

    /**
     * Handles the Triple Strike effect for Ranger skill 10
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onTripleStrikeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        UUID targetId = event.getEntity().getUniqueId();
        PlayerSkillStats stats = getPlayerStats(player);

        // Only process if player has the Triple Strike skill
        if (!stats.hasTripleStrike()) {
            return;
        }

        // Check if target changed
        if (lastTargetMap.containsKey(playerId) && !lastTargetMap.get(playerId).equals(targetId)) {
            // Reset counter if target changed
            hitCounters.put(playerId, 1);
            lastTargetMap.put(playerId, targetId);
            return;
        }

        // Initialize or update hit counter
        int hitCount = hitCounters.getOrDefault(playerId, 0) + 1;
        hitCounters.put(playerId, hitCount);
        lastTargetMap.put(playerId, targetId);

        // Every third hit deals extra damage
        if (hitCount >= 3) {
            // Add 10 damage on third hit
            event.setDamage(event.getDamage() + 10.0);

            // Reset counter
            hitCounters.put(playerId, 0);

            // Notify player


            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.GREEN + "Triple Strike! +10 damage dealt!");
                plugin.getLogger().info("Triple Strike activated for " + player.getName() +
                        ", adding 10 extra damage. Total damage: " + event.getDamage());
            }
        }
    }

    // Add this method to initialize periodic tasks for skill effects
    public void initializePeriodicTasks() {
        // Apply regeneration effect every 3 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, this::applyRegenerationEffects, 20L, 60L);
    }

}