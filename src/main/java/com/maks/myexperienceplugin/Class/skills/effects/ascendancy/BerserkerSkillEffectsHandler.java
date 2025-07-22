package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for Berserker-specific skill effects
 */
public class BerserkerSkillEffectsHandler extends BaseSkillEffectsHandler {
    // IDs start from 200000 for Berserker
    private static final int ID_OFFSET = 200000;

    // Constants
    private static final double RAGE_DAMAGE_MULTIPLIER = 3.0; // 200% increase = 3x damage
    private static final long KILL_FRENZY_DURATION = 30000; // 30 seconds
    private static final long BATTLE_RAGE_DURATION = 5000; // 5 seconds
    private static final long COMBAT_MOMENTUM_CHECK_INTERVAL = 30000; // 30 seconds
    private static final long DEATH_DEFIANCE_COOLDOWN = 120000; // 2 minutes

    // Track player stats
    private final Map<UUID, Double> playerDamageModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerHealthModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerDefenseModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerArmorModifier = new ConcurrentHashMap<>();

    // Track combat state and durations
    private final Map<UUID, Long> lastCombatTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMomentumBonus = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> momentumStacks = new ConcurrentHashMap<>();

    // Kill frenzy stacks
    private final Map<UUID, Integer> killFrenzyStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> killFrenzyExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> killFrenzyTasks = new ConcurrentHashMap<>();

    // Battle rage stacks
    private final Map<UUID, Integer> battleRageStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> battleRageExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> battleRageTasks = new ConcurrentHashMap<>();

    // Attack speed frenzy stacks
    private final Map<UUID, Integer> attackSpeedStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> attackSpeedExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> attackSpeedTasks = new ConcurrentHashMap<>();

    // Skill-specific cooldowns
    private final Map<UUID, Long> deathDefianceCooldown = new ConcurrentHashMap<>();

    // Hit counters
    private final Map<UUID, Integer> hitCounter = new ConcurrentHashMap<>();

    // Target tracking for consecutive hit bonuses
    private final Map<UUID, UUID> lastTargetMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> consecutiveHitCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Integer>> targetDamageStacks = new ConcurrentHashMap<>();

    // Mob kill counters for persistent stacks
    private final Map<UUID, Integer> permanentKillStacks = new ConcurrentHashMap<>();

    // Random for critical hit calculations
    private final Random random = new Random();
    
    /**
     * Roll a chance with debug output
     * @param chance Chance of success (0-100)
     * @param player Player to send debug message to
     * @param mechanicName Name of the mechanic being rolled
     * @return Whether the roll was successful
     */
    private boolean rollChance(double chance, Player player, String mechanicName) {
        if (debuggingFlag == 1) {
            return DebugUtils.rollChanceWithDebug(player, mechanicName, chance);
        } else {
            return Math.random() * 100 < chance;
        }
    }

    public BerserkerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // U cannot wear chestplate but u gain +200% dmg
                if (canApplyUnrestrictedRage(stats)) {
                    stats.multiplyDamageMultiplier(RAGE_DAMAGE_MULTIPLIER);
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("BERSERKER SKILL 1: Applied 200% damage multiplier (3x total) for no chestplate");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 1: +200% damage (no chestplate)");
                    }
                }
                break;
            case 2: // Each 10% hp u lose u gain +10% dmg
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 2: Will apply health-based damage bonus dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 2: +10% damage per 10% HP lost enabled");
                }
                break;
            case 3: // While ur in combat ( 10s after attack ) every 30s u gain +5% dmg
                // This is handled via periodic task
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 3: Will apply combat momentum buff periodically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 3: +5% damage every 30s in combat enabled");
                }
                break;
            case 4: // For every killed mob u gain +1% ms and +1% dmg for 30s ( max 10 stacks )
                // This is handled via kill tracking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 4: Will apply kill frenzy dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 4: +1% damage/speed per kill (max 10 stacks) enabled");
                }
                break;
            case 5: // For every hit u gain +1% dmg for 5s ( max 5 stacks )
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 5: Will apply battle rage dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 5: +1% damage per hit (max 5 stacks) enabled");
                }
                break;
            case 6: // +5% dmg
                stats.addDamageMultiplier(0.05 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 6: Added " + (0.05 * purchaseCount) + " to damage multiplier");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 6: +5% damage");
                }
                break;
            case 7: // -10% hp, +10% dmg (1/2)
                stats.addMaxHealth(-10 * purchaseCount);
                stats.addDamageMultiplier(0.10 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 7: Added -10% HP, +10% damage multiplier (level " + purchaseCount + "/2)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 7: -10% HP, +10% damage (level " + purchaseCount + "/2)");
                }
                break;
            case 8: // U gain -5 armor and +10% crit
                stats.addShieldBlockChance(-5); // Using shield block as a proxy for armor
                stats.addCriticalChance(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 8: Added -5 armor, +10% critical chance");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BERSERKER SKILL 8: -5 armor, +10% critical chance");
                }
                break;
            case 9: // Ur crit deals +15% more dmg
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 9: Will apply critical damage bonus dynamically");
                }
                break;
            case 10: // For every killed mob u gain +1% as for 30s ( max 10 stacks )
                // This is handled via kill tracking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 10: Will apply attack speed frenzy dynamically");
                }
                break;
            case 11: // Ur finishing off mobs that are <5% hp
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 11: Will apply finishing blow dynamically");
                }
                break;
            case 12: // +5 armor, +5% def
                stats.addShieldBlockChance(5); // Using shield block as proxy for armor
                stats.addDefenseBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 12: Added +5 armor, +5% defense");
                }
                break;
            case 13: // U gain +20 hp
                stats.addMaxHealth(20);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 13: Added +20 HP");
                }
                break;
            case 14: // Ur crit now have a 20% chance to cause bleeding that deals 25% ur base dmg per s for 5s
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 14: Will apply bleeding effect dynamically");
                }
                break;
            case 15: // +5% ms
                stats.addMovementSpeedBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 15: Added +5% movement speed");
                }
                break;
            case 16: // Ur crit deals +5% more dmg (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 16: Will add +" + (5 * purchaseCount) + "% to critical damage dynamically");
                }
                break;
            case 17: // +50 dmg
                stats.addBonusDamage(50);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 17: Added +50 flat damage");
                }
                break;
            case 18: // U gain +10% crit chance
                stats.addCriticalChance(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 18: Added +10% critical chance");
                }
                break;
            case 19: // -5% def, +5% dmg
                stats.addDefenseBonus(-5);
                stats.addDamageMultiplier(0.05);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 19: Added -5% defense, +5% damage");
                }
                break;
            case 20: // Ur every 10th hit is crit
                // This is handled via hit counter
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 20: Will track hit count for guaranteed crits");
                }
                break;
            case 21: // +10 armor
                stats.addShieldBlockChance(10); // Using shield block as proxy for armor
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 21: Added +10 armor");
                }
                break;
            case 22: // For every 25 killed mob's u gain +30% dmg ( max 2 stacks )
                // This is handled via persistent kill counter
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 22: Will track permanent kill stacks");
                }
                break;
            case 23: // When hp<50% u gain +25% dmg and +15% crit
                // This is handled dynamically based on health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 23: Will apply low health damage boost dynamically");
                }
                break;
            case 24: // Every hit on the target deals +5% dmg ( resets on target change, max 5 stacks )
                // This is handled via target tracking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 24: Will track target-specific damage stacks");
                }
                break;
            case 25: // U cannot wear boots but u gain +50% dmg and +50% ms
                // This is handled dynamically based on equipment
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 25: Will check for boots and apply bonuses accordingly");
                }
                break;
            case 26: // After killing mob u have 1% chance to gain his head for 10min that gives u +10% dmg
                // This is handled via death event
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 26: Will apply trophy head buffs on kills");
                }
                break;
            case 27: // Once your hp<10% u get fully heald, gain +200% dmg and -50% def for 10s
                // This is handled via damage event
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BERSERKER SKILL 27: Will handle Death Defiance on low health");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown Berserker skill ID: " + skillId);
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Get current health percentage
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double healthPercent = (currentHealth / maxHealth) * 100;

        // Check for Death Defiance (ID 27)
        if (isPurchased(playerId, ID_OFFSET + 27) && healthPercent <= 10 &&
                !isOnCooldown(playerId, deathDefianceCooldown, DEATH_DEFIANCE_COOLDOWN)) {

            // Don't trigger if the damage would kill the player
            if (currentHealth - event.getFinalDamage() > 0) {
                return;
            }

            // Cancel the damage event
            event.setCancelled(true);

            // Heal to full
            player.setHealth(maxHealth);

            // Apply damage and defense modifiers
            applyDeathDefianceBuffs(player);
            
            // Play visual effects
            com.maks.myexperienceplugin.Class.skills.effects.BerserkerVisualEffects.playDeathDefianceEffect(player, plugin);

            // Show notification for this major cooldown ability
            ActionBarUtils.sendActionBar(player,
                    ChatColor.RED + "DEATH DEFIANCE ACTIVATED!");

            // Set cooldown
            deathDefianceCooldown.put(playerId, System.currentTimeMillis());

            // Schedule buff removal
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeDeathDefianceBuffs(player);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Death Defiance buffs expired for " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Death Defiance expired");
                }
            }, 200); // 10 seconds = 200 ticks

            // Schedule notification when cooldown ends
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Death Defiance ready!");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Death Defiance cooldown ended for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Death Defiance cooldown ended");
                    }
                }
            }, DEATH_DEFIANCE_COOLDOWN / 50); // Convert ms to ticks

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Death Defiance activated for " + player.getName() +
                        " at " + healthPercent + "% health");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Death Defiance activated!");
            }

            return;
        }

        // Handle health-based damage bonus (ID 2)
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            // For every 10% health lost, gain 10% damage
            int healthLost = (int) ((100 - healthPercent) / 10);
            double damageBonus = healthLost * 0.10;

            // Update player's damage multiplier
            SkillEffectsHandler.PlayerSkillStats playerStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            playerStats.setDamageMultiplier(1.0 + damageBonus);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied health-based damage bonus for " + player.getName() +
                        ": " + (damageBonus * 100) + "% bonus at " + healthPercent + "% health");
            }
        }

        // Check for low health damage boost (ID 23)
        if (isPurchased(playerId, ID_OFFSET + 23) && healthPercent < 50) {
            // Apply +25% damage and +15% crit chance
            SkillEffectsHandler.PlayerSkillStats playerStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            playerStats.addDamageMultiplier(0.25);
            playerStats.addCriticalChance(15);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied low health damage boost for " + player.getName() +
                        ": +25% damage, +15% crit chance at " + healthPercent + "% health");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        UUID targetId = event.getEntity().getUniqueId();

        // If this is a critical hit
        boolean isCrit = false;
        double critChance = getCriticalChance(player, stats);

        // Update hit counter for guaranteed crits (ID 20)
        int hits = hitCounter.getOrDefault(playerId, 0) + 1;
        hitCounter.put(playerId, hits);

        // Every 10th hit is a crit (ID 20)
        if (isPurchased(playerId, ID_OFFSET + 20) && hits % 10 == 0) {
            isCrit = true;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Guaranteed critical hit for " + player.getName() +
                        " (every 10th hit: " + hits + ")");
            }
        }
        // Random crit chance check
        else {
            boolean success = rollChance(critChance, player, "Critical Hit");

            if (success) {
                isCrit = true;
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Critical hit landed!");
                }
            }
        }

        // Handle critical hit
        if (isCrit) {
            // Apply base crit (2x damage)
            double originalDamage = event.getDamage();
            double critDamage = originalDamage * 2;

            // Apply critical damage bonuses (ID 9, ID 16)
            if (isPurchased(playerId, ID_OFFSET + 9)) {
                critDamage *= 1.15; // +15% more critical damage
            }

            if (isPurchased(playerId, ID_OFFSET + 16)) {
                // Count purchases of ID 16
                int purchases = getPurchaseCount(playerId, ID_OFFSET + 16);
                critDamage *= (1 + (0.05 * purchases)); // +5% per level
            }

            // Apply the critical damage
            event.setDamage(critDamage);

            // Show notification for critical hits (important combat feedback)
            ActionBarUtils.sendActionBar(player,
                    ChatColor.RED + "Critical Hit! " + String.format("%.0f", critDamage) + " dmg");

            // Apply bleeding effect (ID 14)
            if (isPurchased(playerId, ID_OFFSET + 14) && event.getEntity() instanceof LivingEntity) {
                boolean success = rollChance(20.0, player, "Bleeding");

                if (success) {

                LivingEntity target = (LivingEntity) event.getEntity();
                applyBleedingEffect(player, target, originalDamage);

                // Show notification for important debuff application
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Bleeding Applied!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Applied bleeding (25% base damage per second for 5s)");
                    plugin.getLogger().info("Applied bleeding to " + target.getType() +
                            " from critical hit by " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bleeding applied to target");
                }
            }
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Critical hit by " + player.getName() +
                        ": " + originalDamage + " → " + critDamage + " damage");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Critical hit! " +
                        String.format("%.1f", originalDamage) + " → " + String.format("%.1f", critDamage) + " dmg");
            }
        }

        // Handle Battle Rage (ID 5)
        if (isPurchased(playerId, ID_OFFSET + 5)) {
            // Update stack count
            int currentStacks = battleRageStacks.getOrDefault(playerId, 0);
            if (currentStacks < 5) { // Max 5 stacks
                currentStacks++;
                battleRageStacks.put(playerId, currentStacks);
            }

            // Apply damage bonus
            double damageBonus = currentStacks * 0.01; // 1% per stack
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * (1 + damageBonus);
            event.setDamage(bonusDamage);

            // Update expiry time
            battleRageExpiry.put(playerId, System.currentTimeMillis() + BATTLE_RAGE_DURATION);

            // Cancel existing task
            BukkitTask existingTask = battleRageTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule stack removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if stacks are still active
                Long expiry = battleRageExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    battleRageStacks.remove(playerId);
                    battleRageExpiry.remove(playerId);
                    battleRageTasks.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Battle Rage stacks expired for " + player.getName());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Rage stacks expired");
                        }
                    }
                }
            }, BATTLE_RAGE_DURATION / 50); // Convert ms to ticks

            battleRageTasks.put(playerId, task);

            // Only notify when reaching max stacks
            if (currentStacks == 5) {
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Battle Rage MAX! (5 stacks)");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Battle Rage max stacks reached for " + player.getName() +
                            " (+5% damage)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Rage: 5/5 stacks (+5% dmg)");
                }
            } else if (debuggingFlag == 1) {
                plugin.getLogger().info("Battle Rage stack added for " + player.getName() +
                        ": " + currentStacks + "/5 stacks");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Rage: " +
                        currentStacks + "/5 stacks (+" + currentStacks + "% dmg)");
            }
        }

        // Handle target-specific damage stacks (ID 24)
        if (isPurchased(playerId, ID_OFFSET + 24)) {
            // Get current target
            UUID currentTarget = targetId;
            UUID lastTarget = lastTargetMap.get(playerId);

            // If target changed, reset stacks
            if (lastTarget != null && !lastTarget.equals(currentTarget)) {
                Map<UUID, Integer> targetStacks = targetDamageStacks.get(playerId);
                if (targetStacks != null) {
                    targetStacks.remove(lastTarget);
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Target changed for " + player.getName() +
                            ", resetting target damage stacks");
                }
            }

            // Update last target
            lastTargetMap.put(playerId, currentTarget);

            // Get target stacks map
            Map<UUID, Integer> targetStacks = targetDamageStacks.computeIfAbsent(
                    playerId, k -> new HashMap<>());

            // Update stack count
            int currentStacks = targetStacks.getOrDefault(currentTarget, 0);
            if (currentStacks < 5) { // Max 5 stacks
                currentStacks++;
                targetStacks.put(currentTarget, currentStacks);
            }

            // Apply damage bonus
            double damageBonus = currentStacks * 0.05; // 5% per stack
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * (1 + damageBonus);
            event.setDamage(bonusDamage);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Target damage stack applied for " + player.getName() +
                        ": " + currentStacks + "/5 stacks, damage " + originalDamage +
                        " → " + bonusDamage);
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Target stacks: " +
                        currentStacks + "/5 (+" + (currentStacks * 5) + "% dmg)");
            }
        }

        // Handle finishing blow (ID 11)
        if (isPurchased(playerId, ID_OFFSET + 11) && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            double targetMaxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double targetHealth = target.getHealth();
            double healthPercent = (targetHealth / targetMaxHealth) * 100;

            if (healthPercent <= 5) {
                // Instantly kill the target
                target.setHealth(0);

                // Show notification for this powerful effect
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Finishing Blow!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Finishing Blow executed by " + player.getName() +
                            " on " + target.getType() + " at " + healthPercent + "% health");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Finishing Blow executed");
                }
            }
        }

        // Update last combat time for Combat Momentum (ID 3)
        if (isPurchased(playerId, ID_OFFSET + 3)) {
            lastCombatTime.put(playerId, System.currentTimeMillis());

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Updated last combat time for " + player.getName());
            }
        }

        // Check if player has no chestplate for Unarmored Rage (ID 1)
        if (isPurchased(playerId, ID_OFFSET + 1)) {
            checkAndApplyUnrestrictedRage(player);
        }

        // Check if player has no boots for Lightfoot Rage (ID 25)
        if (isPurchased(playerId, ID_OFFSET + 25)) {
            checkAndApplyLightfootRage(player);
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Handle Kill Frenzy (ID 4)
        if (isPurchased(playerId, ID_OFFSET + 4)) {
            // Update stack count
            int currentStacks = killFrenzyStacks.getOrDefault(playerId, 0);
            if (currentStacks < 10) { // Max 10 stacks
                currentStacks++;
                killFrenzyStacks.put(playerId, currentStacks);

                // Apply bonuses
                applyKillFrenzyBonuses(player, currentStacks);

                // Show notification at max stacks
                if (currentStacks == 10) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Kill Frenzy MAX! (10 stacks)");
                }
            }

            // Update expiry time
            killFrenzyExpiry.put(playerId, System.currentTimeMillis() + KILL_FRENZY_DURATION);

            // Cancel existing task
            BukkitTask existingTask = killFrenzyTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule stack removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if stacks are still active
                Long expiry = killFrenzyExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    int oldStacks = killFrenzyStacks.getOrDefault(playerId, 0);
                    killFrenzyStacks.remove(playerId);
                    killFrenzyExpiry.remove(playerId);
                    killFrenzyTasks.remove(playerId);

                    // Remove bonuses
                    removeKillFrenzyBonuses(player, oldStacks);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Kill Frenzy stacks expired for " + player.getName());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Kill Frenzy stacks expired");
                        }
                    }
                }
            }, KILL_FRENZY_DURATION / 50); // Convert ms to ticks

            killFrenzyTasks.put(playerId, task);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Kill Frenzy stack added for " + player.getName() +
                        ": " + currentStacks + "/10 stacks");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Kill Frenzy: " +
                        currentStacks + "/10 stacks (+" + currentStacks + "% dmg/speed)");
            }
        }

        // Handle Attack Speed Frenzy (ID 10)
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            // Update stack count
            int currentStacks = attackSpeedStacks.getOrDefault(playerId, 0);
            if (currentStacks < 10) { // Max 10 stacks
                currentStacks++;
                attackSpeedStacks.put(playerId, currentStacks);

                // Apply bonuses
                applyAttackSpeedBonuses(player, currentStacks);

                // Show notification at max stacks
                if (currentStacks == 10) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Attack Speed Frenzy MAX!");
                }
            }

            // Update expiry time
            attackSpeedExpiry.put(playerId, System.currentTimeMillis() + KILL_FRENZY_DURATION);

            // Cancel existing task
            BukkitTask existingTask = attackSpeedTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule stack removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if stacks are still active
                Long expiry = attackSpeedExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    int oldStacks = attackSpeedStacks.getOrDefault(playerId, 0);
                    attackSpeedStacks.remove(playerId);
                    attackSpeedExpiry.remove(playerId);
                    attackSpeedTasks.remove(playerId);

                    // Remove bonuses
                    removeAttackSpeedBonuses(player, oldStacks);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Attack Speed Frenzy stacks expired for " + player.getName());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Attack Speed Frenzy stacks expired");
                        }
                    }
                }
            }, KILL_FRENZY_DURATION / 50); // Convert ms to ticks

            attackSpeedTasks.put(playerId, task);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Attack Speed Frenzy stack added for " + player.getName() +
                        ": " + currentStacks + "/10 stacks");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Attack Speed Frenzy: " +
                        currentStacks + "/10 stacks (+" + currentStacks + "% speed)");
            }
        }

        // Handle persistent kill stacks (ID 22)
        if (isPurchased(playerId, ID_OFFSET + 22)) {
            // Update kill count
            int killCount = permanentKillStacks.getOrDefault(playerId, 0) + 1;
            permanentKillStacks.put(playerId, killCount);

            // Check if we reached a 25-kill threshold
            if (killCount % 25 == 0) {
                // Calculate stack count (max 2)
                int stackCount = Math.min(2, killCount / 25);

                // Apply damage bonus
                SkillEffectsHandler.PlayerSkillStats playerStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                playerStats.addDamageMultiplier(0.30 * stackCount);

                // Show notification for this powerful effect
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Bloodthirst " + stackCount + " Activated!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Bloodthirst stack " + stackCount + " activated for " +
                            player.getName() + " after " + killCount + " kills");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bloodthirst " + stackCount +
                            " activated (+" + (30 * stackCount) + "% dmg)");
                }
            }
        }

        // Handle trophy head buffs (ID 26)
        if (isPurchased(playerId, ID_OFFSET + 26)) {
            boolean success = rollChance(1.0, player, "Trophy Head");

            if (success) {
                // Apply temporary damage buff
                SkillEffectsHandler.PlayerSkillStats playerStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                playerStats.addDamageMultiplier(0.10);

                // Show notification for this rare drop
                ActionBarUtils.sendActionBar(player,
                        ChatColor.GOLD + "Trophy Head Collected! +10% DMG");

                // Schedule buff removal after 10 minutes
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        // Remove the damage buff
                        SkillEffectsHandler.PlayerSkillStats currentStats =
                                plugin.getSkillEffectsHandler().getPlayerStats(player);
                        currentStats.addDamageMultiplier(-0.10);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Trophy head buff expired for " + player.getName());
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Trophy head buff expired");
                        }
                    }
                }, 12000); // 10 minutes = 12000 ticks

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Collected trophy head (+10% damage for 10 minutes)");
                    plugin.getLogger().info("Trophy head collected by " + player.getName() +
                            " from " + event.getEntity().getType());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Trophy head collected! +10% damage for 10min");
                }
            }
            }
        }

    /**
     * Apply Combat Momentum bonuses
     * This should be called periodically while player is in combat
     */
    public void checkCombatMomentum() {
        for (UUID playerId : lastCombatTime.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;

            long lastCombat = lastCombatTime.getOrDefault(playerId, 0L);
            long currentTime = System.currentTimeMillis();

            // Check if player is in combat (within 10 seconds of last attack)
            if (currentTime - lastCombat <= 10000) {
                // Check if it's been at least 30 seconds since last momentum bonus
                long lastBonus = lastMomentumBonus.getOrDefault(playerId, 0L);

                if (currentTime - lastBonus >= COMBAT_MOMENTUM_CHECK_INTERVAL) {
                    // Apply damage bonus
                    SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    stats.addDamageMultiplier(0.05);

                    // Update stack count
                    int stacks = momentumStacks.getOrDefault(playerId, 0) + 1;
                    momentumStacks.put(playerId, stacks);

                    // Update last bonus time
                    lastMomentumBonus.put(playerId, currentTime);

                    // Show notification for this significant buff
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Combat Momentum " + stacks + "! +" + (5 * stacks) + "% DMG");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Combat Momentum stack " + stacks + " applied to " +
                                player.getName() + " (+5% damage)");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Combat Momentum " + stacks +
                                " activated (+" + (5 * stacks) + "% total dmg)");
                    }
                }
            }
            // If not in combat, reset momentum stacks
            else if (momentumStacks.containsKey(playerId)) {
                int oldStacks = momentumStacks.get(playerId);

                // Remove damage bonus
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addDamageMultiplier(-0.05 * oldStacks);

                // Clear stacks
                momentumStacks.remove(playerId);
                lastMomentumBonus.remove(playerId);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Combat Momentum stacks lost for " + player.getName() +
                            " (no longer in combat)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Combat Momentum expired");
                }
            }
        }
    }

    /**
     * Apply bleeding effect to a target
     */
    private void applyBleedingEffect(Player player, LivingEntity target, double baseDamage) {
        final double bleedDamage = baseDamage * 0.25; // 25% of base damage per second
        final int durationTicks = 5 * 20; // 5 seconds
        final int tickInterval = 20; // Once per second

        // Store the task ID to cancel if needed
        final int[] taskId = {-1};

        // Schedule bleeding ticks
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int ticksRemaining = durationTicks;

            @Override
            public void run() {
                // Check if target is still valid
                if (target == null || target.isDead() || !target.isValid()) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }

                // Apply bleed damage
                // Use EntityDamageEvent.DamageCause.CUSTOM to bypass armor reduction
                target.damage(bleedDamage, player);

                // Visual effect for bleeding
                target.getWorld().spawnParticle(
                        org.bukkit.Particle.BLOCK_CRACK,
                        target.getLocation().add(0, 1, 0),
                        10, 0.5, 0.5, 0.5, 0.1,
                        Material.REDSTONE_BLOCK.createBlockData());

                // Decrement ticks
                ticksRemaining -= tickInterval;

                // Check if done
                if (ticksRemaining <= 0) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);

                    if (debuggingFlag == 1 && player.isOnline()) {
                        plugin.getLogger().info("Bleeding effect expired on " + target.getType());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bleeding effect expired");
                    }
                }
            }
        }, tickInterval, tickInterval);
    }

    /**
     * Apply bonuses from the Kill Frenzy skill
     */
    private void applyKillFrenzyBonuses(Player player, int stacks) {
        // Apply damage multiplier
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(0.01 * stacks); // 1% per stack

        // Apply movement speed bonus
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Remove existing modifiers
            for (AttributeModifier mod : speedAttr.getModifiers()) {
                if (mod.getName().equals("skill.killFrenzy")) {
                    speedAttr.removeModifier(mod);
                }
            }

            // Add new modifier
            AttributeModifier speedMod = new AttributeModifier(
                    UUID.randomUUID(),
                    "skill.killFrenzy",
                    0.01 * stacks, // 1% per stack
                    AttributeModifier.Operation.ADD_SCALAR
            );
            speedAttr.addModifier(speedMod);
        }
    }

    /**
     * Remove bonuses from the Kill Frenzy skill
     */
    private void removeKillFrenzyBonuses(Player player, int stacks) {
        // Remove damage multiplier
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(-0.01 * stacks); // Remove 1% per stack

        // Remove movement speed bonus
        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            // Remove existing modifiers
            speedAttr.getModifiers().stream()
                    .filter(mod -> mod.getName().equals("skill.killFrenzy"))
                    .forEach(speedAttr::removeModifier);
        }
    }

    /**
     * Apply bonuses from the Attack Speed Frenzy skill
     */
    private void applyAttackSpeedBonuses(Player player, int stacks) {
        // Apply attack speed bonus
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            // Remove existing modifiers
            for (AttributeModifier mod : attackSpeedAttr.getModifiers()) {
                if (mod.getName().equals("skill.attackSpeedFrenzy")) {
                    attackSpeedAttr.removeModifier(mod);
                }
            }

            // Add new modifier
            AttributeModifier speedMod = new AttributeModifier(
                    UUID.randomUUID(),
                    "skill.attackSpeedFrenzy",
                    0.01 * stacks, // 1% per stack
                    AttributeModifier.Operation.ADD_SCALAR
            );
            attackSpeedAttr.addModifier(speedMod);
        }
    }

    /**
     * Remove bonuses from the Attack Speed Frenzy skill
     */
    private void removeAttackSpeedBonuses(Player player, int stacks) {
        // Remove attack speed bonus
        AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            // Remove existing modifiers
            attackSpeedAttr.getModifiers().stream()
                    .filter(mod -> mod.getName().equals("skill.attackSpeedFrenzy"))
                    .forEach(attackSpeedAttr::removeModifier);
        }
    }

    /**
     * Apply buffs for Death Defiance skill
     */
    private void applyDeathDefianceBuffs(Player player) {
        // Add +200% damage
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(2.0);

        // Add -50% defense
        stats.addDefenseBonus(-50);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied Death Defiance buffs to " + player.getName() +
                    ": +200% damage, -50% defense for 10s");
        }
    }

    /**
     * Remove buffs from Death Defiance skill
     */
    private void removeDeathDefianceBuffs(Player player) {
        // Remove damage bonus
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(-2.0);

        // Restore defense
        stats.addDefenseBonus(50);
    }

    /**
     * Check if player has no chestplate for Unarmored Rage
     */
    private void checkAndApplyUnrestrictedRage(Player player) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment != null && equipment.getChestplate() == null) {
            // Player has no chestplate, apply the bonus
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            // Apply via custom method to avoid stacking
            if (canApplyUnrestrictedRage(stats)) {
                stats.multiplyDamageMultiplier(RAGE_DAMAGE_MULTIPLIER);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied Unarmored Rage to " + player.getName() +
                            " (no chestplate: +200% damage)");
                }
            }
        } else {
            // Player has a chestplate, remove the bonus if it was applied
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            // Reset multiplier to base value
            stats.setDamageMultiplier(1.0);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Removed Unarmored Rage from " + player.getName() +
                        " (wearing chestplate)");
            }
        }
    }

    /**
     * Check if player has no boots for Lightfoot Rage
     */
    private void checkAndApplyLightfootRage(Player player) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment != null && equipment.getBoots() == null) {
            // Player has no boots, apply the bonuses
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            stats.addDamageMultiplier(0.5); // +50% damage
            stats.addMovementSpeedBonus(50); // +50% movement speed

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied Lightfoot Rage to " + player.getName() +
                        " (no boots: +50% damage, +50% speed)");
            }
        } else {
            // Player has boots, remove the bonuses
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            stats.addDamageMultiplier(-0.5);
            stats.addMovementSpeedBonus(-50);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Removed Lightfoot Rage from " + player.getName() +
                        " (wearing boots)");
            }
        }
    }

    /**
     * Calculate current critical chance for player
     */
    private double getCriticalChance(Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        double critChance = 0;

        // Base crit chance from Skills 8 and 18
        if (isPurchased(playerId, ID_OFFSET + 8)) {
            critChance += 10.0; // +10% from skill 8
        }

        if (isPurchased(playerId, ID_OFFSET + 18)) {
            critChance += 10.0; // +10% from skill 18
        }

        // Additional crit chance when health is below 50% (Skill 23)
        if (isPurchased(playerId, ID_OFFSET + 23)) {
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = player.getHealth();
            double healthPercent = (currentHealth / maxHealth) * 100;

            if (healthPercent < 50) {
                critChance += 15.0; // +15% from skill 23
            }
        }

        // Additional crit chance for full health targets (Skill 26)
        if (isPurchased(playerId, ID_OFFSET + 26)) {
            // This would need to check target health but we don't have access to the target here
            // We'll simplify and just add it as a constant
            critChance += 10.0; // Approximate average bonus
        }

        return critChance;
    }

    /**
     * Check if we can apply the Unarmored Rage damage multiplier
     * This helps prevent stacking the buff multiple times
     */
    private boolean canApplyUnrestrictedRage(SkillEffectsHandler.PlayerSkillStats stats) {
        // If multiplier is already 3.0 (or higher), the buff is already applied
        return stats.getDamageMultiplier() < RAGE_DAMAGE_MULTIPLIER;
    }

    /**
     * Check if player has purchased a specific skill
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        Set<Integer> purchased = plugin.getSkillTreeManager().getPurchasedSkills(playerId);
        return purchased.contains(skillId);
    }

    /**
     * Get the number of times a skill has been purchased
     */
    private int getPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }

    /**
     * Check if on cooldown
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownDuration) {
        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }

        long lastUse = cooldownMap.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownDuration;
    }

    /**
     * Clean up all player data
     */
    public void clearPlayerData(UUID playerId) {
        // Clear all maps
        playerDamageModifier.remove(playerId);
        playerHealthModifier.remove(playerId);
        playerDefenseModifier.remove(playerId);
        playerArmorModifier.remove(playerId);

        lastCombatTime.remove(playerId);
        lastMomentumBonus.remove(playerId);
        momentumStacks.remove(playerId);

        killFrenzyStacks.remove(playerId);
        killFrenzyExpiry.remove(playerId);
        BukkitTask killTask = killFrenzyTasks.remove(playerId);
        if (killTask != null) {
            killTask.cancel();
        }

        battleRageStacks.remove(playerId);
        battleRageExpiry.remove(playerId);
        BukkitTask rageTask = battleRageTasks.remove(playerId);
        if (rageTask != null) {
            rageTask.cancel();
        }

        attackSpeedStacks.remove(playerId);
        attackSpeedExpiry.remove(playerId);
        BukkitTask speedTask = attackSpeedTasks.remove(playerId);
        if (speedTask != null) {
            speedTask.cancel();
        }

        deathDefianceCooldown.remove(playerId);

        hitCounter.remove(playerId);

        lastTargetMap.remove(playerId);
        consecutiveHitCounter.remove(playerId);
        targetDamageStacks.remove(playerId);

        permanentKillStacks.remove(playerId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all Berserker data for player ID: " + playerId);
        }
    }
}
