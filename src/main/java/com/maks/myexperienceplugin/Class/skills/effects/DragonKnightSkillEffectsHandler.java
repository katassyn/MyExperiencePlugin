package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handler for DragonKnight-specific skill effects
 */
public class DragonKnightSkillEffectsHandler extends BaseSkillEffectsHandler {
    private final Random random = new Random();
    private final int debuggingFlag = 0; // Set to 0 in production
    
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

    // Track Battle Rhythm (skill 2) attack speed buff
    private final Map<UUID, Long> battleRhythmCooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> battleRhythmTasks = new HashMap<>();

    // Track Battle Fury (skill 6) damage stacks
    private final Map<UUID, Integer> battleFuryStacks = new HashMap<>();
    private final Map<UUID, BukkitTask> battleFuryTasks = new HashMap<>();

    // Track damage dealt for Battle Fury tracking
    private final Map<UUID, Double> battleFuryDamageCounter = new HashMap<>();

    // Track Dragon Heart (skill 7) cooldown
    private final Map<UUID, Long> dragonHeartCooldowns = new HashMap<>();

    // Track cooldown notifications
    private final Map<UUID, BukkitTask> dragonHeartNotifyTasks = new HashMap<>();

    public DragonKnightSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        switch (skillId) {
            case 1: // +3% def
                stats.addDefenseBonus(3 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 1: Added " + (3 * purchaseCount) + "% defense bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 1: +" + (3 * purchaseCount) + "% defense bonus");
                }
                break;
            case 2: // After successful hit gain +5% as for 5s
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 2: Battle Rhythm will be applied on hits");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 2: Battle Rhythm enabled (+5% attack speed on hit)");
                }
                break;
            case 3: // +1% dmg
                stats.addDamageMultiplier(0.01 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 3: Added " + (0.01 * purchaseCount) + " to damage multiplier");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 3: +" + (1 * purchaseCount) + "% damage multiplier");
                }
                break;
            case 4: // When hp<50% gain +2% def
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 4: Endurance will be applied when HP is low");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 4: Endurance enabled (+2% defense when HP < 50%)");
                }
                break;
            case 5: // +1% ms (1/2)
                stats.addMovementSpeedBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 5: Added " + (1 * purchaseCount) + "% movement speed bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 5: +" + (1 * purchaseCount) + "% movement speed bonus");
                }
                break;
            case 6: // For every 100 damage dealt, you increase your damage by 1% for 5 seconds (stack up to 3 times)
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 6: Battle Fury will be applied when dealing damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 6: Battle Fury enabled (+1% dmg per 100 damage dealt, max 3 stacks)");
                }
                break;
            case 7: // When hp<20% gain 20hp for 5s (cd 1min)
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 7: Dragon Heart will be applied when HP is very low");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 7: Dragon Heart enabled (+20 HP when HP < 20%, 60s cd)");
                }
                break;
            case 8: // +2hp (1/2)
                stats.addMaxHealth(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 8: Added " + (2 * purchaseCount) + " max health bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 8: +" + (2 * purchaseCount) + " max health");
                }
                break;
            case 9: // +1% luck (1/2)
                stats.addLuckBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 9: Added " + (1 * purchaseCount) + "% luck bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 9: +" + (1 * purchaseCount) + "% luck bonus");
                }
                break;
            case 10: // +7 dmg (1/2) - FIXED VALUE
                stats.addBonusDamage(7 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 10: Added " + (7 * purchaseCount) + " bonus damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 10: +" + (7 * purchaseCount) + " bonus damage");
                }
                break;
            case 11: // +5% dmg, -2% ms
                stats.addDamageMultiplier(0.05 * purchaseCount);
                stats.addMovementSpeedBonus(-2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 11: Added " + (5 * purchaseCount) + "% damage multiplier and " +
                            "reduced movement speed by " + (2 * purchaseCount) + "%");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 11: +" + (5 * purchaseCount) + "% damage, -" + (2 * purchaseCount) + "% movement speed");
                }
                break;
            case 12: // After killing a mob u heal 2hp
                // This effect is handled in the EntityDeathEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 12: Life Drain will be applied on kill");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 12: Life Drain enabled (+2 HP on kill)");
                }
                break;
            case 13: // +10 dmg - FIXED VALUE
                stats.addBonusDamage(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 13: Added 10 bonus damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 13: +10 bonus damage");
                }
                break;
            case 14: // +5% shield block chance
                stats.addShieldBlockChance(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 14: Added " + (5 * purchaseCount) + "% shield block chance");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] DRAGONKNIGHT SKILL 14: +" + (5 * purchaseCount) + "% shield block chance");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown DragonKnight skill ID: " + skillId);
                }
                break;
        }
    }

    // Constant for flame reflection
    private static final double FLAME_REFLECTION_PERCENT = 0.25; // 25% damage reflection

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for shield block chance - it's a passive damage reduction ability
        // not related to actually having a shield equipped
        if (stats.getShieldBlockChance() > 0 && rollChance(stats.getShieldBlockChance(), player, "Shield Block")) {
            // Get original damage
            double originalDamage = event.getDamage();
            // Apply 50% damage reduction
            event.setDamage(originalDamage * 0.5);

            // Important defensive ability - always show notification
            // Simple notification just like Ranger's evade
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Shield Block!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " blocked with Shield Block ability, reduced damage from " +
                        originalDamage + " to " + event.getDamage() + " (50% reduction)");
                plugin.getLogger().info("Shield Block chance was: " + stats.getShieldBlockChance() + "%");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Shield Block! " + originalDamage + " → " + event.getDamage() + " dmg");
            }
            
            // Check for Flame Reflection skill
            if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(18)) {
                // Only apply if damage is from an entity
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                    Entity damager = entityEvent.getDamager();
                    
                    if (damager instanceof LivingEntity) {
                        LivingEntity attacker = (LivingEntity) damager;
                        // Calculate reflection damage (25% of original damage)
                        double reflectionDamage = originalDamage * FLAME_REFLECTION_PERCENT;
                        
                        // Apply fire damage to attacker
                        attacker.damage(reflectionDamage, player);
                        
                        // Set attacker on fire for 3 seconds (60 ticks)
                        attacker.setFireTicks(60);
                        
                        // Show notification
                        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Flame Reflection! (" + 
                                String.format("%.1f", reflectionDamage) + " damage)");
                        
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Flame Reflection activated for " + player.getName() + 
                                    ", reflected " + reflectionDamage + " damage to " + attacker.getType());
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Flame Reflection: " + 
                                    reflectionDamage + " damage to " + attacker.getType());
                        }
                    }
                }
            }
        }

        // Check for Dragon Heart skill (When hp<20% gain 20hp for 5s)
        // Important cooldown-based skill - always notify player
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(7)) {
            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;
            long currentTime = System.currentTimeMillis();

            // Check if it's on cooldown
            boolean onCooldown = dragonHeartCooldowns.containsKey(playerId) &&
                    currentTime - dragonHeartCooldowns.get(playerId) <= 60000;

            if (healthPercent < 20) {
                if (!onCooldown) {
                    // Apply temporary health boost
                    player.setHealth(Math.min(player.getHealth() + 20, player.getMaxHealth()));
                    dragonHeartCooldowns.put(playerId, currentTime);

                    // IMPORTANT COOLDOWN SKILL - Show activation
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "DRAGON HEART activated! +20 HP");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Player " + player.getName() + " activated Dragon Heart at " + healthPercent + "% health");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Dragon Heart activated");
                    }

                    // Schedule a notification when cooldown expires
                    // IMPORTANT: Notify when cooldown is over
                    BukkitTask existingTask = dragonHeartNotifyTasks.get(playerId);
                    if (existingTask != null) {
                        existingTask.cancel();
                    }

                    BukkitTask notifyTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            ActionBarUtils.sendActionBar(player,
                                    ChatColor.GREEN + "Dragon Heart ready!");
                            dragonHeartNotifyTasks.remove(playerId);

                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("Dragon Heart cooldown expired for " + player.getName());
                                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Dragon Heart cooldown expired");
                            }
                        }
                    }, 1200L); // 60 seconds = 1200 ticks

                    dragonHeartNotifyTasks.put(playerId, notifyTask);

                } else if (debuggingFlag == 1) {
                    // Show cooldown only in debug mode
                    long timeLeft = 60 - (currentTime - dragonHeartCooldowns.get(playerId)) / 1000;
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Dragon Heart on cooldown (" + timeLeft + "s)");
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Get final damage for more accurate tracking
        double damage = event.getFinalDamage();

        // Check for Battle Rhythm skill (After hit gain +5% as for 5s)
        // Don't show notification since it's a common passive effect
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(2)) {
            // Get attack speed attribute for the player
            AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);

            // Constant name for our modifier
            String modifierName = "skill.battlerhythm";

            // Remove existing modifiers with the same name
            attackSpeedAttr.getModifiers().stream()
                    .filter(mod -> mod.getName().equals(modifierName))
                    .forEach(mod -> attackSpeedAttr.removeModifier(mod));

            // Create new attack speed modifier (+5%)
            AttributeModifier attackSpeedMod = new AttributeModifier(
                    UUID.randomUUID(),
                    modifierName,
                    0.05,  // 5% increase in attack speed
                    AttributeModifier.Operation.ADD_SCALAR  // Multiplicative operation
            );

            // Add the new modifier
            attackSpeedAttr.addModifier(attackSpeedMod);

            // REMOVED the action bar notification completely, even in debug mode
            // Only keep console logs and chat debug message
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rhythm for 5 seconds");
                plugin.getLogger().info("Added attack speed modifier: " +
                        attackSpeedAttr.getValue() + " (base: " +
                        attackSpeedAttr.getBaseValue() + ")");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Rhythm activated");
            }

            // Cancel any existing task
            BukkitTask existingTask = battleRhythmTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Save a reference to the modifier to remove it later
            final AttributeModifier finalMod = attackSpeedMod;

            // Schedule task to remove the buff
            BukkitTask newTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Remove the modifier after 5 seconds
                attackSpeedAttr.removeModifier(finalMod);

                battleRhythmTasks.remove(playerId);

                // REMOVED the action bar notification completely, even in debug mode
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Battle Rhythm expired for " + player.getName());
                    plugin.getLogger().info("Removed attack speed modifier, current speed: " +
                            attackSpeedAttr.getValue());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Rhythm expired");
                }
            }, 100L); // 5 seconds = 100 ticks

            battleRhythmTasks.put(playerId, newTask);
        }

        // Check for Battle Fury skill (For every 100 damage dealt, +1% damage for 5s, stack to 3)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(6)) {
            // Track cumulative damage to reach 100 thresholds
            double currentDamage = battleFuryDamageCounter.getOrDefault(playerId, 0.0) + damage;
            battleFuryDamageCounter.put(playerId, currentDamage);

            // Calculate how many new stacks we should add (each 100 damage = 1 stack)
            int newStacksToAdd = (int)(currentDamage / 100.0);

            if (newStacksToAdd > 0) {
                // Reduce the counter by the amount we used for new stacks
                battleFuryDamageCounter.put(playerId, currentDamage % 100.0);

                // Add stacks up to max of 3
                int currentStacks = battleFuryStacks.getOrDefault(playerId, 0);
                int newStacks = Math.min(3, currentStacks + newStacksToAdd);
                battleFuryStacks.put(playerId, newStacks);

                // Apply the fury damage bonus directly to this attack!
                double bonusMultiplier = 0.01 * newStacks; // 1% per stack
                double newDamage = event.getDamage() * (1.0 + bonusMultiplier);
                event.setDamage(newDamage);

                // Only notify when reaching max stacks for the first time
                if (newStacks == 3 && currentStacks < 3) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Battle Fury MAX! (3/3 stacks, +3% DMG)");
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " now has " + newStacks + "/3 Battle Fury stacks");
                    plugin.getLogger().info("Battle Fury applied: +" + (bonusMultiplier * 100) + "% damage");
                    plugin.getLogger().info("Original damage: " + event.getDamage() + ", New damage: " + newDamage);
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Fury at " + newStacks + " stacks. Damage: " + newDamage);
                }

                // Cancel any existing task
                BukkitTask existingTask = battleFuryTasks.get(playerId);
                if (existingTask != null) {
                    existingTask.cancel();
                }

                // Schedule task to remove all stacks
                BukkitTask newTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    int oldStacks = battleFuryStacks.getOrDefault(playerId, 0);
                    battleFuryStacks.put(playerId, 0);
                    battleFuryTasks.remove(playerId);

                    // Notify when losing stacks
                    if (oldStacks == 3) {
                        ActionBarUtils.sendActionBar(player,
                                ChatColor.GRAY + "Battle Fury stacks expired");
                    }

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Battle Fury stacks expired for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Battle Fury stacks expired");
                    }
                }, 100L); // 5 seconds = 100 ticks

                battleFuryTasks.put(playerId, newTask);
            } else if (battleFuryStacks.getOrDefault(playerId, 0) > 0) {
                // If we already have stacks but didn't add more, still apply the bonus
                int stacks = battleFuryStacks.get(playerId);
                double bonusMultiplier = 0.01 * stacks; // 1% per stack
                double newDamage = event.getDamage() * (1.0 + bonusMultiplier);
                event.setDamage(newDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Using existing Battle Fury stacks: " + stacks);
                    plugin.getLogger().info("Applied bonus: +" + (bonusMultiplier * 100) + "% damage");
                    plugin.getLogger().info("Original damage: " + event.getDamage() + ", New damage: " + newDamage);
                }
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Life Drain skill (After killing a mob, heal 2hp)
        // Minor heal, don't show notification to player
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(12)) {
            double oldHealth = player.getHealth();
            player.setHealth(Math.min(player.getHealth() + 2, player.getMaxHealth()));
            double healthGained = player.getHealth() - oldHealth;

            if (healthGained > 0 && debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " healed " + healthGained + " HP from Life Drain");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Life Drain healed " + healthGained + " HP");
            }
        }
    }

    /**
     * Apply Battle Fury damage bonus if active
     * @param player The player
     * @param damage The base damage
     * @return The modified damage with Battle Fury applied if active
     */
    public double applyBattleFuryDamage(Player player, double damage) {
        UUID playerId = player.getUniqueId();
        int stacks = battleFuryStacks.getOrDefault(playerId, 0);

        if (stacks > 0) {
            double bonusMultiplier = 0.01 * stacks; // 1% per stack
            double newDamage = damage * (1.0 + bonusMultiplier);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied Battle Fury bonus: +" + (bonusMultiplier * 100) + "% damage");
                plugin.getLogger().info("Original damage: " + damage + ", New damage: " + newDamage);
            }

            return newDamage;
        }

        return damage;
    }

    /**
     * Clear any tasks and cooldowns for a player (call when they logout)
     */
    public void clearPlayerData(UUID playerId) {
        // Cancel active tasks
        BukkitTask rhythmTask = battleRhythmTasks.remove(playerId);
        if (rhythmTask != null) {
            rhythmTask.cancel();
        }

        BukkitTask furyTask = battleFuryTasks.remove(playerId);
        if (furyTask != null) {
            furyTask.cancel();
        }

        BukkitTask heartNotifyTask = dragonHeartNotifyTasks.remove(playerId);
        if (heartNotifyTask != null) {
            heartNotifyTask.cancel();
        }

        // Clear stored data
        battleRhythmCooldowns.remove(playerId);
        battleFuryStacks.remove(playerId);
        battleFuryDamageCounter.remove(playerId);
        dragonHeartCooldowns.remove(playerId);

        // Find player and remove attribute modifiers if online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            // Remove Battle Rhythm modifier
            AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            if (attackSpeedAttr != null) {
                attackSpeedAttr.getModifiers().stream()
                        .filter(mod -> mod.getName().equals("skill.battlerhythm"))
                        .forEach(mod -> attackSpeedAttr.removeModifier(mod));
            }
        }
    }
}
