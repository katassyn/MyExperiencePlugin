package com.maks.myexperienceplugin.Class.skills.effects;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles SpellWeaver-specific skill effects
 */
public class SpellWeaverSkillEffectsHandler extends BaseSkillEffectsHandler {
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

    // Track spell damage modifiers
    private final Map<UUID, Double> spellDamageBonus = new ConcurrentHashMap<>();
    private final Map<UUID, Double> spellDamageMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> spellCriticalChance = new ConcurrentHashMap<>();

    // Ability cooldowns
    private final Map<UUID, Long> arcaneStrikeCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> manaShieldCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> moltenShieldCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> frostBoltCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcaneExplosionCooldowns = new ConcurrentHashMap<>();

    // Ability states
    private final Map<UUID, Boolean> arcaneStrikeReady = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> manaShieldActive = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> moltenShieldActive = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> frostBoltReady = new ConcurrentHashMap<>();

    // Hit counters and targets
    private final Map<UUID, Integer> consecutiveHitCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> frostNovaCounter = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastTargetMap = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Integer>> spellFocusStacks = new ConcurrentHashMap<>();

    // Tasks for timers
    private final Map<UUID, BukkitTask> moltenShieldTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> arcaneMomentumTasks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> notificationTasks = new ConcurrentHashMap<>();

    // Cache for affected entities
    private final Map<UUID, Long> frostedEntities = new ConcurrentHashMap<>();
    private final Set<UUID> explosionInProgress = new HashSet<>();

    public SpellWeaverSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        switch (skillId) {
            case 3: // +1% spell dmg
                stats.setSpellDamageMultiplier(1.0 + (0.01 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 3: Set spell damage multiplier to " +
                            (1.0 + (0.01 * purchaseCount)));
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 3: +" + (1 * purchaseCount) + "% spell damage");
                }
                break;
            case 7: // Fire Resistance
                stats.setHasFireResistance(true); // Dedicated fire resistance flag
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 7: Set fire resistance effect flag");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 7: Fire Resistance enabled");
                }
                break;
            case 8: // +1% chance to double spell damage
                stats.setSpellCriticalChance(purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 8: Set spell critical chance to " + purchaseCount + "%");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 8: +" + purchaseCount + "% spell critical chance");
                }
                break;
            case 10: // +2 spell dmg
                stats.setSpellDamageBonus(2);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 10: Set spell damage bonus to 2");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 10: +2 spell damage");
                }
                break;
            case 11: // +1% spell dmg (1/2)
                stats.multiplySpellDamageMultiplier(1.0 + (0.01 * purchaseCount));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 11: Added " + (0.01 * purchaseCount) +
                            " to spell damage multiplier");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 11: +" + (1 * purchaseCount) + "% spell damage multiplier");
                }
                break;
            case 13: // +5 spell dmg
                stats.addSpellDamageBonus(5);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SPELLWEAVER SKILL 13: Added 5 to spell damage bonus");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] SPELLWEAVER SKILL 13: +5 spell damage bonus");
                }
                break;
        }
    }
    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Handle Mana Shield (Skill 2) - 10% chance to reduce next hit by 25%
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(2) &&
                !manaShieldCooldowns.containsKey(playerId)) {

            if (rollChance(10, player, "Mana Shield")) {
                // Calculate damage reduction
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * 0.75; // 25% reduction
                event.setDamage(reducedDamage);

                // Set cooldown for 10 seconds
                manaShieldCooldowns.put(playerId, System.currentTimeMillis());
                manaShieldActive.put(playerId, true);

                // Show notification for important defensive ability
                ActionBarUtils.sendActionBar(player,
                        ChatColor.AQUA + "Mana Shield! (" + (int)((originalDamage - reducedDamage)) + " blocked)");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " activated Mana Shield, reducing damage from " +
                            originalDamage + " to " + reducedDamage);
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Mana Shield activated! " +
                            originalDamage + " → " + reducedDamage + " dmg");
                }

                // Schedule task to remove cooldown after 10 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    manaShieldCooldowns.remove(playerId);
                    manaShieldActive.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Mana Shield cooldown expired for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Mana Shield cooldown expired");
                    }
                }, 200L); // 10 seconds = 200 ticks
            }
        }

        // Handle Molten Shield (Skill 4) - activate when below 50% health
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(4) &&
                !moltenShieldActive.containsKey(playerId) &&
                !moltenShieldCooldowns.containsKey(playerId)) {

            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;

            if (healthPercent < 50) {
                // Activate molten shield - apply absorption
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 10);
                moltenShieldActive.put(playerId, true);
                moltenShieldCooldowns.put(playerId, System.currentTimeMillis());

                // Show notification for important defensive ability
                ActionBarUtils.sendActionBar(player,
                        ChatColor.GOLD + "Molten Shield activated!");

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " activated Molten Shield at " +
                            healthPercent + "% health");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Molten Shield activated (+10 absorption)");
                }

                // Schedule task to remove the shield after 10 seconds
                BukkitTask shieldTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    moltenShieldActive.remove(playerId);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Molten Shield expired for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Molten Shield expired");
                    }
                }, 200L); // 10 seconds = 200 ticks

                moltenShieldTasks.put(playerId, shieldTask);

                // Schedule task for cooldown notification after 30 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    moltenShieldCooldowns.remove(playerId);

                    // Send ready notification
                    if (player.isOnline()) {
                        ActionBarUtils.sendActionBar(player,
                                ChatColor.GOLD + "Molten Shield ready!");
                    }

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Molten Shield cooldown expired for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Molten Shield cooldown expired");
                    }
                }, 600L); // 30 seconds = 600 ticks
            }
        }

        // Reset Arcane Momentum if taking damage (Skill 5)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(5)) {
            // Cancel existing momentum task
            BukkitTask existingTask = arcaneMomentumTasks.remove(playerId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Schedule new momentum check
            BukkitTask newTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Apply speed boost if player is still online
                if (player.isOnline()) {
                    // Apply speed boost
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED,
                            100, // 5 seconds
                            0,   // Level 1 (amplifier 0)
                            false,
                            false,
                            true
                    ));

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Applied Arcane Momentum speed boost to " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Arcane Momentum activated (+3% speed)");
                    }
                }
                arcaneMomentumTasks.remove(playerId);
            }, 100L); // 5 seconds = 100 ticks

            arcaneMomentumTasks.put(playerId, newTask);
        }

        // Reset hit counters when taking damage
        consecutiveHitCounter.remove(playerId);
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        Entity target = event.getEntity();
        UUID targetId = target.getUniqueId();

        // Check if target changed
        if (lastTargetMap.containsKey(playerId) && !lastTargetMap.get(playerId).equals(targetId)) {
            // Reset counters on target change
            consecutiveHitCounter.put(playerId, 0);
            frostNovaCounter.put(playerId, 0);

            // Reset Spell Focus stacks (Skill 12)
            if (spellFocusStacks.containsKey(playerId)) {
                spellFocusStacks.get(playerId).clear();
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " switched targets, resetting hit counters");
            }
        }

        // Update last target
        lastTargetMap.put(playerId, targetId);

        // Handle Arcane Strike (Skill 1) - every 10s next attack does +5 damage and ignores 10% armor
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(1) &&
                arcaneStrikeReady.getOrDefault(playerId, false)) {

            // Calculate spell damage for +5 base damage
            double spellDamage = calculateSpellDamage(5.0, player, stats);

            // Apply bonus damage
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage + spellDamage;

            // Apply armor penetration (simplified, as we don't have direct armor values)
            // In a real implementation, we'd modify the DamageModifier.ARMOR values
            bonusDamage += originalDamage * 0.1; // Roughly emulates 10% armor penetration

            event.setDamage(bonusDamage);
            arcaneStrikeReady.put(playerId, false);

            // Show notification for the damage increase
            ActionBarUtils.sendActionBar(player,
                    ChatColor.LIGHT_PURPLE + "Arcane Strike! +" + (int)(bonusDamage - originalDamage) + " dmg");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " used Arcane Strike, increasing damage from " +
                        originalDamage + " to " + bonusDamage);
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Arcane Strike hit! " +
                        originalDamage + " → " + bonusDamage + " dmg");
            }

            // Schedule next activation notification
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                arcaneStrikeReady.put(playerId, true);
                if (player.isOnline()) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.LIGHT_PURPLE + "Arcane Strike ready!");
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Arcane Strike ready for " + player.getName());
                    if (player.isOnline()) {
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Arcane Strike ready");
                    }
                }
            }, 200L); // 10 seconds = 200 ticks
        }

        // Handle Frost Bolt (Skill 6) - every 15s next attack applies frost
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(6) &&
                frostBoltReady.getOrDefault(playerId, false) &&
                target instanceof LivingEntity) {

            // Calculate spell damage for +7 base damage
            double spellDamage = calculateSpellDamage(7.0, player, stats);

            // Apply additional frost damage
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage + spellDamage);

            // Apply slow
            LivingEntity livingTarget = (LivingEntity) target;
            livingTarget.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    60, // 3 seconds
                    0,  // Level 1 (amplifier 0 = 10% slow)
                    false,
                    true,
                    true
            ));

            frostBoltReady.put(playerId, false);
            frostedEntities.put(targetId, System.currentTimeMillis());

            // Show notification for important ability
            ActionBarUtils.sendActionBar(player,
                    ChatColor.AQUA + "Frost Bolt! (" + (int)(event.getDamage() - originalDamage) + " dmg)");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " used Frost Bolt on " + target.getName() +
                        ", dealing " + (event.getDamage() - originalDamage) + " additional damage and slowing by 10%");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Frost Bolt hit! " +
                        originalDamage + " → " + event.getDamage() + " dmg, 10% slow for 3s");
            }

            // Schedule next activation notification
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                frostBoltReady.put(playerId, true);
                if (player.isOnline()) {
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.AQUA + "Frost Bolt ready!");
                }

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Frost Bolt ready for " + player.getName());
                    if (player.isOnline()) {
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Frost Bolt ready");
                    }
                }
            }, 300L); // 15 seconds = 300 ticks
        }

        // Handle Frost Nova (Skill 9) - every third hit slows enemy
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(9) &&
                target instanceof LivingEntity) {

            int hitCount = frostNovaCounter.getOrDefault(playerId, 0) + 1;
            frostNovaCounter.put(playerId, hitCount);

            if (hitCount >= 3) {
                // Apply stronger slow
                LivingEntity livingTarget = (LivingEntity) target;
                livingTarget.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW,
                        40, // 2 seconds
                        1,  // Level 2 (amplifier 1 = 15% slow)
                        false,
                        true,
                        true
                ));

                // Reset counter
                frostNovaCounter.put(playerId, 0);

                // Notify (minor info, don't use action bar)
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " triggered Frost Nova on " +
                            target.getName() + ", slowing by 15% for 2s");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Frost Nova triggered (15% slow for 2s)");
                }
            }
        }

        // Handle Spell Focus (Skill 12) - each hit on same target deals more damage
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(12)) {
            // Initialize spell focus map if needed
            Map<UUID, Integer> focusMap = spellFocusStacks.computeIfAbsent(playerId, k -> new HashMap<>());

            // Get current stacks and increment
            int currentStacks = focusMap.getOrDefault(targetId, 0);
            int newStacks = currentStacks + 1;
            focusMap.put(targetId, newStacks);

            // Apply damage bonus (2% per stack)
            if (currentStacks > 0) {
                double originalDamage = event.getDamage();
                double focusMultiplier = 1.0 + (currentStacks * 0.02);
                event.setDamage(originalDamage * focusMultiplier);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " has " + currentStacks +
                            " Spell Focus stacks, multiplying damage by " + focusMultiplier +
                            " (" + originalDamage + " → " + event.getDamage() + ")");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Spell Focus: " + currentStacks +
                            " stacks, damage x" + String.format("%.2f", focusMultiplier));
                }
            }
        }

        // Handle consecutive hits counter for Arcane Explosion (Skill 14)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(14) &&
                !arcaneExplosionCooldowns.containsKey(playerId) &&
                !explosionInProgress.contains(playerId)) { // Add check to prevent recursion

            int currentHits = consecutiveHitCounter.getOrDefault(playerId, 0) + 1;
            consecutiveHitCounter.put(playerId, currentHits);

            if (currentHits >= 5) {
                // Calculate spell damage for explosion
                double explosionDamage = calculateSpellDamage(10.0, player, stats);

                // Set explosion flag to prevent recursion
                explosionInProgress.add(playerId);

                try {
                    // Get all entities within 3 blocks
                    Collection<Entity> nearbyEntities = target.getWorld().getNearbyEntities(
                            target.getLocation(), 3, 3, 3);

                    int entitiesHit = 0;

                    // Apply damage to each entity ONLY ONCE
                    for (Entity nearby : nearbyEntities) {
                        if (nearby instanceof LivingEntity && !nearby.equals(player)) {
                            LivingEntity livingEntity = (LivingEntity) nearby;
                            livingEntity.damage(explosionDamage, player);
                            entitiesHit++;
                        }
                    }

                    // Reset counter and set cooldown
                    consecutiveHitCounter.put(playerId, 0);
                    arcaneExplosionCooldowns.put(playerId, System.currentTimeMillis());

                    // Show notification for important AOE ability
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.LIGHT_PURPLE + "Arcane Explosion! (" + entitiesHit + " hit)");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Player " + player.getName() + " triggered Arcane Explosion, hitting " +
                                entitiesHit + " entities for " + explosionDamage + " damage each");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Arcane Explosion! " +
                                entitiesHit + " entities hit for " + explosionDamage + " damage");
                    }
                } finally {
                    // Always remove the flag, even if an exception occurs
                    explosionInProgress.remove(playerId);
                }

                // Schedule cooldown expiration
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    arcaneExplosionCooldowns.remove(playerId);

                    if (player.isOnline()) {
                        ActionBarUtils.sendActionBar(player,
                                ChatColor.LIGHT_PURPLE + "Arcane Explosion ready!");
                    }

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Arcane Explosion cooldown expired for " + player.getName());
                        if (player.isOnline()) {
                            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Arcane Explosion cooldown expired");
                        }
                    }
                }, 300L); // 15 seconds = 300 ticks
            }
        }

        // Handle Molten Shield damage reflection (Skill 4)
        if (moltenShieldActive.getOrDefault(playerId, false) && event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();

            // Calculate reflected damage using spell damage modifiers
            double reflectedDamage = calculateSpellDamage(5.0, player, stats);

            // Reflect damage to attacker
            attacker.damage(reflectedDamage, player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " reflected " + reflectedDamage + " damage to " +
                        attacker.getName() + " via Molten Shield");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Molten Shield reflected " + reflectedDamage + " damage to " +
                        attacker.getName());
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // No specific death-triggered effects for SpellWeaver
    }

    /**
     * Apply fire resistance effect periodically
     * @param player The player to apply effects to
     */
    public void applyFireResistanceEffect(Player player) {
        UUID playerId = player.getUniqueId();
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        if (stats.hasFireResistance()) {
            // Apply Fire Resistance I effect
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    400, // 20 seconds (refreshed periodically)
                    0,   // Level 1
                    false,
                    false, // No particles
                    false  // No icon
            ));

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied Fire Resistance to " + player.getName() + " from passive skill");
            }
        }
    }

    /**
     * Initialize spell cooldowns for a player
     * Should be called when player joins or skills are first loaded
     */
    public void initializePlayerSpellCooldowns(Player player) {
        UUID playerId = player.getUniqueId();

        // Initialize Arcane Strike (Skill 1)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(1)) {
            arcaneStrikeReady.put(playerId, true);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Initialized Arcane Strike as ready for " + player.getName());
            }
        }

        // Initialize Frost Bolt (Skill 6)
        if (plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(6)) {
            frostBoltReady.put(playerId, true);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Initialized Frost Bolt as ready for " + player.getName());
            }
        }
    }

    /**
     * Clear player data when they logout
     */
    public void clearPlayerData(UUID playerId) {
        // Clear cooldowns
        arcaneStrikeCooldowns.remove(playerId);
        manaShieldCooldowns.remove(playerId);
        moltenShieldCooldowns.remove(playerId);
        frostBoltCooldowns.remove(playerId);
        arcaneExplosionCooldowns.remove(playerId);

        // Clear states
        arcaneStrikeReady.remove(playerId);
        manaShieldActive.remove(playerId);
        moltenShieldActive.remove(playerId);
        frostBoltReady.remove(playerId);

        // Clear counters
        consecutiveHitCounter.remove(playerId);
        frostNovaCounter.remove(playerId);
        lastTargetMap.remove(playerId);
        spellFocusStacks.remove(playerId);

        // Cancel tasks
        BukkitTask moltenShieldTask = moltenShieldTasks.remove(playerId);
        if (moltenShieldTask != null) {
            moltenShieldTask.cancel();
        }

        BukkitTask arcaneMomentumTask = arcaneMomentumTasks.remove(playerId);
        if (arcaneMomentumTask != null) {
            arcaneMomentumTask.cancel();
        }

        BukkitTask notificationTask = notificationTasks.remove(playerId);
        if (notificationTask != null) {
            notificationTask.cancel();
        }

        // Clear spell damage stats
        spellDamageBonus.remove(playerId);
        spellDamageMultiplier.remove(playerId);
        spellCriticalChance.remove(playerId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all SpellWeaver data for player ID: " + playerId);
        }
    }
    private double calculateSpellDamage(double baseDamage, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Apply flat spell damage bonus
        double damage = baseDamage + stats.getSpellDamageBonus();

        // Apply spell damage multiplier
        damage *= stats.getSpellDamageMultiplier();

        // Check for spell critical
        if (random.nextDouble() * 100 < stats.getSpellCriticalChance()) {
            damage *= 2.0;
            ActionBarUtils.sendActionBar(player,
                    ChatColor.LIGHT_PURPLE + "Spell Critical! x2 dmg");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " scored a Spell Critical hit, doubling damage to " + damage);
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Spell Critical! Final damage: " + damage);
            }
        }

        return damage;
    }

}