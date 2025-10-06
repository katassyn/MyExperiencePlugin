package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.party.PartyAPI;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for Scale Guardian-specific skill effects
 */
public class ScaleGuardianSkillEffectsHandler extends BaseSkillEffectsHandler {
    // IDs start from 400000 for Scale Guardian (must match ScaleGuardianSkillManager)
    private static final int ID_OFFSET = 400000;

    // Constants
    private static final long LAST_STAND_DURATION = 8000; // 8 seconds
    private static final long LAST_STAND_COOLDOWN = 30000; // 30 seconds
    private static final long WEAKENING_STRIKE_DURATION = 5000; // 5 seconds
    private static final long REACTIVE_DEFENSE_DURATION = 5000; // 5 seconds
    private static final long BLOCK_MOMENTUM_DURATION = 5000; // 5 seconds
    private static final long GUARDIAN_ANGEL_COOLDOWN = 30000; // 30 seconds
    private static final long SLOWING_DEFENSE_DURATION = 3000; // 3 seconds
    private static final long CRITICAL_IMMUNITY_DURATION = 5000; // 5 seconds
    private static final long SHIELD_BASH_COOLDOWN = 10000; // 10 seconds
    private static final long LAST_RESORT_DURATION = 5000; // 5 seconds
    private static final long LAST_RESORT_COOLDOWN = 180000; // 3 minutes
    private static final long WEAKENING_BLOCK_DURATION = 3000; // 3 seconds
    private static final long STATIONARY_CHECK_INTERVAL = 500; // Check every 0.5 seconds

    // Track player stats
    private final Map<UUID, Double> playerDefenseModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerDamageModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerHealingModifier = new ConcurrentHashMap<>();

    // Track cooldowns
    private final Map<UUID, Long> lastStandCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> guardianAngelCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> shieldBashCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastResortCooldown = new ConcurrentHashMap<>();

    // Track buff durations
    private final Map<UUID, Long> lastStandExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> reactiveDefenseExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> blockMomentumExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> criticalImmunityExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastResortExpiry = new ConcurrentHashMap<>();

    // Track buff tasks
    private final Map<UUID, BukkitTask> lastStandTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> reactiveDefenseTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> blockMomentumTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> criticalImmunityTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> lastResortTasks = new ConcurrentHashMap<>();

    // Track block counters
    private final Map<UUID, Integer> blockCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> counterAttackReady = new ConcurrentHashMap<>();

    // Track enemy-specific effects
    private final Map<UUID, Map<UUID, Integer>> weakenedEnemies = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> weakenedEnemiesExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, BukkitTask>> weakenedEnemiesTasks = new ConcurrentHashMap<>();

    // Track stationary players
    private final Map<UUID, Location> lastPlayerLocation = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMovementTime = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> isStationary = new ConcurrentHashMap<>();
    
    // Track Desperate Defense values
    private final Map<UUID, Double> desperateDefenseValues = new ConcurrentHashMap<>();

    // Random for chance-based effects
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

    public ScaleGuardianSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);

        // Start stationary check task
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkStationaryPlayers, 10, STATIONARY_CHECK_INTERVAL / 50);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // +5% shield block chance (1/2)
                stats.addShieldBlockChance(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 1: Applied +" + (5 * purchaseCount) + "% shield block chance");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 1: +" + (5 * purchaseCount) + "% shield block chance");
                }
                break;

            case 2: // +10% damage reflection when blocking (1/2)
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 2: Will apply damage reflection when blocking");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 2: +10% damage reflection when blocking enabled");
                }
                break;

            case 3: // +3% defense for each nearby enemy (max +15%)
                // This is handled dynamically based on nearby entities
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 3: Will apply proximity defense dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 3: +3% defense per nearby enemy (max +15%) enabled");
                }
                break;

            case 4: // Successful blocks restore 5% of maximum health (1/2)
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 4: Will apply healing on successful blocks");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 4: Block heals 5% max HP enabled");
                }
                break;

            case 5: // +20% resistance to knockback effects
                // This is handled dynamically when receiving knockback
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 5: Will apply knockback resistance");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 5: +20% knockback resistance enabled");
                }
                break;

            case 6: // When hp<50%, your defense is increased by 15% (1/2)
                // This is handled dynamically based on health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 6: Will apply defense bonus when health is low");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 6: +15% defense when HP < 50% enabled");
                }
                break;

            case 7: // Being hit by a melee attack has 25% chance to taunt the attacker
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 7: Will apply taunt effect dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 7: 25% chance to taunt attacker enabled");
                }
                break;

            case 8: // +10% defense when not moving for 2 seconds (1/2)
                // This is handled dynamically when stationary
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 8: Will apply stationary defense bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SCALE GUARDIAN SKILL 8: +10% defense when not moving for 2s enabled");
                }
                break;



            case 9: // Blocking an attack reduces damage from that enemy by 5% for 3 seconds (stacks up to 4 times)
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 9: Will apply enemy weakening on blocks");
                }
                break;

            case 10: // When below 30% hp, gain +25% shield block chance for 8 seconds (30 second cooldown)
                // This is handled dynamically based on health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 10: Will apply shield block bonus on low health");
                }
                break;

            case 11: // Your attacks have 15% chance to reduce enemy's damage by 10% for 5 seconds
                // This is handled dynamically when dealing damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 11: Will apply enemy weakening on attacks");
                }
                break;

            case 12: // +3 armor for each piece of heavy armor worn (1/2)
                // This is handled dynamically based on equipment
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 12: Will apply armor bonus based on equipment");
                }
                break;

            case 13: // Blocking a critical hit reflects 40% of the damage back to attacker
                // This is handled dynamically when blocking critical hits
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 13: Will reflect damage on critical blocks");
                }
                break;

            case 14: // Nearby allies (within 8 blocks) gain +10% defense
                // This is handled dynamically based on nearby allies
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 14: Will apply defense aura to nearby allies");
                }
                break;

            case 15: // Taking more than 20% of your max health in a single hit grants +30% defense for 5 seconds
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 15: Will apply reactive defense on heavy hits");
                }
                break;

            case 16: // When surrounded by 3+ enemies, gain +5% healing from all sources per enemy
                // This is handled dynamically based on nearby entities
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 16: Will apply healing bonus when surrounded");
                }
                break;

            case 17: // After blocking, your next attack deals +30% damage (1/2)
                // This is handled dynamically when blocking and attacking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 17: Will apply counter attack bonus");
                }
                break;

            case 18: // Every 3 successful blocks increases your damage by 15% for 5 seconds
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 18: Will apply damage bonus on block streak");
                }
                break;

            case 19: // When an ally within 10 blocks falls below 30% hp, gain +30% movement speed and heal them for 50% hp
                // This is handled dynamically based on nearby allies' health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 19: Will apply guardian angel effect");
                }
                break;

            case 20: // +20% duration of all positive potion effects (1/2)
                // This is handled dynamically when receiving potion effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 20: Will extend potion durations");
                }
                break;

            case 21: // Enemies attacking you have a 10% chance to be slowed by 20% for 3 seconds
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 21: Will apply slowing effect to attackers");
                }
                break;

            case 22: // While above 80% hp, nearby allies take 15% reduced damage
                // This is handled dynamically based on health and nearby allies
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 22: Will apply damage reduction to allies when healthy");
                }
                break;

            case 23: // After taking damage exceeding 15% of your max health, gain immunity to critical hits for 5 seconds
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 23: Will apply critical hit immunity on heavy hits");
                }
                break;

            case 24: // Shield blocks have 25% chance to stun the attacker for 1 second (10 second cooldown)
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 24: Will apply stun effect on blocks");
                }
                break;

            case 25: // Damage taken is reduced by 1% for each 1% of hp you're missing (max 30%)
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 25: Will apply desperate defense damage reduction");
                }
                break;

            case 26: // Allies within 12 blocks gain +10% shield block chance and +5% damage (1/2)
                // This is handled dynamically based on nearby allies
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 26: Will apply ally protection buffs");
                }
                break;

            case 27: // When hp<10%, gain 70% damage reduction for 5 seconds (3 minute cooldown)
                // This is handled dynamically based on health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SCALE GUARDIAN SKILL 27: Will apply last resort damage reduction");
                }
                break;

            default:
                // Many Scale Guardian skills are handled dynamically through events, not here
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Scale Guardian skill " + skillId + " (ID " + originalId + ") is handled dynamically through events");
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
        double damageAmount = event.getFinalDamage();
        double damagePercent = (damageAmount / maxHealth) * 100;

        // Check for Last Stand (ID 10)
        if (isPurchased(playerId, ID_OFFSET + 10) && healthPercent <= 30 &&
                !isOnCooldown(playerId, lastStandCooldown, LAST_STAND_COOLDOWN)) {

            // Apply shield block chance bonus
            stats.addShieldBlockChance(25);

            // Set cooldown
            lastStandCooldown.put(playerId, System.currentTimeMillis());

            // Set expiry
            lastStandExpiry.put(playerId, System.currentTimeMillis() + LAST_STAND_DURATION);

            // Cancel existing task
            BukkitTask existingTask = lastStandTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule buff removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if buff is still active
                Long expiry = lastStandExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    // Remove the buff
                    SkillEffectsHandler.PlayerSkillStats currentStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    currentStats.addShieldBlockChance(-25);

                    lastStandExpiry.remove(playerId);
                    lastStandTasks.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Last Stand expired for " + player.getName());
                        if (player.isOnline()) {
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Last Stand expired");
                        }
                    }
                }
            }, LAST_STAND_DURATION / 50); // Convert ms to ticks

            lastStandTasks.put(playerId, task);

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Last Stand Activated! +25% Shield Block");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Last Stand activated for " + player.getName() + " at " + healthPercent + "% health");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Last Stand activated!");
            }
        }

        // Check for Reactive Defense (ID 15)
        if (isPurchased(playerId, ID_OFFSET + 15) && damagePercent >= 20) {
            // Apply defense bonus
            stats.addDefenseBonus(30);

            // Set expiry
            reactiveDefenseExpiry.put(playerId, System.currentTimeMillis() + REACTIVE_DEFENSE_DURATION);

            // Cancel existing task
            BukkitTask existingTask = reactiveDefenseTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule buff removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if buff is still active
                Long expiry = reactiveDefenseExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    // Remove the buff
                    SkillEffectsHandler.PlayerSkillStats currentStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    currentStats.addDefenseBonus(-30);

                    reactiveDefenseExpiry.remove(playerId);
                    reactiveDefenseTasks.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Reactive Defense expired for " + player.getName());
                        if (player.isOnline()) {
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Reactive Defense expired");
                        }
                    }
                }
            }, REACTIVE_DEFENSE_DURATION / 50); // Convert ms to ticks

            reactiveDefenseTasks.put(playerId, task);

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Reactive Defense! +30% Defense");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Reactive Defense activated for " + player.getName() + " from " + damagePercent + "% damage hit");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Reactive Defense activated!");
            }
        }

        // Check for Critical Immunity (ID 23)
        if (isPurchased(playerId, ID_OFFSET + 23) && damagePercent >= 15) {
            // Set critical immunity flag
            criticalImmunityExpiry.put(playerId, System.currentTimeMillis() + CRITICAL_IMMUNITY_DURATION);

            // Cancel existing task
            BukkitTask existingTask = criticalImmunityTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule immunity removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if immunity is still active
                Long expiry = criticalImmunityExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    criticalImmunityExpiry.remove(playerId);
                    criticalImmunityTasks.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Critical Immunity expired for " + player.getName());
                        if (player.isOnline()) {
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Critical Immunity expired");
                        }
                    }
                }
            }, CRITICAL_IMMUNITY_DURATION / 50); // Convert ms to ticks

            criticalImmunityTasks.put(playerId, task);

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Critical Immunity Activated!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Critical Immunity activated for " + player.getName() + " from " + damagePercent + "% damage hit");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Critical Immunity activated!");
            }
        }

        // Check for Last Resort (ID 27)
        if (isPurchased(playerId, ID_OFFSET + 27) && healthPercent <= 10 &&
                !isOnCooldown(playerId, lastResortCooldown, LAST_RESORT_COOLDOWN)) {

            // Check if Last Resort is already active
            if (lastResortExpiry.containsKey(playerId)) {
                // Already active, just extend the duration
                lastResortExpiry.put(playerId, System.currentTimeMillis() + LAST_RESORT_DURATION);
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Last Resort duration extended for " + player.getName());
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Last Resort duration extended");
                }
            } else {
                // Apply damage reduction (using defense bonus as proxy for damage reduction)
                stats.addDefenseBonus(70);
                
                // Set expiry
                lastResortExpiry.put(playerId, System.currentTimeMillis() + LAST_RESORT_DURATION);
                
                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Last Resort Activated! 70% Damage Reduction");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Last Resort activated for " + player.getName() + " at " + healthPercent + "% health");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Last Resort activated!");
                }
            }
            
            // Set cooldown
            lastResortCooldown.put(playerId, System.currentTimeMillis());

            // Cancel existing task
            BukkitTask existingTask = lastResortTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule buff removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if buff is still active
                Long expiry = lastResortExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    // Remove the buff
                    SkillEffectsHandler.PlayerSkillStats currentStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    currentStats.addDefenseBonus(-70);
                    
                    lastResortExpiry.remove(playerId);
                    lastResortTasks.remove(playerId);
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Last Resort expired for " + player.getName());
                        if (player.isOnline()) {
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Last Resort expired");
                        }
                    }
                }
            }, LAST_RESORT_DURATION / 50); // Convert ms to ticks

            lastResortTasks.put(playerId, task);
        }

        // Check for Low Health Defense (ID 6)
        if (isPurchased(playerId, ID_OFFSET + 6) && healthPercent < 50) {
            // Get purchase count for stacking effect
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 6);

            // Apply defense bonus
            stats.addDefenseBonus(15 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Low Health Defense activated for " + player.getName() + 
                        " at " + healthPercent + "% health (+" + (15 * purchaseCount) + "% defense)");
            }
        }

        // Check for Desperate Defense (ID 25)
        if (isPurchased(playerId, ID_OFFSET + 25)) {
            // Calculate missing health percentage
            double missingHealthPercent = 100 - healthPercent;

            // Cap at 30%
            double damageReduction = Math.min(missingHealthPercent, 30);

            // Get previous Desperate Defense value if any
            double previousDefenseValue = desperateDefenseValues.getOrDefault(playerId, 0.0);
            
            // If there was a previous value, remove it first to prevent stacking
            if (previousDefenseValue > 0) {
                stats.addDefenseBonus(-previousDefenseValue);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Removed previous Desperate Defense value of " + previousDefenseValue + 
                            "% for " + player.getName());
                }
            }

            // Store the current Desperate Defense value for this player
            desperateDefenseValues.put(playerId, damageReduction);

            // Apply the new defense bonus
            stats.addDefenseBonus(damageReduction);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Desperate Defense activated for " + player.getName() + 
                        " at " + healthPercent + "% health (" + damageReduction + "% damage reduction)");
            }
        }

        // Check for Taunt (ID 7)
        if (isPurchased(playerId, ID_OFFSET + 7) && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            boolean success = rollChance(25.0, player, "Taunt");
            
            if (success) {
                // If this is an entity attack, the attacker will be handled in handleEntityDamageByEntity
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Taunt triggered");
                }
            }
        }

        // Check for Slowing Defense (ID 21)
        if (isPurchased(playerId, ID_OFFSET + 21) && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            boolean success = rollChance(10.0, player, "Slowing Defense");
            
            if (success) {
                // If this is an entity attack, the attacker will be handled in handleEntityDamageByEntity
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Slowing Defense triggered");
                }
            }
        }

        // Check for Shield Block (SHIELD BLOCK = -50% DMG)
        // First check custom shield block chance from stats
        double shieldBlockChance = stats.getShieldBlockChance();
        
        if (shieldBlockChance > 0) {
            boolean success = rollChance(shieldBlockChance, player, "Shield Block");
            
            if (success) {
                // Get original damage
                double originalDamage = event.getDamage();
                
                // Instead of multiplicative 50% reduction, add a fixed amount to defense bonus
                // This makes Shield Block additive with other defense bonuses
                // The actual damage reduction will be handled by the main defense calculation
                // which includes diminishing returns and capping
                
                // Store the original damage for logging purposes
                double finalDamage = originalDamage * 0.7; // Approximately 30% reduction
                event.setDamage(finalDamage);

                // Important defensive ability - always show notification
                ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Shield Block! Damage Reduced");

                if (debuggingFlag == 1) {
                    // Get Shield Block skill purchase counts
                    int sgShieldBlockPurchaseCount = plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, ID_OFFSET + 1);
                    boolean hasSgShieldBlock = plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(ID_OFFSET + 1);
                    
                    int dkShieldBlockPurchaseCount = plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, 14);
                    boolean hasDkShieldBlock = plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(14);
                    
                    plugin.getLogger().info("→ Shield Block SUCCESS! Reduced damage from " +
                            String.format("%.1f", originalDamage) + " to " + String.format("%.1f", finalDamage) + 
                            " (approximately 30% reduction)");
                    
                    StringBuilder sources = new StringBuilder();
                    if (hasDkShieldBlock) {
                        sources.append("DragonKnight: +").append(5 * dkShieldBlockPurchaseCount).append("% ");
                    }
                    if (hasSgShieldBlock) {
                        sources.append("ScaleGuardian: +").append(5 * sgShieldBlockPurchaseCount).append("%");
                    }
                    
                    plugin.getLogger().info("  Shield Block Sources: " + sources.toString());
                    
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] → Shield Block reduced damage: " + 
                        String.format("%.1f", originalDamage) + " → " + String.format("%.1f", finalDamage) + 
                        " (30% reduction)");
                }
                
                // Process additional effects for successful blocks
                handleSuccessfulBlock(player, event);
            }
        }
        // Also check for vanilla shield blocking as a fallback
        else if (event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)) {
            // This is a successful block from vanilla mechanics
            handleSuccessfulBlock(player, event);
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " blocked with vanilla shield mechanics");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Vanilla Shield Block!");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        if (player == event.getDamager()) {
            // Player is dealing damage
            handlePlayerDealingDamage(event, player);
        } else if (player == event.getEntity()) {
            // Player is taking damage
            handlePlayerTakingDamage(event, player);
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Scale Guardian doesn't have any skills that trigger on entity death
        // This method is required by the BaseSkillEffectsHandler abstract class
    }

    /**
     * Handle when a player successfully blocks an attack
     */
    private void handleSuccessfulBlock(Player player, EntityDamageEvent event) {
        UUID playerId = player.getUniqueId();

        // Increment block counter for Block Momentum (ID 18)
        if (isPurchased(playerId, ID_OFFSET + 18)) {
            int blocks = blockCounter.getOrDefault(playerId, 0) + 1;
            blockCounter.put(playerId, blocks);

            // Every 3 blocks, apply damage bonus
            if (blocks % 3 == 0) {
                // Apply damage bonus
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addDamageMultiplier(0.15);

                // Set expiry
                blockMomentumExpiry.put(playerId, System.currentTimeMillis() + BLOCK_MOMENTUM_DURATION);

                // Cancel existing task
                BukkitTask existingTask = blockMomentumTasks.get(playerId);
                if (existingTask != null) {
                    existingTask.cancel();
                }

                // Schedule buff removal
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Check if buff is still active
                    Long expiry = blockMomentumExpiry.get(playerId);
                    if (expiry != null && System.currentTimeMillis() >= expiry) {
                        // Remove the buff
                        SkillEffectsHandler.PlayerSkillStats currentStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                        currentStats.addDamageMultiplier(-0.15);

                        blockMomentumExpiry.remove(playerId);
                        blockMomentumTasks.remove(playerId);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Block Momentum expired for " + player.getName());
                            if (player.isOnline()) {
                                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Block Momentum expired");
                            }
                        }
                    }
                }, BLOCK_MOMENTUM_DURATION / 50); // Convert ms to ticks

                blockMomentumTasks.put(playerId, task);

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Block Momentum! +15% Damage");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Block Momentum activated for " + player.getName() + " after " + blocks + " blocks");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Block Momentum activated!");
                }
            }
        }

        // Set counter attack flag (ID 17)
        if (isPurchased(playerId, ID_OFFSET + 17)) {
            counterAttackReady.put(playerId, true);

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Counter Attack Ready!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Counter Attack ready for " + player.getName());
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Counter Attack ready!");
            }
        }

        // Apply Block Healing (ID 4)
        if (isPurchased(playerId, ID_OFFSET + 4)) {
            // Get purchase count for stacking effect
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 4);

            // Calculate healing amount (5% of max health per purchase)
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double healAmount = maxHealth * 0.05 * purchaseCount;

            // Apply healing
            double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
            player.setHealth(newHealth);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Block Healing activated for " + player.getName() + 
                        " (healed " + healAmount + " HP)");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Block Healing: +" + healAmount + " HP");
            }
        }

        // Apply Weakening Block (ID 9)
        if (isPurchased(playerId, ID_OFFSET + 9) && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = entityEvent.getDamager();

            if (attacker instanceof LivingEntity) {
                UUID attackerId = attacker.getUniqueId();

                // Get or create weakened enemies map
                Map<UUID, Integer> weakened = weakenedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
                Map<UUID, Long> expiry = weakenedEnemiesExpiry.computeIfAbsent(playerId, k -> new HashMap<>());
                Map<UUID, BukkitTask> tasks = weakenedEnemiesTasks.computeIfAbsent(playerId, k -> new HashMap<>());

                // Get current stacks
                int stacks = weakened.getOrDefault(attackerId, 0);

                // Add stack if below max
                if (stacks < 4) {
                    stacks++;
                    weakened.put(attackerId, stacks);

                    // Set expiry
                    expiry.put(attackerId, System.currentTimeMillis() + WEAKENING_BLOCK_DURATION);

                    // Cancel existing task
                    BukkitTask existingTask = tasks.get(attackerId);
                    if (existingTask != null) {
                        existingTask.cancel();
                    }

                    // Schedule stack removal
                    final int finalStacks = stacks;
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        // Check if stacks are still active
                        Long stackExpiry = expiry.get(attackerId);
                        if (stackExpiry != null && System.currentTimeMillis() >= stackExpiry) {
                            weakened.remove(attackerId);
                            expiry.remove(attackerId);
                            tasks.remove(attackerId);

                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("Weakening Block stacks expired for enemy " + attackerId);
                                if (player.isOnline()) {
                                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Block expired for enemy");
                                }
                            }
                        }
                    }, WEAKENING_BLOCK_DURATION / 50); // Convert ms to ticks

                    tasks.put(attackerId, task);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Weakening Block applied to enemy " + attackerId + 
                                " by " + player.getName() + " (" + stacks + "/4 stacks)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Block: " + stacks + "/4 stacks");
                    }
                }
            }
        }

        // Apply Shield Bash (ID 24)
        if (isPurchased(playerId, ID_OFFSET + 24) && event instanceof EntityDamageByEntityEvent &&
                !isOnCooldown(playerId, shieldBashCooldown, SHIELD_BASH_COOLDOWN)) {

            boolean success = rollChance(25.0, player, "Shield Bash");
            
            if (success) {
                EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                Entity attacker = entityEvent.getDamager();

                if (attacker instanceof LivingEntity) {
                    LivingEntity livingAttacker = (LivingEntity) attacker;

                    // Apply stun effect (slowness 255 for 1 second)
                    livingAttacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 255));

                    // Set cooldown
                    shieldBashCooldown.put(playerId, System.currentTimeMillis());

                    // Show notification
                    ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Shield Bash! Enemy Stunned");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("→ Stunned enemy for 1 second");
                        plugin.getLogger().info("Shield Bash stunned enemy " + attacker.getType() + 
                                " by " + player.getName());
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Shield Bash stunned enemy!");
                    }
                }
            }
        }

        // Apply Damage Reflection (ID 2)
        if (isPurchased(playerId, ID_OFFSET + 2) && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = entityEvent.getDamager();

            if (attacker instanceof LivingEntity) {
                LivingEntity livingAttacker = (LivingEntity) attacker;

                // Get purchase count for stacking effect
                int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 2);

                // Calculate reflection damage (10% of original damage per purchase)
                double originalDamage = event.getDamage();
                double reflectionDamage = originalDamage * 0.10 * purchaseCount;

                // Apply reflection damage
                livingAttacker.damage(reflectionDamage, player);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Damage Reflection activated for " + player.getName() + 
                            " (reflected " + reflectionDamage + " damage to " + attacker.getType() + ")");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Damage Reflection: " + reflectionDamage + " damage");
                }
            }
        }

        // Apply Critical Block (ID 13)
        if (isPurchased(playerId, ID_OFFSET + 13) && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = entityEvent.getDamager();

            // Check if this was a critical hit (we'll use a heuristic since we can't directly detect it)
            // In Minecraft, critical hits typically do 50% more damage
            if (attacker instanceof LivingEntity) {
                LivingEntity livingAttacker = (LivingEntity) attacker;

                // Assume it's a critical hit if the damage is high relative to the attacker's attack damage
                double originalDamage = event.getDamage();
                double reflectionDamage = originalDamage * 0.40;

                // Apply reflection damage
                livingAttacker.damage(reflectionDamage, player);

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Critical Block! Reflected " + 
                        String.format("%.1f", reflectionDamage) + " Damage");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Critical Block activated for " + player.getName() + 
                            " (reflected " + reflectionDamage + " damage to " + attacker.getType() + ")");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Critical Block: " + reflectionDamage + " damage");
                }
            }
        }
    }

    /**
     * Handle when a player is dealing damage to an entity
     */
    private void handlePlayerDealingDamage(EntityDamageByEntityEvent event, Player player) {
        UUID playerId = player.getUniqueId();
        Entity target = event.getEntity();

        // Apply Counter Attack (ID 17)
        if (isPurchased(playerId, ID_OFFSET + 17) && counterAttackReady.getOrDefault(playerId, false)) {
            // Get purchase count for stacking effect
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 17);

            // Apply damage bonus
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * 0.30 * purchaseCount;
            event.setDamage(originalDamage + bonusDamage);

            // Reset counter attack flag
            counterAttackReady.put(playerId, false);

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Counter Attack! +" + 
                    String.format("%.1f", bonusDamage) + " Damage");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Counter Attack activated for " + player.getName() + 
                        " (+" + bonusDamage + " damage)");
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Counter Attack: +" + bonusDamage + " damage");
            }
        }

        // Apply Weakening Strike (ID 11)
        if (isPurchased(playerId, ID_OFFSET + 11) && target instanceof LivingEntity) {
            boolean success = rollChance(15.0, player, "Weakening Strike");
            
            if (success) {
                LivingEntity livingTarget = (LivingEntity) target;
                UUID targetId = livingTarget.getUniqueId();

                // Get or create weakened enemies map
                Map<UUID, Integer> weakened = weakenedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
                Map<UUID, Long> expiry = weakenedEnemiesExpiry.computeIfAbsent(playerId, k -> new HashMap<>());
                Map<UUID, BukkitTask> tasks = weakenedEnemiesTasks.computeIfAbsent(playerId, k -> new HashMap<>());

                // Set weakening (10% damage reduction)
                weakened.put(targetId, 1);

                // Set expiry
                expiry.put(targetId, System.currentTimeMillis() + WEAKENING_STRIKE_DURATION);

                // Cancel existing task
                BukkitTask existingTask = tasks.get(targetId);
                if (existingTask != null) {
                    existingTask.cancel();
                }

                // Schedule effect removal
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Check if effect is still active
                    Long effectExpiry = expiry.get(targetId);
                    if (effectExpiry != null && System.currentTimeMillis() >= effectExpiry) {
                        weakened.remove(targetId);
                        expiry.remove(targetId);
                        tasks.remove(targetId);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Weakening Strike expired for enemy " + targetId);
                            if (player.isOnline()) {
                                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Strike expired for enemy");
                            }
                        }
                    }
                }, WEAKENING_STRIKE_DURATION / 50); // Convert ms to ticks

                tasks.put(targetId, task);

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Weakening Strike! Enemy Damage -10%");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Weakened enemy for 5 seconds (-10% damage)");
                    plugin.getLogger().info("Weakening Strike applied to enemy " + targetId + 
                            " by " + player.getName());
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Strike applied to enemy");
                }
            }
        }
    }

    /**
     * Handle when a player is taking damage from an entity
     */
    private void handlePlayerTakingDamage(EntityDamageByEntityEvent event, Player player) {
        UUID playerId = player.getUniqueId();
        Entity attacker = event.getDamager();

        // Apply Taunt (ID 7)
        if (isPurchased(playerId, ID_OFFSET + 7) && attacker instanceof LivingEntity) {
            boolean success = rollChance(25.0, player, "Taunt");
            
            if (success) {
                LivingEntity livingAttacker = (LivingEntity) attacker;

                // Set the attacker's target to the player
                // This is a simplification - in a real implementation, you'd need to use
                // the appropriate mob-specific targeting method
                if (livingAttacker instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) livingAttacker).setTarget(player);

                    // Show notification
                    ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Taunt! Enemy Focused on You");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("→ Taunted enemy to focus on player");
                        plugin.getLogger().info("Taunt applied to enemy " + attacker.getType() + 
                                " by " + player.getName());
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Taunt applied to enemy");
                    }
                }
            }
        }

        // Apply Slowing Defense (ID 21)
        if (isPurchased(playerId, ID_OFFSET + 21) && attacker instanceof LivingEntity) {
            boolean success = rollChance(10.0, player, "Slowing Defense");
            
            if (success) {
                LivingEntity livingAttacker = (LivingEntity) attacker;

                // Apply slowness effect (20% slower for 3 seconds)
                livingAttacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Slowing Defense! Enemy Slowed");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Slowed enemy for 3 seconds (-20% speed)");
                    plugin.getLogger().info("Slowing Defense applied to enemy " + attacker.getType() + 
                            " by " + player.getName());
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Slowing Defense applied to enemy");
                }
            }
        }

        // Apply damage reduction from Weakening Block (ID 9)
        if (isPurchased(playerId, ID_OFFSET + 9) && attacker instanceof LivingEntity) {
            UUID attackerId = attacker.getUniqueId();

            // Get weakened enemies map
            Map<UUID, Integer> weakened = weakenedEnemies.get(playerId);
            if (weakened != null) {
                Integer stacks = weakened.get(attackerId);
                if (stacks != null && stacks > 0) {
                    // Apply damage reduction (5% per stack)
                    double baseReduction = 0.05 * stacks;
                    double originalDamage = event.getDamage();
            
                    // Scale reduction based on current damage to prevent near-immunity when stacked
                    // with other defensive mechanics
                    double scaledReduction = baseReduction * Math.min(1.0, originalDamage / 5.0);
            
                    // Ensure damage doesn't go below a reasonable minimum (0.5)
                    double reducedDamage = Math.max(0.5, originalDamage * (1 - scaledReduction));
                    event.setDamage(reducedDamage);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Weakening Block reduced damage from enemy " + attackerId + 
                                " by " + (scaledReduction * 100) + "% (scaled from " + (baseReduction * 100) + "%, " + stacks + " stacks)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Block: -" + 
                                (scaledReduction * 100) + "% damage (scaled from " + (baseReduction * 100) + "%, " + stacks + " stacks)");
                    }
                }
            }
        }

        // Apply damage reduction from Weakening Strike (ID 11)
        if (isPurchased(playerId, ID_OFFSET + 11) && attacker instanceof LivingEntity) {
            UUID attackerId = attacker.getUniqueId();

            // Get weakened enemies map
            Map<UUID, Integer> weakened = weakenedEnemies.get(playerId);
            if (weakened != null) {
                Integer stacks = weakened.get(attackerId);
                if (stacks != null && stacks > 0) {
                    // Apply damage reduction (10%)
                    double originalDamage = event.getDamage();
                    // Reduce effectiveness when damage is already low
                    double reductionFactor = Math.min(0.1, 0.1 * (originalDamage / 5.0)); // Scale reduction based on damage
                    // Ensure damage doesn't go below a reasonable minimum
                    double reducedDamage = Math.max(0.5, originalDamage * (1 - reductionFactor));
                    event.setDamage(reducedDamage);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Weakening Strike reduced damage from enemy " + attackerId + 
                                " by " + (reductionFactor * 100) + "% (scaled based on current damage)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Weakening Strike: -" + 
                                String.format("%.1f", reductionFactor * 100) + "% damage (scaled from 10%)");
                    }
                }
            }
        }

        // Apply Critical Immunity (ID 23)
        if (isPurchased(playerId, ID_OFFSET + 23)) {
            // Check if immunity is active
            Long expiry = criticalImmunityExpiry.get(playerId);
            if (expiry != null && System.currentTimeMillis() < expiry) {
                // Assume this is a critical hit and reduce damage by 50%
                // In a real implementation, you'd need to detect critical hits properly
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * 0.5;
                event.setDamage(reducedDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Critical Immunity reduced critical hit damage for " + player.getName() + 
                            " by 50%");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Critical Immunity: -50% crit damage");
                }
            }
        }
    }

    /**
     * Check for stationary players to apply Stationary Defense (ID 8)
     */
    private void checkStationaryPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // Skip if player doesn't have the skill
            if (!isPurchased(playerId, ID_OFFSET + 8)) continue;

            Location currentLocation = player.getLocation();
            Location lastLocation = lastPlayerLocation.get(playerId);

            // Update last location
            lastPlayerLocation.put(playerId, currentLocation);

            // Check if player has moved
            if (lastLocation != null && isSameLocation(currentLocation, lastLocation)) {
                // Player hasn't moved
                Long lastMoved = lastMovementTime.getOrDefault(playerId, 0L);
                long currentTime = System.currentTimeMillis();

                // Check if player has been stationary for 2 seconds
                if (currentTime - lastMoved >= 2000) {
                    // Apply stationary defense if not already applied
                    if (!isStationary.getOrDefault(playerId, false)) {
                        // Get purchase count for stacking effect
                        int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 8);

                        // Apply defense bonus
                        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                        stats.addDefenseBonus(10 * purchaseCount);

                        // Set stationary flag
                        isStationary.put(playerId, true);

                        // Show notification
                        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Stationary Defense! +" + 
                                (10 * purchaseCount) + "% Defense");

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Stationary Defense activated for " + player.getName() + 
                                    " (+" + (10 * purchaseCount) + "% defense)");
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Stationary Defense activated!");
                        }
                    }
                }
            } else {
                // Player has moved
                lastMovementTime.put(playerId, System.currentTimeMillis());

                // Remove stationary defense if it was applied
                if (isStationary.getOrDefault(playerId, false)) {
                    // Get purchase count for stacking effect
                    int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 8);

                    // Remove defense bonus
                    SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    stats.addDefenseBonus(-10 * purchaseCount);

                    // Reset stationary flag
                    isStationary.put(playerId, false);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Stationary Defense deactivated for " + player.getName());
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Stationary Defense deactivated");
                    }
                }
            }
        }
    }

    /**
     * Check for nearby entities to apply Proximity Defense (ID 3)
     */
    public void checkProximityDefense(Player player) {
        UUID playerId = player.getUniqueId();

        // Skip if player doesn't have the skill
        if (!isPurchased(playerId, ID_OFFSET + 3)) return;

        // Count nearby enemies
        int nearbyEnemies = 0;
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                nearbyEnemies++;
            }
        }

        // Cap at 5 enemies (for max 15% bonus)
        nearbyEnemies = Math.min(nearbyEnemies, 5);

        // Apply defense bonus
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        double defenseBonus = nearbyEnemies * 3;

        // Store the current modifier to avoid stacking
        Double currentModifier = playerDefenseModifier.getOrDefault(playerId, 0.0);
        stats.addDefenseBonus(-currentModifier); // Remove old modifier
        stats.addDefenseBonus(defenseBonus); // Add new modifier
        playerDefenseModifier.put(playerId, defenseBonus);

        if (debuggingFlag == 1 && nearbyEnemies > 0) {
            plugin.getLogger().info("Proximity Defense activated for " + player.getName() + 
                    " (+" + defenseBonus + "% defense from " + nearbyEnemies + " nearby enemies)");
            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Proximity Defense: +" + 
                    defenseBonus + "% defense (" + nearbyEnemies + " enemies)");
        }
    }

    /**
     * Check for surrounded status to apply Surrounded Healing (ID 16)
     */
    public void checkSurroundedHealing(Player player) {
        UUID playerId = player.getUniqueId();

        // Skip if player doesn't have the skill
        if (!isPurchased(playerId, ID_OFFSET + 16)) return;

        // Count nearby enemies
        int nearbyEnemies = 0;
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                nearbyEnemies++;
            }
        }

        // Apply healing bonus if surrounded by 3+ enemies
        if (nearbyEnemies >= 3) {
            // Fixed healing bonus of 5% when surrounded by 3+ enemies
            double healingBonus = 5.0;

            // Store the current modifier to avoid stacking
            Double currentModifier = playerHealingModifier.getOrDefault(playerId, 0.0);

            // Update healing modifier if different
            if (Math.abs(currentModifier - healingBonus) > 0.1) {
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addDefenseBonus(-currentModifier); // Remove old modifier (using defense as proxy for healing)
                stats.addDefenseBonus(healingBonus); // Add new modifier (using defense as proxy for healing)
                playerHealingModifier.put(playerId, healingBonus);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Surrounded Healing activated for " + player.getName() + 
                            " (+5% healing when surrounded by 3+ enemies)");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Surrounded Healing: +5% healing (surrounded by " + 
                            nearbyEnemies + " enemies)");
                }
            }
        } else {
            // Remove healing bonus if not surrounded
            Double currentModifier = playerHealingModifier.getOrDefault(playerId, 0.0);
            if (currentModifier > 0) {
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addDefenseBonus(-currentModifier); // Using defense as proxy for healing
                playerHealingModifier.put(playerId, 0.0);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Surrounded Healing deactivated for " + player.getName());
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Surrounded Healing deactivated");
                }
            }
        }
    }

    /**
     * Check for nearby allies to apply Protective Aura (ID 14) and Ally Protection (ID 26)
     */
    public void checkAllyEffects(Player player) {
        UUID playerId = player.getUniqueId();

        boolean hasProtectiveAura = isPurchased(playerId, ID_OFFSET + 14);
        boolean hasAllyProtection = isPurchased(playerId, ID_OFFSET + 26);

        // Skip if player doesn't have either skill
        if (!hasProtectiveAura && !hasAllyProtection) return;

        // Get player's health percentage for Healthy Protection (ID 22)
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double healthPercent = (currentHealth / maxHealth) * 100;
        boolean isHealthy = healthPercent >= 80;
        boolean hasHealthyProtection = isPurchased(playerId, ID_OFFSET + 22) && isHealthy;

        // Find nearby allies
        for (Entity entity : player.getNearbyEntities(12, 12, 12)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;

                // Check if the player is in the same party as the ally
                if (!isInSameParty(player, ally)) {
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Skipping player " + ally.getName() + " - not in the same party as " + player.getName());
                    }
                    continue;
                }

                // Apply Protective Aura (ID 14) - 8 block range
                if (hasProtectiveAura && player.getLocation().distance(ally.getLocation()) <= 8) {
                    SkillEffectsHandler.PlayerSkillStats allyStats = plugin.getSkillEffectsHandler().getPlayerStats(ally);
                    allyStats.addDefenseBonus(10);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Protective Aura applied to party member " + ally.getName() + 
                                " by " + player.getName() + " (+10% defense)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Protective Aura applied to " + ally.getName());
                    }
                }

                // Apply Ally Protection (ID 26) - 12 block range
                if (hasAllyProtection) {
                    // Get purchase count for stacking effect
                    int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 26);

                    SkillEffectsHandler.PlayerSkillStats allyStats = plugin.getSkillEffectsHandler().getPlayerStats(ally);
                    allyStats.addShieldBlockChance(10 * purchaseCount);
                    allyStats.addDamageMultiplier(0.05 * purchaseCount);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Ally Protection applied to party member " + ally.getName() + 
                                " by " + player.getName() + " (+" + (10 * purchaseCount) + "% shield block, +" + 
                                (5 * purchaseCount) + "% damage)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Ally Protection applied to " + ally.getName());
                    }
                }

                // Apply Healthy Protection (ID 22) - 12 block range
                if (hasHealthyProtection) {
                    // Apply damage reduction to ally (using defense bonus as proxy)
                    SkillEffectsHandler.PlayerSkillStats allyStats = plugin.getSkillEffectsHandler().getPlayerStats(ally);
                    allyStats.addDefenseBonus(15);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Healthy Protection applied to party member " + ally.getName() + 
                                " by " + player.getName() + " (15% damage reduction)");
                        ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Healthy Protection applied to " + ally.getName());
                    }
                }

                // Check for Guardian Angel (ID 19) - 10 block range
                if (isPurchased(playerId, ID_OFFSET + 19) && player.getLocation().distance(ally.getLocation()) <= 10 &&
                        !isOnCooldown(playerId, guardianAngelCooldown, GUARDIAN_ANGEL_COOLDOWN)) {

                    // Check ally's health
                    double allyMaxHealth = ally.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double allyCurrentHealth = ally.getHealth();
                    double allyHealthPercent = (allyCurrentHealth / allyMaxHealth) * 100;

                    if (allyHealthPercent < 30) {
                        // Apply movement speed to player
                        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                        if (speedAttr != null) {
                            // Remove existing modifiers
                            for (AttributeModifier mod : speedAttr.getModifiers()) {
                                if (mod.getName().equals("skill.guardianAngel")) {
                                    speedAttr.removeModifier(mod);
                                }
                            }

                            // Add new modifier
                            AttributeModifier speedMod = new AttributeModifier(
                                    UUID.randomUUID(),
                                    "skill.guardianAngel",
                                    0.3, // 30% increase
                                    AttributeModifier.Operation.ADD_SCALAR
                            );
                            speedAttr.addModifier(speedMod);

                            // Schedule modifier removal after 5 seconds
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                speedAttr.getModifiers().stream()
                                        .filter(mod -> mod.getName().equals("skill.guardianAngel"))
                                        .forEach(speedAttr::removeModifier);
                            }, 100); // 5 seconds = 100 ticks
                        }

                        // Heal ally
                        double healAmount = allyMaxHealth * 0.5;
                        double newHealth = Math.min(allyCurrentHealth + healAmount, allyMaxHealth);
                        ally.setHealth(newHealth);

                        // Set cooldown
                        guardianAngelCooldown.put(playerId, System.currentTimeMillis());

                        // Show notification
                        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Guardian Angel! Healed " + ally.getName());
                        ActionBarUtils.sendActionBar(ally, ChatColor.AQUA + player.getName() + " healed you with Guardian Angel!");

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Guardian Angel activated for " + player.getName() + 
                                    " to heal ally " + ally.getName() + " for " + healAmount + " HP");
                            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Guardian Angel healed " + 
                                    ally.getName() + " for " + healAmount + " HP");
                        }
                    }
                }
            }
        }
    }

    /**
     * Check for heavy armor to apply Heavy Armor Mastery (ID 12)
     */
    public void checkHeavyArmorMastery(Player player) {
        UUID playerId = player.getUniqueId();

        // Skip if player doesn't have the skill
        if (!isPurchased(playerId, ID_OFFSET + 12)) return;

        // Count heavy armor pieces
        int heavyArmorPieces = 0;
        EntityEquipment equipment = player.getEquipment();
        StringBuilder armorTypes = new StringBuilder();

        if (equipment != null) {
            // Check helmet
            ItemStack helmet = equipment.getHelmet();
            if (isHeavyArmor(helmet)) {
                heavyArmorPieces++;
                armorTypes.append(helmet.getType().name()).append(" ");
            }

            // Check chestplate
            ItemStack chestplate = equipment.getChestplate();
            if (isHeavyArmor(chestplate)) {
                heavyArmorPieces++;
                armorTypes.append(chestplate.getType().name()).append(" ");
            }

            // Check leggings
            ItemStack leggings = equipment.getLeggings();
            if (isHeavyArmor(leggings)) {
                heavyArmorPieces++;
                armorTypes.append(leggings.getType().name()).append(" ");
            }

            // Check boots
            ItemStack boots = equipment.getBoots();
            if (isHeavyArmor(boots)) {
                heavyArmorPieces++;
                armorTypes.append(boots.getType().name()).append(" ");
            }
        }

        // Get purchase count for stacking effect
        int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 12);

        // Apply armor as actual armor value to the player
        AttributeInstance armorAttribute = player.getAttribute(Attribute.GENERIC_ARMOR);
        if (armorAttribute != null) {
            // Remove any existing modifiers from this skill
            armorAttribute.getModifiers().stream()
                    .filter(mod -> mod.getName().equals("skill.heavyArmorMastery"))
                    .forEach(armorAttribute::removeModifier);

            // Add new modifier if we have heavy armor pieces
            if (heavyArmorPieces > 0) {
                double armorBonus = 3.0 * heavyArmorPieces * purchaseCount;
                AttributeModifier armorModifier = new AttributeModifier(
                        UUID.randomUUID(),
                        "skill.heavyArmorMastery",
                        armorBonus,
                        AttributeModifier.Operation.ADD_NUMBER
                );
                armorAttribute.addModifier(armorModifier);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Heavy Armor Mastery activated for " + player.getName() + 
                            " (+" + armorBonus + " armor from " + heavyArmorPieces + " heavy armor pieces)");
                    plugin.getLogger().info("  Armor pieces: " + armorTypes.toString());
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Heavy Armor Mastery: +" + 
                            armorBonus + " armor (" + heavyArmorPieces + " pieces: " + armorTypes.toString() + ")");
                }
            }
        }
    }

    /**
     * Check if an item is heavy armor
     */
    private boolean isHeavyArmor(ItemStack item) {
        if (item == null) return false;

        Material material = item.getType();
        return material == Material.IRON_HELMET || material == Material.IRON_CHESTPLATE || 
               material == Material.IRON_LEGGINGS || material == Material.IRON_BOOTS || 
               material == Material.DIAMOND_HELMET || material == Material.DIAMOND_CHESTPLATE || 
               material == Material.DIAMOND_LEGGINGS || material == Material.DIAMOND_BOOTS || 
               material == Material.NETHERITE_HELMET || material == Material.NETHERITE_CHESTPLATE || 
               material == Material.NETHERITE_LEGGINGS || material == Material.NETHERITE_BOOTS;
    }

    /**
     * Apply Potion Master (ID 20) to extend potion durations
     * 
     * NOTE: This currently only works with vanilla Minecraft potions.
     * It does not work with custom alchemy effects from the plugin due to the way
     * AlchemyEffect is implemented (duration is final and set in constructor).
     * To make it work with custom effects would require significant changes to the alchemy system.
     */
    public void extendPotionDuration(Player player, PotionEffect effect) {
        UUID playerId = player.getUniqueId();

        // Skip if player doesn't have the skill
        if (!isPurchased(playerId, ID_OFFSET + 20)) return;

        // Skip if it's a negative effect
        if (isNegativeEffect(effect.getType())) return;

        // Get purchase count for stacking effect
        int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 20);

        // Calculate extended duration (20% per purchase)
        int originalDuration = effect.getDuration();
        int extendedDuration = (int) (originalDuration * (1 + 0.2 * purchaseCount));

        // Apply extended effect
        PotionEffect extendedEffect = new PotionEffect(
                effect.getType(),
                extendedDuration,
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.hasParticles(),
                effect.hasIcon()
        );

        // Remove original effect and apply extended one
        player.removePotionEffect(effect.getType());
        player.addPotionEffect(extendedEffect);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Potion Master extended " + effect.getType() + " duration for " + player.getName() + 
                    " from " + originalDuration + " to " + extendedDuration + " ticks");
            ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] Potion Master: Extended " + 
                    effect.getType() + " by " + (20 * purchaseCount) + "%");
        }
    }

    /**
     * Check if a potion effect is negative
     */
    private boolean isNegativeEffect(PotionEffectType type) {
        return type == PotionEffectType.POISON || type == PotionEffectType.WITHER || 
               type == PotionEffectType.HARM || type == PotionEffectType.CONFUSION || 
               type == PotionEffectType.BLINDNESS || type == PotionEffectType.HUNGER || 
               type == PotionEffectType.WEAKNESS || type == PotionEffectType.SLOW || 
               type == PotionEffectType.SLOW_DIGGING;
    }

    /**
     * Check if two locations are the same (for stationary detection)
     */
    private boolean isSameLocation(Location loc1, Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) return false;

        // Check if the player has moved significantly
        double distanceSquared = loc1.distanceSquared(loc2);
        return distanceSquared < 0.01; // Very small threshold for movement
    }

    /**
     * Check if two players are in the same party using PartyAPI
     * @param player1 First player
     * @param player2 Second player
     * @return true if both players are in the same party, false otherwise
     */
    private boolean isInSameParty(Player player1, Player player2) {
        // If either player is null, they can't be in the same party
        if (player1 == null || player2 == null) return false;

        // If it's the same player, return true
        if (player1.equals(player2)) return true;

        // Check if player1 is in a party
        if (!PartyAPI.isInParty(player1)) return false;

        // Get all members of player1's party
        List<Player> partyMembers = PartyAPI.getPartyMembers(player1);

        // Check if player2 is in the list of party members
        return partyMembers.contains(player2);
    }

    /**
     * Check if player has purchased a specific skill
     */
    protected boolean isPurchased(UUID playerId, int skillId) {
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
        playerDefenseModifier.remove(playerId);
        playerDamageModifier.remove(playerId);
        playerHealingModifier.remove(playerId);
        
        // Clear Desperate Defense values
        desperateDefenseValues.remove(playerId);

        lastStandCooldown.remove(playerId);
        guardianAngelCooldown.remove(playerId);
        shieldBashCooldown.remove(playerId);
        lastResortCooldown.remove(playerId);

        lastStandExpiry.remove(playerId);
        reactiveDefenseExpiry.remove(playerId);
        blockMomentumExpiry.remove(playerId);
        criticalImmunityExpiry.remove(playerId);
        lastResortExpiry.remove(playerId);

        BukkitTask lastStandTask = lastStandTasks.remove(playerId);
        if (lastStandTask != null) {
            lastStandTask.cancel();
        }

        BukkitTask reactiveDefenseTask = reactiveDefenseTasks.remove(playerId);
        if (reactiveDefenseTask != null) {
            reactiveDefenseTask.cancel();
        }

        BukkitTask blockMomentumTask = blockMomentumTasks.remove(playerId);
        if (blockMomentumTask != null) {
            blockMomentumTask.cancel();
        }

        BukkitTask criticalImmunityTask = criticalImmunityTasks.remove(playerId);
        if (criticalImmunityTask != null) {
            criticalImmunityTask.cancel();
        }

        BukkitTask lastResortTask = lastResortTasks.remove(playerId);
        if (lastResortTask != null) {
            lastResortTask.cancel();
        }

        blockCounter.remove(playerId);
        counterAttackReady.remove(playerId);

        weakenedEnemies.remove(playerId);
        weakenedEnemiesExpiry.remove(playerId);
        Map<UUID, BukkitTask> tasks = weakenedEnemiesTasks.remove(playerId);
        if (tasks != null) {
            tasks.values().forEach(BukkitTask::cancel);
        }

        lastPlayerLocation.remove(playerId);
        lastMovementTime.remove(playerId);
        isStationary.remove(playerId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all Scale Guardian data for player ID: " + playerId);
        }
    }
}
