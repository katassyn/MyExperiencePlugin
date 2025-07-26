package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler for FlameWarden-specific skill effects
 */
public class FlameWardenSkillEffectsHandler extends BaseSkillEffectsHandler {
    // IDs start from 300000 for FlameWarden
    private static final int ID_OFFSET = 300000;

    // Constants
    private static final double IGNITE_CHANCE = 0.15; // 15% chance to ignite
    private static final long IGNITE_DURATION = 3000; // 3 seconds
    private static final double BURNING_DAMAGE_BONUS = 0.10; // 10% bonus damage against burning enemies
    private static final double FLAME_SHIELD_REDUCTION = 0.15; // 15% damage reduction from burning enemies
    private static final double FIRE_NOVA_CHANCE = 0.20; // 20% chance to trigger fire nova
    private static final double FIRE_NOVA_DAMAGE = 30.0; // 30 damage for fire nova
    private static final double BURNING_AURA_BONUS = 0.05; // 5% damage per burning enemy
    private static final double BURNING_AURA_MAX = 0.15; // Max 15% damage bonus
    private static final double SPREADING_FLAMES_CHANCE = 0.30; // 30% chance to spread flames
    private static final double CRITICAL_BURN_DAMAGE = 5.0; // 5 fire damage over 3 seconds
    private static final double SURROUNDED_DEFENSE_BONUS = 0.15; // 15% defense when surrounded
    private static final double SPLASH_DAMAGE_PERCENT = 0.20; // 20% splash damage
    private static final double BURNING_MOMENTUM_BONUS = 0.05; // 5% damage and speed per stack
    private static final int BURNING_MOMENTUM_MAX_STACKS = 3; // Max 3 stacks
    private static final long BURNING_MOMENTUM_DURATION = 5000; // 5 seconds
    private static final double THIRD_STRIKE_BONUS = 0.40; // 40% bonus fire damage
    private static final double DESPERATE_NOVA_COOLDOWN = 30000; // 30 seconds
    private static final double EXTENDED_BURN_CHANCE = 0.15; // 15% chance to extend burn
    private static final double BURNING_PRESENCE_DAMAGE = 0.20; // 20% damage bonus
    private static final double BURNING_PRESENCE_DEFENSE = 0.10; // 10% defense bonus
    private static final double FLAME_REFLECTION_PERCENT = 0.25; // 25% damage reflection
    private static final double HOTTER_FLAMES_BONUS = 0.30; // 30% bonus burning damage
    private static final double CRITICAL_EXPLOSION_DAMAGE = 100.0; // 100 area damage
    private static final double BURN_DAMAGE_SCALING_BONUS = 0.01; // 1% damage per second
    private static final double BURN_DAMAGE_SCALING_MAX = 0.10; // Max 10% bonus per enemy
    private static final double BURNING_PROTECTION_REDUCTION = 0.03; // 3% damage reduction per burning enemy
    private static final double BURNING_PROTECTION_MAX = 0.15; // Max 15% damage reduction
    private static final double LAST_STAND_THRESHOLD = 0.40; // 40% health threshold
    private static final double LAST_STAND_DAMAGE = 100.0; // 100 damage
    private static final double BURNING_RETALIATION_CHANCE = 0.20; // 20% chance to spread fire
    private static final double EMBRACE_THE_FLAME_BONUS = 0.25; // 25% damage while burning
    private static final double OPENING_STRIKE_CHANCE = 0.25; // 25% crit chance against high health enemies
    private static final double PHOENIX_REBIRTH_DAMAGE = 50.0; // 5000% damage (50x)
    private static final long PHOENIX_REBIRTH_COOLDOWN = 300000; // 5 minutes

    // Track burning entities
    private final Map<UUID, Map<UUID, Long>> burningEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Integer>> burnDuration = new ConcurrentHashMap<>();

    // Track player stats
    private final Map<UUID, Double> playerDamageModifier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerDefenseModifier = new ConcurrentHashMap<>();

    // Track cooldowns
    private final Map<UUID, Long> desperateNovaCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastStandCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> phoenixRebirthCooldown = new ConcurrentHashMap<>();

    // Track burning momentum stacks
    private final Map<UUID, Integer> burningMomentumStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> burningMomentumExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> burningMomentumTasks = new ConcurrentHashMap<>();

    // Track consecutive hits on the same target
    private final Map<UUID, Map<UUID, Integer>> consecutiveHits = new ConcurrentHashMap<>();

    // Random for chance calculations
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

    public FlameWardenSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // Attacks have 15% chance to ignite enemies for 3 seconds (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 1: Will apply ignite chance dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 1: 15% chance to ignite enemies enabled");
                }
                break;
            case 2: // +10% damage against burning enemies (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 2: Will apply burning damage bonus dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 2: +10% damage vs burning enemies enabled");
                }
                break;
            case 3: // Gain fire resistance potion effect infinity
                // Apply fire resistance potion effect (infinite duration, hidden)
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                ChatNotificationUtils.send(player, ChatColor.GOLD + "You feel protected from fire! (Fire Resistance effect applied)");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 3: Applied hidden fire resistance effect to " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 3: Hidden Fire Resistance effect enabled");
                }
                break;
            case 4: // When hp<50%, your attacks have +10% chance to ignite enemies
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 4: Will apply desperate ignition dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 4: +10% ignite chance when HP < 50% enabled");
                }
                break;
            case 5: // Burning enemies deal -15% damage to you (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 5: Will apply flame shield dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 5: -15% damage from burning enemies enabled");
                }
                break;
            case 6: // Taking damage has 20% chance to trigger a fire nova dealing 30 damage to nearby enemies and ignite them
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 6: Will apply fire nova dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 6: 20% chance fire nova on damage taken (30 dmg) enabled");
                }
                break;
            case 7: // +5% damage for each burning enemy within 10 blocks (max +15%)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 7: Will apply burning aura dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 7: +5% damage per burning enemy (max +15%) enabled");
                }
                break;
            case 8: // Ignited enemies spread burn to nearby enemies within 3 blocks (30% chance)
                // This is handled dynamically when enemies are ignited
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 8: Will apply spreading flames dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 8: 30% chance to spread flames to nearby enemies enabled");
                }
                break;
            case 9: // Critical hits deal additional 5 fire damage over 3 seconds (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 9: Will apply critical burn dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 9: Critical hits deal +5 fire damage over 3s enabled");
                }
                break;
            case 10: // When surrounded by 3+ enemies, gain +15% defense
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 10: Will apply surrounded defense dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 10: +15% defense when surrounded by 3+ enemies enabled");
                }
                break;
            case 11: // Attacks deal splash damage (20% of damage) to enemies within 2 blocks of target (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 11: Will apply splash damage dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 11: 20% splash damage to enemies within 2 blocks enabled");
                }
                break;
            case 12: // After killing a burning enemy, gain +5% damage and movement speed for 5 seconds (stacks up to 3 times)
                // This is handled dynamically when killing enemies
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 12: Will apply burning momentum dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 12: +5% damage/speed per burning enemy kill (max 3 stacks) enabled");
                }
                break;
            case 13: // Standing in fire heals you instead of dealing damage
                // This is handled dynamically when taking fire damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 13: Will apply fire healing dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 13: Fire heals instead of damages enabled");
                }
                break;
            case 14: // Every third attack on the same enemy deals +40% damage as fire damage
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 14: Will apply third strike dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 14: Every 3rd attack on same enemy +40% fire damage enabled");
                }
                break;
            case 15: // When hp<30%, ignite all enemies within 5 blocks (30 second cooldown)
                // This is handled dynamically when health is low
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 15: Will apply desperate nova dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 15: Ignite all enemies within 5 blocks when HP < 30% enabled (30s cd)");
                }
                break;
            case 16: // +15% chance for burning duration to extend by 2 seconds whenever the enemy takes damage
                // This is handled dynamically when enemies take damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 16: Will apply extended burn dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] FLAMEWARDEN SKILL 16: 15% chance to extend burn duration by 2s enabled");
                }
                break;
            case 17: // While you have 3+ burning enemies nearby, gain +20% damage and +10% defense
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 17: Will apply burning presence dynamically");
                }
                break;
            case 18: // When blocking an attack, reflect 25% of damage as fire damage (1/2)
                // This is handled dynamically when blocking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 18: Will apply flame reflection dynamically");
                }
                break;
            case 19: // Your fires burn 30% hotter (enemies take +30% burning damage)
                // This is handled dynamically when enemies take burning damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 19: Will apply hotter flames dynamically");
                }
                break;
            case 20: // Critical hits create a fire explosion dealing 100 area damage (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 20: Will apply critical explosion dynamically");
                }
                break;
            case 21: // +1% damage for each second an enemy burns (max +10% per enemy)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 21: Will apply burn damage scaling dynamically");
                }
                break;
            case 22: // Each burning enemy reduces your damage taken by 3% (max 15%)
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 22: Will apply burning protection dynamically");
                }
                break;
            case 23: // After taking a hit that reduces you below 40% hp, emit a fire nova dealing 100 damage to all nearby enemies and burn them
                // This is handled dynamically when taking damage
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 23: Will apply last stand dynamically");
                }
                break;
            case 24: // Burning enemies have 20% chance to spread fire to their attackers
                // This is handled dynamically when burning enemies are attacked
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 24: Will apply burning retaliation dynamically");
                }
                break;
            case 25: // You deal +25% damage while burning yourself after you burn enemy
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 25: Will apply embrace the flame dynamically");
                }
                break;
            case 26: // Attacks against enemies above 80% hp have +25% chance to critically hit (1/2)
                // This is handled dynamically in combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 26: Will apply opening strike dynamically");
                }
                break;
            case 27: // When killed, explode in flames dealing massive damage 5000% to nearby enemies and igniting them (5 min cooldown)
                // This is handled dynamically when player dies
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("FLAMEWARDEN SKILL 27: Will apply phoenix rebirth dynamically");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown FlameWarden skill ID: " + skillId);
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

        // Check for Fire Healing (ID 13)
        if (isPurchased(playerId, ID_OFFSET + 13) && 
                (event.getCause() == EntityDamageEvent.DamageCause.FIRE || 
                 event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || 
                 event.getCause() == EntityDamageEvent.DamageCause.LAVA)) {

            // Cancel the damage event
            event.setCancelled(true);

            // Heal the player instead (10% of the damage that would have been dealt)
            double healAmount = event.getDamage() * 0.1;
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);
            player.setHealth(newHealth);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Fire Healing activated for " + player.getName() + 
                        ", healed for " + healAmount);
            }
            return;
        }

        // Check for Fire Nova (ID 6)
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            boolean success = rollChance(FIRE_NOVA_CHANCE * 100, player, "Fire Nova");
            
            if (success) {
                triggerFireNova(player, FIRE_NOVA_DAMAGE);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Fire Nova triggered, damage: " + FIRE_NOVA_DAMAGE);
                }
            }
        }

        // Check for Desperate Nova (ID 15)
        if (isPurchased(playerId, ID_OFFSET + 15) && healthPercent < 30 && 
                !isOnCooldown(playerId, desperateNovaCooldown, DESPERATE_NOVA_COOLDOWN)) {

            // Ignite all enemies within 5 blocks
            List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;
                    igniteEntity(player, target, IGNITE_DURATION);
                }
            }

            // Set cooldown
            desperateNovaCooldown.put(playerId, System.currentTimeMillis());

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Desperate Nova Activated!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Desperate Nova activated for " + player.getName() + 
                        " at " + healthPercent + "% health");
            }
        }

        // Check for Last Stand (ID 23)
        if (isPurchased(playerId, ID_OFFSET + 23) && 
                healthPercent < LAST_STAND_THRESHOLD * 100 && 
                !isOnCooldown(playerId, lastStandCooldown, DESPERATE_NOVA_COOLDOWN)) {

            // Trigger a powerful fire nova
            triggerFireNova(player, LAST_STAND_DAMAGE);

            // Set cooldown
            lastStandCooldown.put(playerId, System.currentTimeMillis());

            // Show notification
            ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Last Stand Activated!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Last Stand activated for " + player.getName() + 
                        " at " + healthPercent + "% health");
            }
        }

        // Apply Burning Protection (ID 22)
        if (isPurchased(playerId, ID_OFFSET + 22)) {
            int burningEnemiesCount = countBurningEnemiesNearby(player, 10);
            double damageReduction = Math.min(BURNING_PROTECTION_MAX, 
                    burningEnemiesCount * BURNING_PROTECTION_REDUCTION);

            if (damageReduction > 0) {
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * (1 - damageReduction);
                event.setDamage(reducedDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Burning Protection reduced damage for " + player.getName() + 
                            " by " + (damageReduction * 100) + "% (" + originalDamage + " → " + reducedDamage + ")");
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Get target entity
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getEntity();
        UUID targetId = target.getUniqueId();

        // Get current health percentage
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double healthPercent = (currentHealth / maxHealth) * 100;

        // Get target health percentage
        double targetMaxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double targetHealth = target.getHealth();
        double targetHealthPercent = (targetHealth / targetMaxHealth) * 100;

        // Check if target is burning
        boolean isTargetBurning = isEntityBurning(player, target);

        // Apply damage bonus against burning enemies (ID 2)
        if (isPurchased(playerId, ID_OFFSET + 2) && isTargetBurning) {
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 2);
            double damageBonus = BURNING_DAMAGE_BONUS * purchaseCount;
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * (1 + damageBonus);
            event.setDamage(bonusDamage);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied burning damage bonus for " + player.getName() + 
                        ": " + originalDamage + " → " + bonusDamage + " (+" + (damageBonus * 100) + "%)");
            }
        }

        // Apply Burning Aura (ID 7)
        if (isPurchased(playerId, ID_OFFSET + 7)) {
            int burningEnemiesCount = countBurningEnemiesNearby(player, 10);
            double damageBonus = Math.min(BURNING_AURA_MAX, burningEnemiesCount * BURNING_AURA_BONUS);

            // Send chat message with burning enemies count
            ChatNotificationUtils.send(player, ChatColor.GOLD + "[Burning Aura] " + burningEnemiesCount + " burning enemies nearby");

            if (damageBonus > 0) {
                double originalDamage = event.getDamage();
                double bonusDamage = originalDamage * (1 + damageBonus);
                event.setDamage(bonusDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Burning Aura added " + (damageBonus * 100) + "% damage for " + 
                            player.getName() + " (" + burningEnemiesCount + " burning enemies nearby)");
                }
            }
        }

        // Apply Burning Presence (ID 17)
        if (isPurchased(playerId, ID_OFFSET + 17)) {
            int burningEnemiesCount = countBurningEnemiesNearby(player, 10);
            
            // Send chat message with burning enemies count
            ChatNotificationUtils.send(player, ChatColor.GOLD + "[Burning Presence] " + burningEnemiesCount + " burning enemies nearby" +
                    (burningEnemiesCount >= 3 ? " - ACTIVE!" : " - Need 3+ for activation"));

            if (burningEnemiesCount >= 3) {
                // Apply damage and defense bonuses
                double originalDamage = event.getDamage();
                double bonusDamage = originalDamage * (1 + BURNING_PRESENCE_DAMAGE);
                event.setDamage(bonusDamage);

                // Store defense bonus to be applied when taking damage
                playerDefenseModifier.put(playerId, BURNING_PRESENCE_DEFENSE);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Burning Presence activated for " + player.getName() + 
                            ": +" + (BURNING_PRESENCE_DAMAGE * 100) + "% damage, +" + 
                            (BURNING_PRESENCE_DEFENSE * 100) + "% defense");
                }
            } else {
                // Remove defense bonus if not enough burning enemies
                playerDefenseModifier.remove(playerId);
            }
        }

        // Apply Burn Damage Scaling (ID 21)
        if (isPurchased(playerId, ID_OFFSET + 21) && isTargetBurning) {
            // Get burn duration in seconds
            int burnSeconds = getBurnDuration(player, target);
            double damageBonus = Math.min(BURN_DAMAGE_SCALING_MAX, burnSeconds * BURN_DAMAGE_SCALING_BONUS);

            if (damageBonus > 0) {
                double originalDamage = event.getDamage();
                double bonusDamage = originalDamage * (1 + damageBonus);
                event.setDamage(bonusDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Burn Damage Scaling added " + (damageBonus * 100) + "% damage for " + 
                            player.getName() + " (enemy burning for " + burnSeconds + " seconds)");
                }
            }
        }

        // Apply Embrace the Flame (ID 25)
        if (isPurchased(playerId, ID_OFFSET + 25) && player.getFireTicks() > 0) {
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * (1 + EMBRACE_THE_FLAME_BONUS);
            event.setDamage(bonusDamage);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Embrace the Flame added " + (EMBRACE_THE_FLAME_BONUS * 100) + 
                        "% damage for " + player.getName() + " (player is burning)");
            }
        }

        // Apply Third Strike (ID 14)
        if (isPurchased(playerId, ID_OFFSET + 14)) {
            // Get consecutive hits map for this player
            Map<UUID, Integer> hitsMap = consecutiveHits.computeIfAbsent(playerId, k -> new HashMap<>());

            // Get hit count for this target
            int hitCount = hitsMap.getOrDefault(targetId, 0) + 1;
            hitsMap.put(targetId, hitCount);

            // Every third hit deals bonus damage
            if (hitCount % 3 == 0) {
                double originalDamage = event.getDamage();
                double bonusDamage = originalDamage * THIRD_STRIKE_BONUS;
                event.setDamage(originalDamage + bonusDamage);

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Third Strike! +" + 
                        (int)(THIRD_STRIKE_BONUS * 100) + "% Fire Damage");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Third Strike activated for " + player.getName() + 
                            ": +" + (THIRD_STRIKE_BONUS * 100) + "% damage");
                }
            }
        }

        // Apply Splash Damage (ID 11)
        if (isPurchased(playerId, ID_OFFSET + 11)) {
            // Check if this is a splash damage event to prevent chaining
            if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
                int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 11);
                double splashPercent = SPLASH_DAMAGE_PERCENT * purchaseCount;
                double originalDamage = event.getDamage();
                double splashDamage = originalDamage * splashPercent;
    
                // Apply splash damage to nearby entities
                List<Entity> nearbyEntities = target.getNearbyEntities(2, 2, 2);
                for (Entity nearby : nearbyEntities) {
                    if (nearby instanceof LivingEntity && nearby != player && nearby != target) {
                        LivingEntity nearbyTarget = (LivingEntity) nearby;
                        // Use CUSTOM damage cause to prevent chaining
                        nearbyTarget.damage(splashDamage);
                        // Set the last damage cause to the player for proper attribution
                        nearbyTarget.setLastDamageCause(new EntityDamageByEntityEvent(player, nearbyTarget, 
                                EntityDamageEvent.DamageCause.CUSTOM, splashDamage));
    
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Splash damage of " + splashDamage + " applied to " + 
                                    nearbyTarget.getType() + " from " + player.getName() + "'s attack");
                        }
                    }
                }
    
                if (!nearbyEntities.isEmpty()) {
                    // Show notification
                    ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Splash Damage! (" + nearbyEntities.size() + " entities)");
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Splash Damage applied to " + nearbyEntities.size() + 
                                " entities for " + player.getName());
                    }
                }
            }
        }

        // Determine if this is a critical hit
        boolean isCrit = false;
        double critChance = getCriticalChance(player, target, targetHealthPercent);

        if (rollChance(critChance * 100, player, "Critical Hit")) {
            isCrit = true;

            // Apply Critical Burn (ID 9)
            if (isPurchased(playerId, ID_OFFSET + 9)) {
                int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 9);
                double burnDamage = CRITICAL_BURN_DAMAGE * purchaseCount;

                // Apply burning effect
                applyBurningEffect(player, target, burnDamage);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Critical Burn applied to " + target.getType() + 
                            " for " + burnDamage + " damage over 3 seconds");
                }
            }

            // Apply Critical Explosion (ID 20)
            if (isPurchased(playerId, ID_OFFSET + 20)) {
                int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 20);
                double explosionDamage = CRITICAL_EXPLOSION_DAMAGE * purchaseCount;

                // Create explosion effect
                target.getWorld().createExplosion(target.getLocation(), 0, false, false, player);

                // Damage nearby entities
                List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
                for (Entity nearby : nearbyEntities) {
                    if (nearby instanceof LivingEntity && nearby != player) {
                        LivingEntity nearbyTarget = (LivingEntity) nearby;
                        nearbyTarget.damage(explosionDamage, player);
                        igniteEntity(player, nearbyTarget, IGNITE_DURATION);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Critical Explosion damage of " + explosionDamage + 
                                    " applied to " + nearbyTarget.getType());
                        }
                    }
                }

                // Show notification
                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Critical Explosion!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Critical Explosion triggered for " + player.getName() + 
                            " dealing " + explosionDamage + " damage to " + nearbyEntities.size() + " entities");
                }
            }
        }

        // Apply Ignite Chance (ID 1)
        double igniteChance = 0;

        if (isPurchased(playerId, ID_OFFSET + 1)) {
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 1);
            igniteChance += IGNITE_CHANCE * purchaseCount;
        }

        // Apply Desperate Ignition (ID 4)
        if (isPurchased(playerId, ID_OFFSET + 4) && healthPercent < 50) {
            igniteChance += 0.10; // +10% chance when below 50% health
        }

        // Check if ignite should be applied
        if (igniteChance > 0) {
            boolean success = rollChance(igniteChance * 100, player, "Ignite");
            
            if (success) {
                igniteEntity(player, target, IGNITE_DURATION);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Ignited " + target.getType() + " for " + (IGNITE_DURATION/1000) + "s");
                }

                // Apply Spreading Flames (ID 8)
                if (isPurchased(playerId, ID_OFFSET + 8)) {
                    boolean spreadSuccess = rollChance(SPREADING_FLAMES_CHANCE * 100, player, "Spreading Flames");
                    
                    if (spreadSuccess) {
                        List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
                        int spreadCount = 0;
                        for (Entity nearby : nearbyEntities) {
                            if (nearby instanceof LivingEntity && nearby != player && nearby != target) {
                                LivingEntity nearbyTarget = (LivingEntity) nearby;
                                igniteEntity(player, nearbyTarget, IGNITE_DURATION);
                                spreadCount++;
                            }
                        }
                        
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("→ Spreading Flames ignited " + spreadCount + " entities");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        LivingEntity entity = event.getEntity();

        // Check if the killed entity was burning
        boolean wasEntityBurning = isEntityBurning(player, entity);

        // Apply Burning Momentum (ID 12)
        if (isPurchased(playerId, ID_OFFSET + 12) && wasEntityBurning) {
            // Update stack count
            int currentStacks = burningMomentumStacks.getOrDefault(playerId, 0);
            if (currentStacks < BURNING_MOMENTUM_MAX_STACKS) {
                currentStacks++;
                burningMomentumStacks.put(playerId, currentStacks);

                // Apply bonuses
                applyBurningMomentumBonuses(player, currentStacks);

                // Show notification at max stacks
                if (currentStacks == BURNING_MOMENTUM_MAX_STACKS) {
                    ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Burning Momentum MAX! (" + 
                            BURNING_MOMENTUM_MAX_STACKS + " stacks)");
                }
            }

            // Update expiry time
            burningMomentumExpiry.put(playerId, System.currentTimeMillis() + BURNING_MOMENTUM_DURATION);

            // Cancel existing task
            BukkitTask existingTask = burningMomentumTasks.get(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule stack removal
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Check if stacks are still active
                Long expiry = burningMomentumExpiry.get(playerId);
                if (expiry != null && System.currentTimeMillis() >= expiry) {
                    int oldStacks = burningMomentumStacks.getOrDefault(playerId, 0);
                    burningMomentumStacks.remove(playerId);
                    burningMomentumExpiry.remove(playerId);
                    burningMomentumTasks.remove(playerId);

                    // Remove bonuses
                    removeBurningMomentumBonuses(player, oldStacks);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Burning Momentum stacks expired for " + player.getName());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Burning Momentum stacks expired");
                        }
                    }
                }
            }, BURNING_MOMENTUM_DURATION / 50); // Convert ms to ticks

            burningMomentumTasks.put(playerId, task);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Burning Momentum stack added for " + player.getName() + 
                        ": " + currentStacks + "/" + BURNING_MOMENTUM_MAX_STACKS + " stacks");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Burning Momentum: " + 
                        currentStacks + "/" + BURNING_MOMENTUM_MAX_STACKS + " stacks (+" + 
                        (currentStacks * BURNING_MOMENTUM_BONUS * 100) + "% dmg/speed)");
            }
        }

        // Remove entity from burning entities map
        Map<UUID, Long> playerBurningEntities = burningEntities.get(playerId);
        if (playerBurningEntities != null) {
            playerBurningEntities.remove(entity.getUniqueId());
        }

        // Remove entity from burn duration map
        Map<UUID, Integer> playerBurnDuration = burnDuration.get(playerId);
        if (playerBurnDuration != null) {
            playerBurnDuration.remove(entity.getUniqueId());
        }

        // Remove entity from consecutive hits map
        Map<UUID, Integer> hitsMap = consecutiveHits.get(playerId);
        if (hitsMap != null) {
            hitsMap.remove(entity.getUniqueId());
        }
    }

    /**
     * Apply periodic effects for FlameWarden
     */
    public void applyPeriodicEffects() {
        // Update burn durations and check for extended burns
        for (UUID playerId : burningEntities.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;

            Map<UUID, Long> playerBurningEntities = burningEntities.get(playerId);
            Map<UUID, Integer> playerBurnDuration = burnDuration.computeIfAbsent(playerId, k -> new HashMap<>());

            // Check each burning entity
            for (UUID entityId : new HashSet<>(playerBurningEntities.keySet())) {
                Entity entity = Bukkit.getEntity(entityId);
                if (entity == null || !entity.isValid() || !(entity instanceof LivingEntity)) {
                    playerBurningEntities.remove(entityId);
                    playerBurnDuration.remove(entityId);
                    continue;
                }

                LivingEntity target = (LivingEntity) entity;
                long burnEndTime = playerBurningEntities.get(entityId);

                // Check if burn has expired
                if (System.currentTimeMillis() > burnEndTime) {
                    playerBurningEntities.remove(entityId);
                    playerBurnDuration.remove(entityId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Burn expired on " + target.getType() + " for player " + player.getName());
                    }
                    continue;
                }

                // Increment burn duration
                int seconds = playerBurnDuration.getOrDefault(entityId, 0) + 1;
                playerBurnDuration.put(entityId, seconds);

                // Apply Extended Burn (ID 16)
                if (isPurchased(playerId, ID_OFFSET + 16)) {
                    boolean success = rollChance(EXTENDED_BURN_CHANCE * 100, player, "Extended Burn");
                    
                    if (success) {
                        long newEndTime = burnEndTime + 2000;
                        playerBurningEntities.put(entityId, newEndTime);
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("→ Extended burn duration by 2 seconds");
                        }
                    }
                }

                // Apply burning damage
                double baseDamage = 1.0; // Base burning damage per second

                // Apply Hotter Flames (ID 19)
                if (isPurchased(playerId, ID_OFFSET + 19)) {
                    baseDamage *= (1 + HOTTER_FLAMES_BONUS);
                }

                target.damage(baseDamage, player);

                // Apply Burning Retaliation (ID 24)
                if (isPurchased(playerId, ID_OFFSET + 24) && target.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) target.getLastDamageCause();
                    if (damageEvent.getDamager() instanceof LivingEntity && damageEvent.getDamager() != player) {
                        boolean success = rollChance(BURNING_RETALIATION_CHANCE * 100, player, "Burning Retaliation");
                        
                        if (success) {
                            LivingEntity attacker = (LivingEntity) damageEvent.getDamager();
                            igniteEntity(player, attacker, IGNITE_DURATION);
                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("→ Burning Retaliation ignited " + attacker.getType());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if player is standing in fire for Fire Healing (ID 13)
     */
    public void checkFireHealing() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // Skip if player doesn't have Fire Healing
            if (!isPurchased(playerId, ID_OFFSET + 13)) continue;

            // Check if player is standing in fire or lava
            Block block = player.getLocation().getBlock();
            if (block.getType() == Material.FIRE || block.getType() == Material.LAVA) {
                // Heal player
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = player.getHealth();
                double healAmount = 1.0; // Heal 1 health per check

                if (currentHealth < maxHealth) {
                    player.setHealth(Math.min(maxHealth, currentHealth + healAmount));

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Fire Healing healed " + player.getName() + 
                                " for " + healAmount + " while standing in " + block.getType());
                    }
                }
            }
        }
    }

    /**
     * Ignite an entity
     */
    private void igniteEntity(Player player, LivingEntity target, long duration) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Set fire ticks (1 tick = 50ms, so 20 ticks = 1 second)
        target.setFireTicks((int) (duration / 50));

        // Add to burning entities map
        Map<UUID, Long> playerBurningEntities = burningEntities.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerBurningEntities.put(targetId, System.currentTimeMillis() + duration);

        // Initialize burn duration
        Map<UUID, Integer> playerBurnDuration = burnDuration.computeIfAbsent(playerId, k -> new HashMap<>());
        playerBurnDuration.put(targetId, 0);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Ignited " + target.getType() + " for " + (duration / 1000) + 
                    " seconds by player " + player.getName());
        }
    }

    /**
     * Apply burning effect (damage over time)
     */
    private void applyBurningEffect(Player player, LivingEntity target, double totalDamage) {
        final double damagePerTick = totalDamage / 3.0; // Spread over 3 seconds
        final int durationTicks = 3 * 20; // 3 seconds
        final int tickInterval = 20; // Once per second

        // Store the task ID to cancel if needed
        final int[] taskId = {-1};

        // Schedule burning ticks
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int ticksRemaining = durationTicks;

            @Override
            public void run() {
                // Check if target is still valid
                if (target == null || target.isDead() || !target.isValid()) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }

                // Apply burn damage
                target.damage(damagePerTick, player);

                // Visual effect for burning
                target.getWorld().spawnParticle(
                        org.bukkit.Particle.FLAME,
                        target.getLocation().add(0, 1, 0),
                        10, 0.5, 0.5, 0.5, 0.1);

                // Decrement ticks
                ticksRemaining -= tickInterval;

                // Check if done
                if (ticksRemaining <= 0) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);

                    if (debuggingFlag == 1 && player.isOnline()) {
                        plugin.getLogger().info("Burning effect expired on " + target.getType());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Burning effect expired");
                    }
                }
            }
        }, tickInterval, tickInterval);
    }

    /**
     * Trigger a fire nova around the player
     */
    private void triggerFireNova(Player player, double damage) {
        // Visual effect
        player.getWorld().spawnParticle(
                org.bukkit.Particle.FLAME,
                player.getLocation(),
                50, 3, 1, 3, 0.1);

        // Damage nearby entities
        List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                igniteEntity(player, target, IGNITE_DURATION);
            }
        }

        // Show notification
        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Fire Nova!");

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Fire Nova triggered for " + player.getName() + 
                    " dealing " + damage + " damage to " + nearbyEntities.size() + " entities");
        }
    }

    /**
     * Count burning enemies near the player
     */
    private int countBurningEnemiesNearby(Player player, double radius) {
        int count = 0;
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (isEntityBurning(player, livingEntity)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Check if an entity is burning
     */
    private boolean isEntityBurning(Player player, LivingEntity entity) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Long> playerBurningEntities = burningEntities.get(playerId);

        return (playerBurningEntities != null && 
                playerBurningEntities.containsKey(entity.getUniqueId())) || 
                entity.getFireTicks() > 0;
    }

    /**
     * Get burn duration in seconds
     */
    private int getBurnDuration(Player player, LivingEntity entity) {
        UUID playerId = player.getUniqueId();
        Map<UUID, Integer> playerBurnDuration = burnDuration.get(playerId);

        return playerBurnDuration != null ? 
                playerBurnDuration.getOrDefault(entity.getUniqueId(), 0) : 0;
    }

    /**
     * Calculate critical chance based on skills
     */
    private double getCriticalChance(Player player, LivingEntity target, double targetHealthPercent) {
        UUID playerId = player.getUniqueId();
        double critChance = 0.05; // Base 5% crit chance

        // Apply Opening Strike (ID 26)
        if (isPurchased(playerId, ID_OFFSET + 26) && targetHealthPercent > 80) {
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 26);
            critChance += OPENING_STRIKE_CHANCE * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Opening Strike added " + (OPENING_STRIKE_CHANCE * purchaseCount * 100) + 
                        "% crit chance against " + target.getType() + " at " + targetHealthPercent + "% health");
            }
        }

        return critChance;
    }

    /**
     * Apply bonuses from Burning Momentum
     */
    private void applyBurningMomentumBonuses(Player player, int stacks) {
        UUID playerId = player.getUniqueId();
        double bonus = stacks * BURNING_MOMENTUM_BONUS;

        // Apply damage multiplier
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(bonus);

        // Store for later removal
        playerDamageModifier.put(playerId, bonus);

        // Apply movement speed bonus
        player.setWalkSpeed((float) (player.getWalkSpeed() * (1 + bonus)));

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied Burning Momentum bonuses to " + player.getName() + 
                    ": +" + (bonus * 100) + "% damage and movement speed");
        }
    }

    /**
     * Remove bonuses from Burning Momentum
     */
    private void removeBurningMomentumBonuses(Player player, int stacks) {
        UUID playerId = player.getUniqueId();
        double bonus = playerDamageModifier.getOrDefault(playerId, 0.0);

        // Remove damage multiplier
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDamageMultiplier(-bonus);

        // Remove from storage
        playerDamageModifier.remove(playerId);

        // Reset movement speed
        player.setWalkSpeed(0.2f); // Default walk speed

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Removed Burning Momentum bonuses from " + player.getName());
        }
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
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, double cooldownDuration) {
        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }

        long lastUse = cooldownMap.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownDuration;
    }

    /**
     * Handle player death for Phoenix Rebirth
     */
    public void handlePlayerDeath(PlayerDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Phoenix Rebirth (ID 27)
        if (isPurchased(playerId, ID_OFFSET + 27) && 
                !isOnCooldown(playerId, phoenixRebirthCooldown, PHOENIX_REBIRTH_COOLDOWN)) {

            // Get player's location for the explosion
            org.bukkit.Location deathLocation = player.getLocation();

            // Schedule the explosion for after respawn
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Create visual explosion effect
                deathLocation.getWorld().createExplosion(deathLocation, 0, false, false, player);

                // Create flame particles
                deathLocation.getWorld().spawnParticle(
                        org.bukkit.Particle.FLAME,
                        deathLocation,
                        100, 5, 3, 5, 0.2);

                // Apply damage to all nearby entities
                List<Entity> nearbyEntities = (List<Entity>) deathLocation.getWorld().getNearbyEntities(deathLocation, 10, 10, 10);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Calculate massive damage (5000% = 50x base damage)
                        double baseDamage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                        double phoenixDamage = baseDamage * PHOENIX_REBIRTH_DAMAGE;
                        
                        target.damage(phoenixDamage, player);
                        igniteEntity(player, target, IGNITE_DURATION * 2); // Double ignite duration

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Phoenix Rebirth dealt " + phoenixDamage + 
                                    " damage to " + target.getType());
                        }
                    }
                }

                // Set cooldown
                phoenixRebirthCooldown.put(playerId, System.currentTimeMillis());

                // Show notification to player when they respawn
                if (player.isOnline()) {
                    ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "☀ Phoenix Rebirth Activated! ☀");
                    ChatNotificationUtils.send(player, ChatColor.GOLD + "You exploded in a fiery rebirth! (5 minute cooldown)");
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Phoenix Rebirth activated for " + player.getName() + 
                            " upon death, dealing massive damage to " + nearbyEntities.size() + " entities");
                }
            }, 1L); // Execute 1 tick after death
        }
    }

    /**
     * Clean up all player data
     */
    public void clearPlayerData(UUID playerId) {
        // Clear all maps
        burningEntities.remove(playerId);
        burnDuration.remove(playerId);
        playerDamageModifier.remove(playerId);
        playerDefenseModifier.remove(playerId);
        desperateNovaCooldown.remove(playerId);
        lastStandCooldown.remove(playerId);
        phoenixRebirthCooldown.remove(playerId);
        burningMomentumStacks.remove(playerId);
        burningMomentumExpiry.remove(playerId);

        BukkitTask task = burningMomentumTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }

        consecutiveHits.remove(playerId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all FlameWarden data for player ID: " + playerId);
        }
    }
}
