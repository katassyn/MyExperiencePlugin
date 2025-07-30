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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Chronomancer-specific skill effects
 */
public class ChronomancerSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
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

    // Heal tracking for Slowed Heal (skill 11)
    private final Map<UUID, Double> healAmountThisInterval = new ConcurrentHashMap<>();
    private final Map<UUID, Long> healIntervalReset = new ConcurrentHashMap<>();

    // Rewind Death tracking (skill 21)
    private final Map<UUID, Deque<TimedLocation>> rewindDeathHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Long> rewindDeathCooldown = new ConcurrentHashMap<>();

    private static class TimedLocation {
        final Location location;
        final long timestamp;

        TimedLocation(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
    }

    // Dodge mitigation stacks for skill 23
    private final Map<UUID, Integer> dodgeMitigationStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> dodgeMitigationExpiry = new ConcurrentHashMap<>();
    // Post-dodge spell damage (skill 27)
    private final Map<UUID, Long> postDodgePowerExpiry = new ConcurrentHashMap<>();
    // Temporal Barrier tracking (skill 25)
    private final Map<UUID, Long> temporalBarrierExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> temporalBarrierCooldown = new ConcurrentHashMap<>();

    // Attack speed stacks for Rapid Strikes (skill 12)
    private final Map<UUID, Integer> attackSpeedStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> attackSpeedExpiry = new ConcurrentHashMap<>();

    // Movement speed stacks for Speed Surge (skill 2)
    private final Map<UUID, Integer> speedSurgeStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> speedSurgeExpiry = new ConcurrentHashMap<>();

    // === New mechanic tracking ===
    // Evasive Power after a successful dodge (skill 3)
    private final Map<UUID, Long> evasivePowerExpiry = new ConcurrentHashMap<>();
    // Attack Hinder stacks applied to targets (skill 4)
    private final Map<UUID, Map<UUID, Integer>> attackHinderStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> attackHinderExpiry = new ConcurrentHashMap<>();
    // Triple Slow stacks on targets (skill 5)
    private final Map<UUID, Integer> tripleSlowHitCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Integer>> tripleSlowStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> tripleSlowExpiry = new ConcurrentHashMap<>();
    // Dodge Defense stacks (skill 9)
    private final Map<UUID, Integer> dodgeDefenseStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> dodgeDefenseExpiry = new ConcurrentHashMap<>();

    // Momentum Build stacks (skill 17)
    private final Map<UUID, Integer> momentumStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> momentumExpiry = new ConcurrentHashMap<>();

    // Dodge Explosion ready flag (skill 18)
    private final Map<UUID, Long> dodgeExplosionExpiry = new ConcurrentHashMap<>();

    // Hit Evasion Boost duration (skill 19)
    private final Map<UUID, Long> hitEvasionBoostExpiry = new ConcurrentHashMap<>();

    public ChronomancerSkillEffectsHandler(MyExperiencePlugin plugin) {
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

    /**
     * Track player locations for Rewind Death.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!"Chronomancer".equals(plugin.getClassManager().getPlayerAscendancy(player.getUniqueId()))) {
            return;
        }
        Deque<TimedLocation> deque = rewindDeathHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        deque.addLast(new TimedLocation(event.getTo().clone(), now));
        while (!deque.isEmpty() && now - deque.peekFirst().timestamp > 4000) {
            deque.removeFirst();
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

        // Check for Rewind Death (skill 21)
        if (isPurchased(playerId, ID_OFFSET + 21) && player.getHealth() - event.getFinalDamage() <= 0) {
            long now = System.currentTimeMillis();
            if (rewindDeathCooldown.getOrDefault(playerId, 0L) <= now) {
                Location rewindLoc = getRewindLocation(playerId);
                if (rewindLoc != null) {
                    rewindDeathCooldown.put(playerId, now + 300000); // 5 min cooldown
                    event.setCancelled(true);
                    player.teleport(rewindLoc);
                    player.setHealth(Math.max(player.getMaxHealth() * 0.2, 1));
                    player.getWorld().spawnParticle(Particle.PORTAL, rewindLoc.add(0,1,0), 60,1,1,1,0.2);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Rewind Death activated!");
                    return;
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

        // Temporal Barrier active damage reduction (skill 25)
        if (isPurchased(playerId, ID_OFFSET + 25)) {
            long now = System.currentTimeMillis();
            if (temporalBarrierExpiry.getOrDefault(playerId, 0L) > now) {
                event.setDamage(event.getDamage() * 0.5);
            } else if (player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.15
                    && temporalBarrierCooldown.getOrDefault(playerId, 0L) <= now) {
                temporalBarrierExpiry.put(playerId, now + 5000);
                temporalBarrierCooldown.put(playerId, now + 180000);
                event.setDamage(event.getDamage() * 0.5);
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0,1,0), 40,1,1,1,0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Temporal Barrier activated!");
            }
        }

        // Apply Dodge Mitigation (skill 23)
        int stacks = dodgeMitigationStacks.getOrDefault(playerId, 0);
        long expiry = dodgeMitigationExpiry.getOrDefault(playerId, 0L);
        if (stacks > 0 && expiry > System.currentTimeMillis()) {
            double reduction = 0.05 * stacks;
            event.setDamage(event.getDamage() * (1 - reduction));
        } else if (expiry <= System.currentTimeMillis()) {
            dodgeMitigationStacks.remove(playerId);
            dodgeMitigationExpiry.remove(playerId);
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if player is the attacker
        if (event.getDamager() == player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            UUID targetId = target.getUniqueId();

            // Slow Strike (skill 1)
            if (isPurchased(playerId, ID_OFFSET + 1) && Math.random() < 0.10) {
                applySlowEffect(player, target, 3000);
            }

            // Consume Evasive Power bonus (skill 3)
            if (evasivePowerExpiry.containsKey(playerId)) {
                if (evasivePowerExpiry.get(playerId) > System.currentTimeMillis()) {
                    event.setDamage(event.getDamage() * 1.05);
                }
                evasivePowerExpiry.remove(playerId);
            }

            // Speed Surge (skill 2)
            if (isPurchased(playerId, ID_OFFSET + 2)) {
                int current = speedSurgeStacks.getOrDefault(playerId, 0);
                int stacks = Math.min(current + 1, 5);
                speedSurgeStacks.put(playerId, stacks);
                speedSurgeExpiry.put(playerId, System.currentTimeMillis() + 3000);
                SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(player);
                st.addMovementSpeedBonus((stacks - current) * 2);
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                applyMovementSpeedEffects(player, st);
            }

            // Momentum Build (skill 17)
            if (isPurchased(playerId, ID_OFFSET + 17)) {
                int current = momentumStacks.getOrDefault(playerId, 0);
                int stacks = Math.min(current + 1, 10);
                momentumStacks.put(playerId, stacks);
                momentumExpiry.put(playerId, System.currentTimeMillis() + 5000);
                SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(player);
                st.addSpellDamageBonus(stacks - current);
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            }

            // Attack Hinder (skill 4)
            if (isPurchased(playerId, ID_OFFSET + 4)) {
                Map<UUID, Integer> map = attackHinderStacks.computeIfAbsent(playerId, k -> new HashMap<>());
                Map<UUID, Long> timers = attackHinderExpiry.computeIfAbsent(playerId, k -> new HashMap<>());
                int newStacks = Math.min(map.getOrDefault(targetId, 0) + 1, 3);
                map.put(targetId, newStacks);
                timers.put(targetId, System.currentTimeMillis() + 4000);
                if (target instanceof Player) {
                    ((Player) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, newStacks - 1, false, true, true));
                }
            }

            // Triple Slow (skill 5)
            if (isPurchased(playerId, ID_OFFSET + 5)) {
                int count = tripleSlowHitCounter.getOrDefault(playerId, 0) + 1;
                if (count >= 3) {
                    count = 0;
                    Map<UUID, Integer> map = tripleSlowStacks.computeIfAbsent(playerId, k -> new HashMap<>());
                    Map<UUID, Long> timers = tripleSlowExpiry.computeIfAbsent(playerId, k -> new HashMap<>());
                    int newStacks = Math.min(map.getOrDefault(targetId, 0) + 1, 4);
                    map.put(targetId, newStacks);
                    timers.put(targetId, System.currentTimeMillis() + 3000);
                    if (target instanceof Player) {
                        ((Player) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, newStacks - 1, false, true, true));
                    }
                }
                tripleSlowHitCounter.put(playerId, count);
            }

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
                // Increase damage against slowed enemies and scale with SpellWeaver bonuses
                double increased = event.getDamage() * 1.15; // 15% more damage
                double finalDamage = calculateSpellDamage(increased, player, stats);
                event.setDamage(finalDamage);

                // Visual effect
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Flux increased damage!");
            }

            // Slow Damage Boost (skill 20)
            if (isPurchased(playerId, ID_OFFSET + 20) && isSlowed(playerId, targetId)) {
                event.setDamage(event.getDamage() * 1.05);
            }

            // Slowed Heal (skill 11)
            if (isPurchased(playerId, ID_OFFSET + 11) && isSlowed(playerId, targetId)) {
                long now = System.currentTimeMillis();
                if (now > healIntervalReset.getOrDefault(playerId, 0L)) {
                    healIntervalReset.put(playerId, now + 5000);
                    healAmountThisInterval.put(playerId, 0.0);
                }
                double healedSoFar = healAmountThisInterval.getOrDefault(playerId, 0.0);
                double maxHeal = player.getMaxHealth() * 0.05;
                if (healedSoFar < maxHeal) {
                    double heal = Math.min(player.getMaxHealth() * 0.01, maxHeal - healedSoFar);
                    player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
                    healAmountThisInterval.put(playerId, healedSoFar + heal);
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 5, 0.5,1,0.5,0.05);
                }
            }

            // Extended Slow (skill 24)
            if (isPurchased(playerId, ID_OFFSET + 24) && isSlowed(playerId, targetId)) {
                Map<UUID, Long> slowed = slowedEnemies.get(playerId);
                if (slowed != null && slowed.containsKey(targetId)) {
                    long newExpiry = slowed.get(targetId) + 1000; // extend by 1s
                    slowed.put(targetId, newExpiry);
                    if (target instanceof Player) {
                        Player tp = (Player) target;
                        int dur = (int) ((newExpiry - System.currentTimeMillis()) / 50);
                        tp.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Math.max(dur, 20), 1, false, true, true));
                    }
                }
            }

            // Rapid Strikes (skill 12)
            if (isPurchased(playerId, ID_OFFSET + 12)) {
                int stacks = Math.min(attackSpeedStacks.getOrDefault(playerId, 0) + 1, 4);
                attackSpeedStacks.put(playerId, stacks);
                attackSpeedExpiry.put(playerId, System.currentTimeMillis() + 3000);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, stacks - 1, false, true, true));
            }

            // Apply Echo (skill 18)
            if (isPurchased(playerId, ID_OFFSET + 18)) {
                // 20% chance to echo
                if (Math.random() < 0.2) {
                    // Set echo active
                    echoEffectActive.put(playerId, System.currentTimeMillis() + 500); // 0.5s duration

                    // Schedule echo damage
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        // Apply echo damage scaled with SpellWeaver bonuses
                        double echoDamage = event.getDamage() * 0.5; // 50% of original damage
                        double finalDamage = calculateSpellDamage(echoDamage, player, stats);
                        target.damage(finalDamage, player);

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
                    // Apply second hit immediately with SpellWeaver scaling
                    double paradoxDamage = event.getDamage(); // Same damage as original
                    double finalDamage = calculateSpellDamage(paradoxDamage, player, stats);
                    target.damage(finalDamage, player);

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Paradox caused a double hit!");
                }
            }

            // Time Freeze (skill 26)
            if (isPurchased(playerId, ID_OFFSET + 26) && Math.random() < 0.15) {
                if (target instanceof Player) {
                    Player tp = (Player) target;
                    tp.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 6, false, true, true));
                    tp.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20, 128, false, true, true));
                    ActionBarUtils.sendActionBar(tp, ChatColor.LIGHT_PURPLE + "Time Frozen!");
                }
                target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0,1,0), 30,0.5,1,0.5,0.05);
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
                // Increase damage against time-affected enemies with scaling
                double increased = event.getDamage() * 1.25; // 25% more damage
                double finalDamage = calculateSpellDamage(increased, player, stats);
                event.setDamage(finalDamage);

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

                    // Store damage for delayed application, scaled with SpellWeaver bonuses
                    double fractureDamage = event.getDamage() * 0.2; // 20% of original damage
                    double scaledDamage = calculateSpellDamage(fractureDamage, player, stats);
                    fractures.put(targetId, scaledDamage);
                    fractureTimers.put(targetId, System.currentTimeMillis() + 2000); // 2s delay

                    // Visual effect
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Time Fracture created!");
                }
            }

            // Cooldown Reset (skill 22)
            if (isPurchased(playerId, ID_OFFSET + 22) && Math.random() < 0.10) {
                resetSkillCooldowns(playerId);
                ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Chronomancer cooldowns reset!");
            }

            // Dodge Explosion (skill 18)
            if (dodgeExplosionExpiry.containsKey(playerId)) {
                long expiry = dodgeExplosionExpiry.remove(playerId);
                if (expiry > System.currentTimeMillis()) {
                    double base = event.getDamage() * 0.07;
                    double dmg = calculateSpellDamage(base, player, stats);
                    for (Entity e : player.getNearbyEntities(3,3,3)) {
                        if (e instanceof LivingEntity && e != player && e != target) {
                            ((LivingEntity) e).damage(dmg, player);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0,1,0), 20,0.5,1,0.5,0.05);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Dodge Explosion!");
                }
            }
        }

        // Check if player is the defender
        if (event.getEntity() == player) {
            // Apply Precognition (skill 6)
            if (isPurchased(playerId, ID_OFFSET + 6)) {
                double dodgeChance = 0.15;
                if (hitEvasionBoostExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                    dodgeChance += 0.10;
                }
                if (Math.random() < dodgeChance) {
                    // Cancel damage
                    event.setCancelled(true);

                    // Register dodge mitigation for skill 23
                    if (isPurchased(playerId, ID_OFFSET + 23)) {
                        registerDodge(playerId);
                    }

                    // Register Evasive Power and Dodge Defense buffs
                    if (isPurchased(playerId, ID_OFFSET + 3)) {
                        evasivePowerExpiry.put(playerId, System.currentTimeMillis() + 5000);
                    }
                    if (isPurchased(playerId, ID_OFFSET + 9)) {
                        int stack = Math.min(dodgeDefenseStacks.getOrDefault(playerId, 0) + 1, 3);
                        dodgeDefenseStacks.put(playerId, stack);
                        dodgeDefenseExpiry.put(playerId, System.currentTimeMillis() + 5000);
                    }

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Precognition allowed you to dodge!");
                }
            }

            // Survival Dampening (skill 8)
            if (isPurchased(playerId, ID_OFFSET + 8) && player.getHealth() <= player.getMaxHealth() * 0.5) {
                event.setDamage(event.getDamage() * 0.93);
            }

            // Apply Dodge Defense damage reduction (skill 9)
            if (dodgeDefenseStacks.containsKey(playerId)) {
                long expiry = dodgeDefenseExpiry.getOrDefault(playerId, 0L);
                if (expiry > System.currentTimeMillis()) {
                    int stack = dodgeDefenseStacks.get(playerId);
                    event.setDamage(event.getDamage() * (1 - 0.05 * stack));
                } else {
                    dodgeDefenseStacks.remove(playerId);
                    dodgeDefenseExpiry.remove(playerId);
                }
            }

            // Weakening Slow (skill 13)
            if (isPurchased(playerId, ID_OFFSET + 13) && event.getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) event.getDamager();
                Map<UUID, Long> slowed = slowedEnemies.get(playerId);
                if (slowed != null && slowed.getOrDefault(attacker.getUniqueId(), 0L) > System.currentTimeMillis()) {
                    event.setDamage(event.getDamage() * 0.95);
                }
            }

            if (isPurchased(playerId, ID_OFFSET + 19)) {
                hitEvasionBoostExpiry.put(playerId, System.currentTimeMillis() + 3000);
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

        // Kill Heal (skill 6)
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            double heal = player.getMaxHealth() * 0.03;
            player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
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
                            double finalDamage = calculateSpellDamage(damage, player,
                                    plugin.getSkillEffectsHandler().getPlayerStats(player));
                            target.damage(finalDamage, player);

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

        // Clear expired attack speed stacks
        for (UUID id : new HashSet<>(attackSpeedStacks.keySet())) {
            if (System.currentTimeMillis() > attackSpeedExpiry.getOrDefault(id, 0L)) {
                attackSpeedStacks.remove(id);
                attackSpeedExpiry.remove(id);
            }
        }

        // Clear expired Speed Surge stacks
        for (UUID id : new HashSet<>(speedSurgeStacks.keySet())) {
            if (System.currentTimeMillis() > speedSurgeExpiry.getOrDefault(id, 0L)) {
                int stacks = speedSurgeStacks.remove(id);
                speedSurgeExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addMovementSpeedBonus(-2 * stacks);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                    applyMovementSpeedEffects(p, st);
                }
            }
        }

        // Remove expired temporal barriers
        for (UUID id : new HashSet<>(temporalBarrierExpiry.keySet())) {
            if (System.currentTimeMillis() > temporalBarrierExpiry.getOrDefault(id, 0L)) {
                temporalBarrierExpiry.remove(id);
            }
        }

        // Clear expired post-dodge power buffs
        for (UUID id : new HashSet<>(postDodgePowerExpiry.keySet())) {
            if (System.currentTimeMillis() > postDodgePowerExpiry.getOrDefault(id, 0L)) {
                postDodgePowerExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addSpellDamageBonus(-15);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                }
            }
        }

        // Clear expired Evasive Power buffs
        for (UUID id : new HashSet<>(evasivePowerExpiry.keySet())) {
            if (System.currentTimeMillis() > evasivePowerExpiry.get(id)) {
                evasivePowerExpiry.remove(id);
            }
        }

        // Clear expired Momentum Build stacks
        for (UUID id : new HashSet<>(momentumStacks.keySet())) {
            if (System.currentTimeMillis() > momentumExpiry.getOrDefault(id, 0L)) {
                int stacks = momentumStacks.remove(id);
                momentumExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addSpellDamageBonus(-stacks);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                }
            }
        }

        // Remove expired dodge explosion flags
        for (UUID id : new HashSet<>(dodgeExplosionExpiry.keySet())) {
            if (System.currentTimeMillis() > dodgeExplosionExpiry.getOrDefault(id, 0L)) {
                dodgeExplosionExpiry.remove(id);
            }
        }

        // Remove expired Hit Evasion Boost buffs
        for (UUID id : new HashSet<>(hitEvasionBoostExpiry.keySet())) {
            if (System.currentTimeMillis() > hitEvasionBoostExpiry.getOrDefault(id, 0L)) {
                hitEvasionBoostExpiry.remove(id);
            }
        }

        // Decay Attack Hinder stacks
        for (UUID pid : new HashSet<>(attackHinderStacks.keySet())) {
            Map<UUID, Integer> map = attackHinderStacks.get(pid);
            Map<UUID, Long> timers = attackHinderExpiry.get(pid);
            if (map == null || timers == null) continue;
            for (UUID tid : new HashSet<>(map.keySet())) {
                if (System.currentTimeMillis() > timers.getOrDefault(tid, 0L)) {
                    map.remove(tid);
                    timers.remove(tid);
                }
            }
            if (map.isEmpty()) {
                attackHinderStacks.remove(pid);
                attackHinderExpiry.remove(pid);
            }
        }

        // Decay Triple Slow stacks
        for (UUID pid : new HashSet<>(tripleSlowStacks.keySet())) {
            Map<UUID, Integer> map = tripleSlowStacks.get(pid);
            Map<UUID, Long> timers = tripleSlowExpiry.get(pid);
            if (map == null || timers == null) continue;
            for (UUID tid : new HashSet<>(map.keySet())) {
                if (System.currentTimeMillis() > timers.getOrDefault(tid, 0L)) {
                    map.remove(tid);
                    timers.remove(tid);
                }
            }
            if (map.isEmpty()) {
                tripleSlowStacks.remove(pid);
                tripleSlowExpiry.remove(pid);
            }
        }

        // Clear expired Dodge Defense stacks
        for (UUID id : new HashSet<>(dodgeDefenseStacks.keySet())) {
            if (System.currentTimeMillis() > dodgeDefenseExpiry.getOrDefault(id, 0L)) {
                dodgeDefenseStacks.remove(id);
                dodgeDefenseExpiry.remove(id);
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
                        // Deal damage scaled with SpellWeaver bonuses
                        double base = player.getMaxHealth() * 0.2; // 20% of player's max health
                        double finalDamage = calculateSpellDamage(base, player,
                                plugin.getSkillEffectsHandler().getPlayerStats(player));
                        target.damage(finalDamage, player);

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

    /**
     * Calculate damage for ascendancy abilities while applying SpellWeaver bonuses.
     * @param baseDamage Raw damage before Spellweaver modifiers
     * @param player     The caster
     * @param stats      Player stats containing spell damage bonuses
     * @return Final damage after applying bonuses and critical chance
     */
    private double calculateSpellDamage(double baseDamage, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        double damage = baseDamage + stats.getSpellDamageBonus();
        damage *= stats.getSpellDamageMultiplier();
        if (Math.random() * 100 < stats.getSpellCriticalChance()) {
            damage *= 2.0;
            ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Spell Critical! x2 dmg");
        }
        return damage;
    }

    private void applyMovementSpeedEffects(Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID id = player.getUniqueId();
        float base = 0.2f;
        float total = 1.0f + ((float) stats.getMovementSpeedBonus() / 100.0f);

        if (speedSurgeStacks.containsKey(id) && speedSurgeExpiry.getOrDefault(id, 0L) > System.currentTimeMillis()) {
            total += 0.02f * speedSurgeStacks.get(id);
        }

        player.setWalkSpeed(Math.min(base * total, 1.0f));
    }

    private void resetSkillCooldowns(UUID playerId) {
        temporalShiftCooldowns.remove(playerId);
        rewindCooldowns.remove(playerId);
        timeStopCooldowns.remove(playerId);
        timeLoopCooldowns.remove(playerId);
    }

    private void registerDodge(UUID playerId) {
        int stacks = Math.min(dodgeMitigationStacks.getOrDefault(playerId, 0) + 1, 3);
        dodgeMitigationStacks.put(playerId, stacks);
        dodgeMitigationExpiry.put(playerId, System.currentTimeMillis() + 4000);

        // Activate Evasive Power buff
        if (isPurchased(playerId, ID_OFFSET + 3)) {
            evasivePowerExpiry.put(playerId, System.currentTimeMillis() + 5000);
        }

        // Grant Dodge Defense stack
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            int def = Math.min(dodgeDefenseStacks.getOrDefault(playerId, 0) + 1, 3);
            dodgeDefenseStacks.put(playerId, def);
            dodgeDefenseExpiry.put(playerId, System.currentTimeMillis() + 5000);
        }

        if (isPurchased(playerId, ID_OFFSET + 27)) {
            Player p = plugin.getServer().getPlayer(playerId);
            if (p != null) {
                SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                st.addSpellDamageBonus(15);
                plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                postDodgePowerExpiry.put(playerId, System.currentTimeMillis() + 5000);
            }
        }

        if (isPurchased(playerId, ID_OFFSET + 18)) {
            dodgeExplosionExpiry.put(playerId, System.currentTimeMillis() + 5000);
        }
    }

    private Location getRewindLocation(UUID playerId) {
        Deque<TimedLocation> deque = rewindDeathHistory.get(playerId);
        if (deque == null) {
            return null;
        }
        long threshold = System.currentTimeMillis() - 3000;
        for (TimedLocation tl : deque) {
            if (tl.timestamp <= threshold) {
                return tl.location;
            }
        }
        return null;
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
        healAmountThisInterval.remove(playerId);
        healIntervalReset.remove(playerId);
        attackSpeedStacks.remove(playerId);
        attackSpeedExpiry.remove(playerId);
        dodgeMitigationStacks.remove(playerId);
        dodgeMitigationExpiry.remove(playerId);
        postDodgePowerExpiry.remove(playerId);
        evasivePowerExpiry.remove(playerId);
        attackHinderStacks.remove(playerId);
        attackHinderExpiry.remove(playerId);
        tripleSlowHitCounter.remove(playerId);
        tripleSlowStacks.remove(playerId);
        tripleSlowExpiry.remove(playerId);
        dodgeDefenseStacks.remove(playerId);
        dodgeDefenseExpiry.remove(playerId);
        momentumStacks.remove(playerId);
        momentumExpiry.remove(playerId);
        dodgeExplosionExpiry.remove(playerId);
        hitEvasionBoostExpiry.remove(playerId);
        temporalBarrierExpiry.remove(playerId);
        temporalBarrierCooldown.remove(playerId);
        speedSurgeStacks.remove(playerId);
        speedSurgeExpiry.remove(playerId);
        rewindDeathHistory.remove(playerId);
        rewindDeathCooldown.remove(playerId);

        plugin.getLogger().info("Cleared all Chronomancer data for player ID: " + playerId);
    }
}
