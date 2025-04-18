package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
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

    public RangerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +1% movement speed
                stats.setMovementSpeedBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 1: Set movement speed bonus to " + (1 * purchaseCount) + "%");
                }
                break;
            case 2: // Nature's Recovery - Gain Regeneration I
                stats.setHasRegenerationEffect(true);
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
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 4: Set evade chance to " + (2 * purchaseCount) + "%");
                }
                break;
            case 5: // +1 HP
                stats.setMaxHealthBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 5: Set max health bonus to " + (1 * purchaseCount));
                }
                break;
            case 6: // +3$ per killed mob - FIXED VALUE
                stats.setGoldPerKill(3);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 6: Set gold per kill to EXACTLY 3");
                }
                break;
            case 8: // +1% evade chance (1/2)
                stats.addEvadeChance(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 8: Added " + (1 * purchaseCount) + "% to evade chance");
                }
                break;
            case 9: // +1% luck (1/2)
                stats.setLuckBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 9: Set luck bonus to " + (1 * purchaseCount) + "%");
                }
                break;
            case 10: // each 3 hits deals +10 dmg - handled by a more complex system
                stats.setHasTripleStrike(true);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 10: Set triple strike flag");
                }
                break;
            case 11: // +1% dmg (1/3)
                stats.setDamageMultiplier(1.0 + (0.01 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 11: Set damage multiplier to " + (1.0 + (0.01 * purchaseCount)));
                }
                break;
            case 12: // Wind Mastery: +2 max stacks of wind
                stats.setMaxWindStacks(5); // Default 3 + 2 from Wind Mastery
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 12: Set max wind stacks to 5");
                }
                break;
            case 13: // +1% def (1/2)
                stats.setDefenseBonus(1 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 13: Set defense bonus to " + (1 * purchaseCount) + "%");
                }
                break;
            case 14: // +4% evade chance, -2% dmg
                stats.addEvadeChance(4 * purchaseCount);
                stats.multiplyDamageMultiplier(1.0 - (0.02 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("RANGER SKILL 14: Added " + (4 * purchaseCount) + "% evade and reduced damage by " + (2 * purchaseCount) + "%");
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
            stats.loseWindStack();

            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.RED + "Lost a Wind Stack due to damage! Current stacks: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
                plugin.getLogger().info("Player " + player.getName() + " lost Wind Stack. Current: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
            }
        }

        // Apply evade chance
        if (stats.getEvadeChance() > 0 && random.nextDouble() * 100 < stats.getEvadeChance()) {
            event.setCancelled(true);
            player.sendMessage("§a§oYou evaded the attack!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " evaded attack with " + stats.getEvadeChance() + "% chance");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Handle Triple Strike
        if (stats.hasTripleStrike()) {
            UUID playerId = player.getUniqueId();
            UUID targetId = event.getEntity().getUniqueId();

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

                if (debuggingFlag == 1) {
                    player.sendMessage(ChatColor.GREEN + "Triple Strike! +10 damage dealt!");
                    plugin.getLogger().info("Triple Strike activated for " + player.getName() +
                            ", adding 10 extra damage. Total damage: " + event.getDamage());
                }
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Wind Stacks skill
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(7)) {
            stats.addWindStack();

            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.GREEN + "Wind Stack added! Current stacks: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
                plugin.getLogger().info("Player " + player.getName() + " gained Wind Stack. Current: " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
            }
        }

        // Apply gold per kill bonus
        if (stats.getGoldPerKill() > 0) {
            plugin.moneyRewardHandler.depositMoney(player, stats.getGoldPerKill());
            player.sendMessage("§6+" + stats.getGoldPerKill() + "$ from trophy hunter skill!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " received " +
                        stats.getGoldPerKill() + "$ from Trophy Hunter skill");
            }
        }
    }

    /**
     * Periodic check for Wind Stacks effects
     */
    public void checkWindStacksEffects(Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Check if stacks have expired
        if (stats.getWindStacks() > 0 && stats.hasWindStacksExpired()) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Wind Stacks expired for " + player.getName());
            }
            stats.setWindStacks(0);
            return;
        }

        // Apply effects based on current stack count
        if (stats.getWindStacks() > 0) {
            // Nothing to do here as the wind stack bonuses
            // are already calculated in the PlayerSkillStats object
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Wind Stacks active for " + player.getName() + ": " +
                        stats.getWindStacks() + "/" + stats.getMaxWindStacks());
            }
        }
    }
}