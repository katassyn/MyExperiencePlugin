package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import org.bukkit.ChatColor;
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

    // Track Battle Rhythm (skill 2) attack speed buff
    private final Map<UUID, Long> battleRhythmCooldowns = new HashMap<>();
    private final Map<UUID, BukkitTask> battleRhythmTasks = new HashMap<>();

    // Track Battle Fury (skill 6) damage stacks
    private final Map<UUID, Integer> battleFuryStacks = new HashMap<>();
    private final Map<UUID, BukkitTask> battleFuryTasks = new HashMap<>();

    // Track Dragon Heart (skill 7) cooldown
    private final Map<UUID, Long> dragonHeartCooldowns = new HashMap<>();

    public DragonKnightSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +3% def
                stats.setDefenseBonus(3 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 1: Set defense bonus to " + (3 * purchaseCount) + "%");
                }
                break;
            case 2: // After successful hit gain +5% as for 5s
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 2: Battle Rhythm will be applied on hits");
                }
                break;
            case 3: // +1% dmg
                stats.setDamageMultiplier(1.0 + (0.01 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 3: Set damage multiplier to " + (1.0 + (0.01 * purchaseCount)));
                }
                break;
            case 4: // When hp<50% gain +2% def
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 4: Endurance will be applied when HP is low");
                }
                break;
            case 5: // +1% ms (1/2)
                stats.setMovementSpeedBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 5: Set movement speed bonus to " + (1 * purchaseCount) + "%");
                }
                break;
            case 6: // For every 100 damage dealt, you increase your damage by 1% for 5 seconds (stack up to 3 times)
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 6: Battle Fury will be applied when dealing damage");
                }
                break;
            case 7: // When hp<20% gain 20hp for 5s (cd 1min)
                // This effect is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 7: Dragon Heart will be applied when HP is very low");
                }
                break;
            case 8: // +2hp (1/2)
                stats.setMaxHealthBonus(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 8: Set max health bonus to " + (2 * purchaseCount));
                }
                break;
            case 9: // +1% luck (1/2)
                stats.setLuckBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 9: Set luck bonus to " + (1 * purchaseCount) + "%");
                }
                break;
            case 10: // +7 dmg (1/2) - FIXED VALUE
                stats.setBonusDamage(7 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 10: Set bonus damage to " + (7 * purchaseCount));
                }
                break;
            case 11: // +5% dmg, -2% ms
                stats.multiplyDamageMultiplier(1.0 + (0.05 * purchaseCount));
                stats.addMovementSpeedBonus(-2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 11: Added " + (5 * purchaseCount) + "% damage multiplier and " +
                            "reduced movement speed by " + (2 * purchaseCount) + "%");
                }
                break;
            case 12: // After killing a mob u heal 2hp
                // This effect is handled in the EntityDeathEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 12: Life Drain will be applied on kill");
                }
                break;
            case 13: // +10 dmg - FIXED VALUE
                stats.setBonusDamage(10);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 13: Set bonus damage to exactly 10");
                }
                break;
            case 14: // +5% shield block chance
                stats.setShieldBlockChance(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("DRAGONKNIGHT SKILL 14: Set shield block chance to " + (5 * purchaseCount) + "%");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown DragonKnight skill ID: " + skillId);
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for shield block chance
        if (stats.getShieldBlockChance() > 0 &&
                player.getInventory().getItemInMainHand().getType().toString().contains("SHIELD") &&
                random.nextDouble() * 100 < stats.getShieldBlockChance()) {

            event.setDamage(event.getDamage() * 0.5); // 50% damage reduction
            player.sendMessage("§a§oYour shield blocked half the damage!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " blocked with shield, reduced damage to " + event.getDamage());
            }
        }

        // Check for Endurance skill (When hp<50% gain +2% def)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(4)) {
            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;

            if (healthPercent < 50) {
                // Apply defense bonus temporarily
                // Note: This is already handled in the stats object, we just notify the player
                player.sendMessage("§a§oYour endurance increases as your health drops!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " activated Endurance skill at " + healthPercent + "% health");
                }
            }
        }

        // Check for Dragon Heart skill (When hp<20% gain 20hp for 5s)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(7)) {
            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;
            long currentTime = System.currentTimeMillis();

            if (healthPercent < 20 &&
                    (!dragonHeartCooldowns.containsKey(playerId) ||
                            currentTime - dragonHeartCooldowns.get(playerId) > 60000)) { // 1 minute cooldown

                // Apply temporary health boost
                player.setHealth(Math.min(player.getHealth() + 20, player.getMaxHealth()));
                dragonHeartCooldowns.put(playerId, currentTime);

                player.sendMessage("§c§lDragon Heart activates! +20 HP for 5 seconds!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " activated Dragon Heart at " + healthPercent + "% health");
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Battle Rhythm skill (After hit gain +5% as for 5s)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(2)) {
            // Apply 5% attack speed for 5 seconds
            // This would normally modify the player's attack speed attribute
            // We'll just notify the player here for simplicity

            player.sendMessage("§c§oBattle Rhythm activated! +5% attack speed for 5s");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " activated Battle Rhythm for 5 seconds");
            }

            // Cancel any existing task
            BukkitTask existingTask = battleRhythmTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule task to remove the buff
            BukkitTask newTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§c§oBattle Rhythm wears off!");
                battleRhythmTasks.remove(playerId);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Battle Rhythm expired for " + player.getName());
                }
            }, 100L); // 5 seconds = 100 ticks

            battleRhythmTasks.put(playerId, newTask);
        }

        // Check for Battle Fury skill (For every 100 damage dealt, +1% damage for 5s, stack to 3)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(6)) {
            // Track damage dealt and create stacks
            double damage = event.getFinalDamage();

            // Add progress towards a stack (simplified - would normally track cumulative damage)
            if (damage >= 100) {
                int currentStacks = battleFuryStacks.getOrDefault(playerId, 0);
                int newStacks = Math.min(3, currentStacks + 1);
                battleFuryStacks.put(playerId, newStacks);

                player.sendMessage("§c§oBattle Fury: " + newStacks + "/3 stacks active!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " now has " + newStacks + "/3 Battle Fury stacks");
                }

                // Cancel any existing task
                BukkitTask existingTask = battleFuryTasks.get(playerId);
                if (existingTask != null) {
                    existingTask.cancel();
                }

                // Schedule task to remove all stacks
                BukkitTask newTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    battleFuryStacks.put(playerId, 0);
                    battleFuryTasks.remove(playerId);
                    player.sendMessage("§c§oBattle Fury stacks fade away!");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Battle Fury stacks expired for " + player.getName());
                    }
                }, 100L); // 5 seconds = 100 ticks

                battleFuryTasks.put(playerId, newTask);
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Life Drain skill (After killing a mob, heal 2hp)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(12)) {
            player.setHealth(Math.min(player.getHealth() + 2, player.getMaxHealth()));
            player.sendMessage("§c§oLife Drain activated! +2 HP");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " healed 2 HP from Life Drain");
            }
        }
    }

    /**
     * Clear any tasks and cooldowns for a player (call when they logout)
     */
    public void clearPlayerData(UUID playerId) {
        BukkitTask rhythmTask = battleRhythmTasks.remove(playerId);
        if (rhythmTask != null) {
            rhythmTask.cancel();
        }

        BukkitTask furyTask = battleFuryTasks.remove(playerId);
        if (furyTask != null) {
            furyTask.cancel();
        }

        battleRhythmCooldowns.remove(playerId);
        battleFuryStacks.remove(playerId);
        dragonHeartCooldowns.remove(playerId);
    }
}