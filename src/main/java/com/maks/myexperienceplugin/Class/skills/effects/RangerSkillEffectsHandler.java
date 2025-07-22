package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handler for Ranger-specific skill effects
 */
public class RangerSkillEffectsHandler extends BaseSkillEffectsHandler {
    private final Map<UUID, Integer> hitCounters = new HashMap<>();
    private final Map<UUID, UUID> lastTargetMap = new HashMap<>();
    private final Random random = new Random();
    private final int debuggingFlag = 1; // Set to 0 in production
    
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

    public RangerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        switch (skillId) {
            case 1: // +1% movement speed
                stats.addMovementSpeedBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 1: Added " + (1 * purchaseCount) + "% movement speed bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 1: +" + (1 * purchaseCount) + "% movement speed");
                }
                break;
            case 2: // Nature's Recovery - Gain Regeneration I
                stats.setHasRegenerationEffect(true);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 2: Set regeneration effect flag");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 2: Enabled Regeneration I effect");
                }
                break;
            case 3: // +5 damage - FIXED VALUE
                stats.addBonusDamage(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 3: Added 5 bonus damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 3: +5 bonus damage");
                }
                break;
            case 4: // +2% evade chance
                stats.addEvadeChance(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 4: Added " + (2 * purchaseCount) + "% evade chance");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 4: +" + (2 * purchaseCount) + "% evade chance");
                }
                break;
            case 5: // +1 HP
                stats.addMaxHealth(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 5: Added " + (1 * purchaseCount) + " max health");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 5: +" + (1 * purchaseCount) + " max health");
                }
                break;
            case 6: // +3$ per killed mob - FIXED VALUE
                stats.addGoldPerKill(3);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 6: Added 3 gold per kill");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 6: +3 gold per kill");
                }
                break;
            case 8: // +1% evade chance (1/2)
                stats.addEvadeChance(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 8: Added " + (1 * purchaseCount) + "% to evade chance");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 8: +" + (1 * purchaseCount) + "% evade chance");
                }
                break;
            case 9: // +1% luck (1/2)
                stats.addLuckBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 9: Added " + (1 * purchaseCount) + "% luck bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 9: +" + (1 * purchaseCount) + "% luck bonus");
                }
                break;
            case 10: // each 3 hits deals +10 dmg - handled by a more complex system
                stats.setHasTripleStrike(true);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 10: Set triple strike flag");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 10: Enabled Triple Strike (+10 dmg every 3rd hit)");
                }
                break;
            case 11: // +1% dmg (1/3)
                stats.addDamageMultiplier(0.01 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 11: Added " + (0.01 * purchaseCount) + " to damage multiplier");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 11: +" + (1 * purchaseCount) + "% damage multiplier");
                }
                break;
            case 12: // Wind Mastery: +2 max stacks of wind
                stats.setMaxWindStacks(5); // Default 3 + 2 from Wind Mastery
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 12: Set max wind stacks to 5");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 12: Max Wind Stacks set to 5 (was 3)");
                }
                break;
            case 13: // +1% def (1/2)
                stats.addDefenseBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 13: Added " + (1 * purchaseCount) + "% defense bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 13: +" + (1 * purchaseCount) + "% defense bonus");
                }
                break;
            case 14: // +4% evade chance, -2% dmg
                stats.addEvadeChance(4 * purchaseCount);
                stats.multiplyDamageMultiplier(1.0 - (0.02 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 14: Added " + (4 * purchaseCount) + "% evade chance and reduced damage multiplier by " + (0.02 * purchaseCount));
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] RANGER SKILL 14: +" + (4 * purchaseCount) + "% evade chance, -" + (2 * purchaseCount) + "% damage");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown Ranger skill ID: " + skillId);
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Check Wind Stacks - stacks are lost when taking damage
        if (stats.getWindStacks() > 0 && plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId()).contains(7)) {
            int oldStacks = stats.getWindStacks();
            stats.loseWindStack();
            int newStacks = stats.getWindStacks();

            // Only notify when all stacks are lost
            if (newStacks == 0 && oldStacks > 0) {
                ActionBarUtils.sendActionBar(player,
                        ChatColor.YELLOW + "Wind Stacks lost!");
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " lost Wind Stack. Current: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Wind Stack lost. " + oldStacks + " → " + newStacks);
            }
        }

        // Apply evade chance - important defensive ability, always notify
        if (stats.getEvadeChance() > 0 && rollChance(stats.getEvadeChance(), player, "Evade")) {
            event.setCancelled(true);

            // Important survival ability - show notification
            ActionBarUtils.sendActionBar(player,
                    ChatColor.GREEN + "Evaded!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " evaded attack with " + stats.getEvadeChance() + "% chance");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Attack evaded with " + stats.getEvadeChance() + "% chance");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Handle Triple Strike - don't show notifications to player
        if (stats.hasTripleStrike()) {
            UUID playerId = player.getUniqueId();
            UUID targetId = event.getEntity().getUniqueId();

            // Check if target changed
            if (lastTargetMap.containsKey(playerId) && !lastTargetMap.get(playerId).equals(targetId)) {
                // Reset counter if target changed
                hitCounters.put(playerId, 1);
                lastTargetMap.put(playerId, targetId);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Triple Strike counter reset due to target change for " + player.getName());
                }
                return;
            }

            // Initialize or update hit counter
            int hitCount = hitCounters.getOrDefault(playerId, 0) + 1;
            hitCounters.put(playerId, hitCount);
            lastTargetMap.put(playerId, targetId);

            // Every third hit deals extra damage
            if (hitCount >= 3) {
                // Add 10 damage on third hit
                double oldDamage = event.getDamage();
                event.setDamage(oldDamage + 10.0);

                // Reset counter
                hitCounters.put(playerId, 0);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Triple Strike activated for " + player.getName() +
                            ", adding 10 extra damage. Total damage: " + event.getDamage());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Triple Strike! Damage: " + oldDamage + " → " + event.getDamage());
                }
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Wind Stacks skill - important stack-based effect
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(7)) {
            int oldStacks = stats.getWindStacks();
            stats.addWindStack();
            int newStacks = stats.getWindStacks();

            // Only notify when reaching max stacks
            if (newStacks > oldStacks && newStacks == stats.getMaxWindStacks()) {
                ActionBarUtils.sendActionBar(player,
                        ChatColor.GREEN + "Wind Stacks MAX! (" + newStacks + "/" + stats.getMaxWindStacks() + ")");
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " gained Wind Stack. Current: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Wind Stack gained. " + oldStacks + " → " + newStacks);
            }
        }

        // Apply gold per kill bonus - don't notify, minor resource gain
        if (stats.getGoldPerKill() > 0) {
            plugin.moneyRewardHandler.depositMoney(player, stats.getGoldPerKill());

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " received " +
                        stats.getGoldPerKill() + "$ from Trophy Hunter skill");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Received " + stats.getGoldPerKill() + "$ from Trophy Hunter");
            }
        }
    }

    /**
     * Periodic check for Wind Stacks effects
     */
    public void checkWindStacksEffects(Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Check if stacks have expired
        if (stats.getWindStacks() > 0 && stats.hasWindStacksExpired()) {
            // Notify when losing all stacks
            ActionBarUtils.sendActionBar(player,
                    ChatColor.GRAY + "Wind Stacks expired");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Wind Stacks expired for " + player.getName());
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Wind Stacks expired");
            }
            stats.setWindStacks(0);
            return;
        }
    }
}