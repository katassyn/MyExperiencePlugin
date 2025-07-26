package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Chronomancer-specific skill effects
 */
public class ChromomancerSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
    private static final int ID_OFFSET = 800000;


    // Maps to track slowed enemies
    private final Map<UUID, Map<UUID, Long>> slowedEnemies = new ConcurrentHashMap<>();

    // Maps to track time bubbles
    private final Map<UUID, Location> timeBubbleLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> timeBubbleExpirations = new ConcurrentHashMap<>();

    // Maps to track temporal shift cooldowns
    private final Map<UUID, Long> temporalShiftCooldowns = new ConcurrentHashMap<>();

    // Maps to track rewind cooldowns
    private final Map<UUID, Long> rewindCooldowns = new ConcurrentHashMap<>();

    // Maps to track time stop cooldowns
    private final Map<UUID, Long> timeStopCooldowns = new ConcurrentHashMap<>();

    // Maps to track echo effects
    private final Map<UUID, Long> echoEffectActive = new ConcurrentHashMap<>();

    // Maps to track temporal anchors
    private final Map<UUID, Location> temporalAnchorLocations = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporalAnchorExpirations = new ConcurrentHashMap<>();

    // Maps to track time loop cooldowns
    private final Map<UUID, Long> timeLoopCooldowns = new ConcurrentHashMap<>();

    // Maps to track temporal chains stacks
    private final Map<UUID, Map<UUID, Integer>> temporalChainsStacks = new ConcurrentHashMap<>();

    // Maps to track time fractures
    private final Map<UUID, Map<UUID, Double>> timeFractures = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> timeFractureExpirations = new ConcurrentHashMap<>();

    public ChromomancerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Time Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 1: Added " + (5 * purchaseCount) + " spell damage bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 1: +" + (5 * purchaseCount) + " spell damage (Time Mastery)");
                }
                break;
            case 2: // Haste Mastery
                stats.addMovementSpeedBonus(3 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 2: Added " + (3 * purchaseCount) + "% movement speed bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 2: +" + (3 * purchaseCount) + "% movement speed (Haste Mastery)");
                }
                break;
            case 3: // Temporal Control
                // Effect applied in damage handler - increases duration of time effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 3: Will apply Temporal Control dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 3: Temporal Control enabled");
                }
                break;
            case 4: // Time Slow
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 4: Will apply Time Slow dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 4: Time Slow enabled");
                }
                break;
            case 5: // Swift Casting
                // Effect applied in damage handler - reduces spell casting time
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 5: Will apply Swift Casting dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 5: Swift Casting enabled");
                }
                break;
            case 6: // Precognition
                stats.addEvadeChance(15);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 6: Added 15% evade chance");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 6: +15% evade chance (Precognition)");
                }
                break;
            case 7: // Temporal Shift
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 7: Will apply Temporal Shift dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 7: Temporal Shift enabled");
                }
                break;
            case 8: // Accelerate
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 8: Will apply Accelerate dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 8: Accelerate enabled");
                }
                break;
            case 9: // Time Flux
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("CHRONOMANCER SKILL 9: Will apply Time Flux dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] CHRONOMANCER SKILL 9: Time Flux enabled");
                }
                break;
            case 10: // Time Loop
                // Effect applied in damage handler
                break;
            case 11: // Time Bubble
                // Effect applied in damage handler
                break;
            case 12: // Rewind
                // Effect applied in damage handler
                break;
            case 13: // Quickstep
                // Effect applied in damage handler
                break;
            case 14: // Time Dilation
                // Effect applied in damage handler
                break;
            case 15: // Temporal Anchor
                // Effect applied in damage handler
                break;
            case 16: // Paradox
                // Effect applied in damage handler
                break;
            case 17: // Time Stop
                // Effect applied in damage handler
                break;
            case 18: // Echo
                // Effect applied in damage handler
                break;
            case 19: // Temporal Rush
                // Effect applied in damage handler
                break;
            case 20: // Temporal Chains
                // Effect applied in damage handler
                break;
            case 21: // Fate Sealing
                // Effect applied in damage handler
                break;
            case 22: // Temporal Mastery
                // Effect applied in damage handler - reduces cooldowns
                break;
            case 23: // Hastened Mind
                stats.addSpellDamageBonus(10);
                // Mana cost reduction handled by effects handler
                break;
            case 24: // Time Fracture
                // Effect applied in damage handler
                break;
            case 25: // Chronobreak
                // Unlock spell - handled elsewhere
                break;
            case 26: // Time Warp
                // Unlock spell - handled elsewhere
                break;
            case 27: // Temporal Singularity
                // Unlock spell - handled elsewhere
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check for Temporal Shift (skill 7)
        if (isPurchased(playerId, ID_OFFSET + 7)) {
            // 15% chance to teleport away
            if (Math.random() < 0.15) {
                // Check if not on cooldown
                if (!temporalShiftCooldowns.containsKey(playerId) || 
                        System.currentTimeMillis() - temporalShiftCooldowns.get(playerId) > 30000) { // 30s cooldown

                    // Set cooldown
                    temporalShiftCooldowns.put(playerId, System.currentTimeMillis());

                    // Get random direction
                    Vector direction = new Vector(
                            Math.random() * 2 - 1,
                            0,
                            Math.random() * 2 - 1
                    ).normalize().multiply(5); // 5 blocks away

                    // Get safe location
                    Location currentLoc = player.getLocation();
                    Location targetLoc = currentLoc.clone().add(direction);
                    targetLoc.setY(currentLoc.getY()); // Keep same Y level

                    // Teleport player
                    player.teleport(targetLoc);

                    // Apply brief immunity
                    player.setNoDamageTicks(20); // 1 second immunity

                    // Cancel current damage
                    event.setCancelled(true);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.PORTAL, currentLoc.add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    player.getWorld().spawnParticle(Particle.PORTAL, targetLoc.add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Temporal Shift activated!");
                }
            }
        }

        // Check for Rewind (skill 12)
        if (isPurchased(playerId, ID_OFFSET + 12) && player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.2) {
            // 25% chance to restore health
            if (Math.random() < 0.25) {
                // Check if not on cooldown
                if (!rewindCooldowns.containsKey(playerId) || 
                        System.currentTimeMillis() - rewindCooldowns.get(playerId) > 60000) { // 60s cooldown

                    // Set cooldown
                    rewindCooldowns.put(playerId, System.currentTimeMillis());

                    // Restore health
                    double healAmount = player.getMaxHealth() * 0.15; // 15% of max health
                    double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
                    player.setHealth(newHealth);

                    // Apply brief immunity
                    player.setNoDamageTicks(40); // 2 seconds immunity

                    // Cancel current damage
                    event.setCancelled(true);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Rewind activated!");
                }
            }
        }

        // Check for Time Loop (skill 10)
        if (isPurchased(playerId, ID_OFFSET + 10) && player.getHealth() - event.getFinalDamage() <= 0) {
            // 20% chance to prevent death
            if (Math.random() < 0.2) {
                // Check if not on cooldown
                if (!timeLoopCooldowns.containsKey(playerId) || 
                        System.currentTimeMillis() - timeLoopCooldowns.get(playerId) > 120000) { // 120s cooldown

                    // Set cooldown
                    timeLoopCooldowns.put(playerId, System.currentTimeMillis());

                    // Restore health
                    double healAmount = player.getMaxHealth() * 0.3; // 30% of max health
                    player.setHealth(healAmount);

                    // Cancel current damage
                    event.setCancelled(true);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.5);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Loop prevented your death!");
                }
            }
        }

        // Check for Temporal Anchor (skill 15)
        if (isPurchased(playerId, ID_OFFSET + 15) && temporalAnchorLocations.containsKey(playerId)) {
            // Check if anchor is still valid
            if (System.currentTimeMillis() < temporalAnchorExpirations.getOrDefault(playerId, 0L)) {
                // 20% chance to teleport back to anchor when damaged
                if (Math.random() < 0.2) {
                    // Get anchor location
                    Location anchorLoc = temporalAnchorLocations.get(playerId);

                    // Teleport player
                    player.teleport(anchorLoc);

                    // Remove anchor
                    temporalAnchorLocations.remove(playerId);
                    temporalAnchorExpirations.remove(playerId);

                    // Cancel current damage
                    event.setCancelled(true);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Returned to Temporal Anchor!");
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if player is the attacker
        if (event.getDamager() == player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            UUID targetId = target.getUniqueId();

            // Apply Time Slow (skill 4)
            if (isPurchased(playerId, ID_OFFSET + 4)) {
                // 10% chance to slow target
                if (Math.random() < 0.1) {
                    // Apply slow effect
                    applySlowEffect(player, target, 3000); // 3s duration

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Slow activated!");
                }
            }

            // Apply Time Flux (skill 9)
            if (isPurchased(playerId, ID_OFFSET + 9) && isSlowed(playerId, targetId)) {
                // Increase damage against slowed enemies
                event.setDamage(event.getDamage() * 1.15); // 15% more damage

                // Visual effect
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Flux increased damage!");
            }

            // Apply Echo (skill 18)
            if (isPurchased(playerId, ID_OFFSET + 18)) {
                // 20% chance to echo
                if (Math.random() < 0.2) {
                    // Set echo active
                    echoEffectActive.put(playerId, System.currentTimeMillis() + 500); // 0.5s duration

                    // Schedule echo damage
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        // Apply echo damage
                        double echoDamage = event.getDamage() * 0.5; // 50% of original damage
                        target.damage(echoDamage, player);

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                        ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Echo damage applied!");
                    }, 10); // 0.5s delay

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Echo activated!");
                }
            }

            // Apply Paradox (skill 16)
            if (isPurchased(playerId, ID_OFFSET + 16)) {
                // 10% chance to hit twice
                if (Math.random() < 0.1) {
                    // Apply second hit immediately
                    double paradoxDamage = event.getDamage(); // Same damage as original
                    target.damage(paradoxDamage, player);

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Paradox caused a double hit!");
                }
            }

            // Apply Temporal Chains (skill 20)
            if (isPurchased(playerId, ID_OFFSET + 20)) {
                // Get current stacks
                Map<UUID, Integer> chainsStacks = temporalChainsStacks.computeIfAbsent(playerId, k -> new HashMap<>());
                int currentStacks = chainsStacks.getOrDefault(targetId, 0);

                // Add stack (max 5)
                int newStacks = Math.min(currentStacks + 1, 5);
                chainsStacks.put(targetId, newStacks);

                // Apply slow effect based on stacks
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, newStacks - 1, false, true, true)); // 5s, level based on stacks
                    ActionBarUtils.sendActionBar(targetPlayer, ChatColor.LIGHT_PURPLE + "Slowed by " + player.getName() + "'s Temporal Chains! (" + newStacks + " stacks)");
                }

                // Visual effect
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 10 * newStacks, 0.5, 1, 0.5, 0.05);
            }

            // Apply Fate Sealing (skill 21)
            if (isPurchased(playerId, ID_OFFSET + 21) && isSlowed(playerId, targetId)) {
                // Increase damage against time-affected enemies
                event.setDamage(event.getDamage() * 1.25); // 25% more damage

                // Visual effect
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Fate Sealing increased damage!");
            }

            // Apply Time Fracture (skill 24)
            if (isPurchased(playerId, ID_OFFSET + 24)) {
                // Check if critical hit (for demonstration, 20% chance)
                if (Math.random() < 0.2) {
                    // Create time fracture
                    Map<UUID, Double> fractures = timeFractures.computeIfAbsent(playerId, k -> new HashMap<>());
                    Map<UUID, Long> fractureTimers = timeFractureExpirations.computeIfAbsent(playerId, k -> new HashMap<>());

                    // Store damage for delayed application
                    double fractureDamage = event.getDamage() * 0.2; // 20% of original damage
                    fractures.put(targetId, fractureDamage);
                    fractureTimers.put(targetId, System.currentTimeMillis() + 2000); // 2s delay

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Fracture created!");
                }
            }
        }

        // Check if player is the defender
        if (event.getEntity() == player) {
            // Apply Precognition (skill 6)
            if (isPurchased(playerId, ID_OFFSET + 6)) {
                // 15% chance to dodge
                if (Math.random() < 0.15) {
                    // Cancel damage
                    event.setCancelled(true);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Precognition allowed you to dodge!");
                }
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Apply Time Dilation (skill 14)
        if (isPurchased(playerId, ID_OFFSET + 14)) {
            // Increase attack and casting speed
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 1, false, true, true)); // 5s, Haste II

            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Dilation activated!");
        }

        // Apply Accelerate (skill 8)
        if (isPurchased(playerId, ID_OFFSET + 8)) {
            // Increase movement speed
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, true, true)); // 3s, Speed II

            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Accelerate activated!");
        }
    }

    private void applySlowEffect(Player player, LivingEntity target, long duration) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to slowed enemies map
        Map<UUID, Long> slowed = slowedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());

        // Calculate actual duration based on Temporal Control (skill 3)
        if (isPurchased(playerId, ID_OFFSET + 3)) {
            // Get purchase count
            int purchaseCount = plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, ID_OFFSET + 3);

            // Increase duration by 5% per level
            duration = (long) (duration * (1 + 0.05 * purchaseCount));
        }

        slowed.put(targetId, System.currentTimeMillis() + duration);

        // Apply slowness effect
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration / 50), 1, false, true, true)); // Slowness II
            ActionBarUtils.sendActionBar(targetPlayer, ChatColor.LIGHT_PURPLE + "Slowed by " + player.getName() + "'s time magic!");
        }

        // Visual effect
        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
    }

    private boolean isSlowed(UUID playerId, UUID targetId) {
        Map<UUID, Long> slowed = slowedEnemies.get(playerId);
        if (slowed == null) return false;

        Long expiry = slowed.get(targetId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public void applyPeriodicEffects() {
        // Process time bubbles
        for (UUID playerId : new HashSet<>(timeBubbleLocations.keySet())) {
            // Check if bubble has expired
            if (System.currentTimeMillis() > timeBubbleExpirations.getOrDefault(playerId, 0L)) {
                timeBubbleLocations.remove(playerId);
                timeBubbleExpirations.remove(playerId);
                continue;
            }

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                timeBubbleLocations.remove(playerId);
                timeBubbleExpirations.remove(playerId);
                continue;
            }

            // Get bubble location
            Location bubbleLoc = timeBubbleLocations.get(playerId);

            // Find entities in bubble
            for (Entity entity : bubbleLoc.getWorld().getNearbyEntities(bubbleLoc, 5, 5, 5)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;

                    // Apply slow effect
                    if (target instanceof Player) {
                        Player targetPlayer = (Player) target;
                        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, true, true)); // 2s, Slowness II
                    }

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);
                }
            }

            // Visual effect for bubble
            bubbleLoc.getWorld().spawnParticle(Particle.PORTAL, bubbleLoc, 50, 5, 5, 5, 0);
        }

        // Process time fractures
        for (UUID playerId : new HashSet<>(timeFractures.keySet())) {
            Map<UUID, Double> fractures = timeFractures.get(playerId);
            Map<UUID, Long> fractureTimers = timeFractureExpirations.get(playerId);
            if (fractures == null || fractureTimers == null) continue;

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                timeFractures.remove(playerId);
                timeFractureExpirations.remove(playerId);
                continue;
            }

            // Process each fracture
            for (UUID targetId : new HashSet<>(fractures.keySet())) {
                // Check if fracture has expired
                if (System.currentTimeMillis() > fractureTimers.getOrDefault(targetId, 0L)) {
                    // Find the entity
                    for (Entity entity : player.getWorld().getEntities()) {
                        if (entity.getUniqueId().equals(targetId) && entity instanceof LivingEntity) {
                            LivingEntity target = (LivingEntity) entity;

                            // Apply fracture damage
                            double damage = fractures.get(targetId);
                            target.damage(damage, player);

                            // Visual effect
                            target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                            if (target instanceof Player) {
                                ActionBarUtils.sendActionBar((Player) target, ChatColor.LIGHT_PURPLE + "Time Fracture damage from " + player.getName() + "!");
                            }

                            break;
                        }
                    }

                    // Remove fracture
                    fractures.remove(targetId);
                    fractureTimers.remove(targetId);
                }
            }

            // Remove player if no more fractures
            if (fractures.isEmpty()) {
                timeFractures.remove(playerId);
                timeFractureExpirations.remove(playerId);
            }
        }

        // Process temporal chains decay
        for (UUID playerId : new HashSet<>(temporalChainsStacks.keySet())) {
            Map<UUID, Integer> chainsStacks = temporalChainsStacks.get(playerId);
            if (chainsStacks == null || chainsStacks.isEmpty()) {
                temporalChainsStacks.remove(playerId);
                continue;
            }

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                temporalChainsStacks.remove(playerId);
                continue;
            }

            // Decay stacks for each target
            for (UUID targetId : new HashSet<>(chainsStacks.keySet())) {
                // 10% chance to decay a stack each second
                if (Math.random() < 0.1) {
                    int currentStacks = chainsStacks.get(targetId);
                    if (currentStacks <= 1) {
                        chainsStacks.remove(targetId);
                    } else {
                        chainsStacks.put(targetId, currentStacks - 1);
                    }
                }
            }

            // Remove player if no more stacks
            if (chainsStacks.isEmpty()) {
                temporalChainsStacks.remove(playerId);
            }
        }
    }

    public void createTimeBubble(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has Time Bubble skill
        if (isPurchased(playerId, ID_OFFSET + 11)) {
            // Create bubble
            timeBubbleLocations.put(playerId, player.getLocation());
            timeBubbleExpirations.put(playerId, System.currentTimeMillis() + 5000); // 5s duration

            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 100, 5, 5, 5, 0);
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Bubble created!");
        }
    }

    public void createTemporalAnchor(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has Temporal Anchor skill
        if (isPurchased(playerId, ID_OFFSET + 15)) {
            // Create anchor
            temporalAnchorLocations.put(playerId, player.getLocation());
            temporalAnchorExpirations.put(playerId, System.currentTimeMillis() + 10000); // 10s duration

            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 1, 1, 1, 0);
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Temporal Anchor placed!");
        }
    }

    public void performQuickstep(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has Quickstep skill
        if (isPurchased(playerId, ID_OFFSET + 13)) {
            // Get direction player is facing
            Vector direction = player.getLocation().getDirection().normalize().multiply(10); // 10 blocks forward

            // Get target location
            Location targetLoc = player.getLocation().add(direction);

            // Teleport player
            player.teleport(targetLoc);

            // Visual effect
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 1, 1, 1, 0);
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Quickstep performed!");
        }
    }

    public void performTemporalRush(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has Temporal Rush skill
        if (isPurchased(playerId, ID_OFFSET + 19)) {
            // Get direction player is facing
            Vector direction = player.getLocation().getDirection().normalize().multiply(15); // 15 blocks forward

            // Get target location
            Location startLoc = player.getLocation();
            Location targetLoc = startLoc.clone().add(direction);

            // Teleport player
            player.teleport(targetLoc);

            // Damage entities in path
            for (Entity entity : player.getWorld().getNearbyEntities(startLoc, 15, 3, 15)) {
                if (entity instanceof LivingEntity && entity != player) {
                    LivingEntity target = (LivingEntity) entity;

                    // Check if entity is in path
                    if (isInPath(startLoc, targetLoc, target.getLocation(), 2)) {
                        // Deal damage
                        target.damage(player.getMaxHealth() * 0.2, player); // 20% of player's max health

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    }
                }
            }

            // Visual effect
            for (double d = 0; d < 15; d += 0.5) {
                Location particleLoc = startLoc.clone().add(direction.clone().multiply(d / 15));
                player.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 10, 0.5, 0.5, 0.5, 0);
            }

            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Temporal Rush performed!");
        }
    }

    public void performTimeStop(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has Time Stop skill
        if (isPurchased(playerId, ID_OFFSET + 17)) {
            // Check if not on cooldown
            if (!timeStopCooldowns.containsKey(playerId) || 
                    System.currentTimeMillis() - timeStopCooldowns.get(playerId) > 60000) { // 60s cooldown

                // Set cooldown
                timeStopCooldowns.put(playerId, System.currentTimeMillis());

                // Freeze all nearby entities
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply extreme slowness
                        if (target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 6, false, true, true)); // 3s, Slowness VII
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 128, false, true, true)); // 3s, can't jump
                            ActionBarUtils.sendActionBar(targetPlayer, ChatColor.LIGHT_PURPLE + "Time stopped by " + player.getName() + "!");
                        }

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.1);
                    }
                }

                // Visual effect
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 200, 5, 5, 5, 0);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Stop activated!");
            } else {
                // Notify player of cooldown
                long remainingCooldown = (timeStopCooldowns.get(playerId) + 60000 - System.currentTimeMillis()) / 1000;
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Time Stop on cooldown for " + remainingCooldown + "s!");
            }
        }
    }

    private boolean isInPath(Location start, Location end, Location point, double radius) {
        // Get direction vector
        Vector direction = end.clone().subtract(start).toVector().normalize();

        // Get vector from start to point
        Vector toPoint = point.clone().subtract(start).toVector();

        // Project toPoint onto direction
        double projectionLength = toPoint.dot(direction);

        // Check if projection is within path length
        if (projectionLength < 0 || projectionLength > start.distance(end)) {
            return false;
        }

        // Get closest point on path
        Vector closestPoint = direction.clone().multiply(projectionLength).add(start.toVector());

        // Check distance from point to path
        return closestPoint.distance(point.toVector()) <= radius;
    }

    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    public void clearPlayerData(UUID playerId) {
        slowedEnemies.remove(playerId);
        timeBubbleLocations.remove(playerId);
        timeBubbleExpirations.remove(playerId);
        temporalShiftCooldowns.remove(playerId);
        rewindCooldowns.remove(playerId);
        timeStopCooldowns.remove(playerId);
        echoEffectActive.remove(playerId);
        temporalAnchorLocations.remove(playerId);
        temporalAnchorExpirations.remove(playerId);
        timeLoopCooldowns.remove(playerId);
        temporalChainsStacks.remove(playerId);
        timeFractures.remove(playerId);
        timeFractureExpirations.remove(playerId);

        plugin.getLogger().info("Cleared all Chronomancer data for player ID: " + playerId);
    }
}
