package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Elementalist-specific skill effects
 */
public class ElementalistSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
    private static final int ID_OFFSET = 700000;

    // Debug flag specifically for chance-based effects (separate from base debuggingFlag)
    private int chanceDebugFlag = 0; // Set to 0 for production

    // DAMAGE SCALING - percentage of original player damage
    private static final double AOE_DAMAGE_PERCENT = 0.10; // AoE skills do 10% of original damage
    private static final double CHAIN_DAMAGE_PERCENT = 0.15; // Chain skills do 15% of original damage
    private static final double EXPLOSION_DAMAGE_PERCENT = 0.20; // Explosions do 20% of original damage
    private static final double MAX_EXPLOSION_RADIUS = 2.5; // Reduced from 3-5 blocks
    private static final double MAX_SPELL_DAMAGE_MULTIPLIER = 2.0; // Cap spell damage multiplier

    // Safety cap - never more than 50% of target's current health in one hit
    private static final double MAX_SINGLE_HIT_PERCENT = 0.50;

    // Anti-recursion system
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    private static final long MIN_DAMAGE_INTERVAL = 50; // Minimum 50ms between damage instances

    // Prevent infinite recursion in damage events
    private final Set<String> processingDamage = ConcurrentHashMap.newKeySet();

    // Smart damage system - tracks recursion depth to prevent infinite loops
    private final ThreadLocal<Integer> skillRecursionDepth = ThreadLocal.withInitial(() -> 0);
    private final Map<UUID, Long> lastSkillDamageTime = new ConcurrentHashMap<>();
    private static final long SKILL_DAMAGE_COOLDOWN = 100; // 100ms cooldown between skill damage instances
    private static final int MAX_RECURSION_DEPTH = 2; // Allow max 2 levels of skill chains

    // Maps to track burning enemies
    private final Map<UUID, Map<UUID, Long>> burningEnemies = new ConcurrentHashMap<>();

    // Maps to track frozen enemies
    private final Map<UUID, Map<UUID, Long>> frozenEnemies = new ConcurrentHashMap<>();

    // Maps to track charged enemies
    private final Map<UUID, Map<UUID, Long>> chargedEnemies = new ConcurrentHashMap<>();

    // Maps to track stoned enemies
    private final Map<UUID, Map<UUID, Long>> stonedEnemies = new ConcurrentHashMap<>();

    // Maps to track fire shield
    private final Map<UUID, Long> fireShieldActive = new ConcurrentHashMap<>();

    // Maps to track ice barrier
    private final Map<UUID, Long> iceBarrierActive = new ConcurrentHashMap<>();

    // Maps to track spell counters
    private final Map<UUID, Integer> fireSpellCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> iceSpellCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lightningSpellCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> stoneSpellCounter = new ConcurrentHashMap<>();

    // Track entities currently exploding to prevent chain reactions
    private final Set<UUID> currentlyExploding = new HashSet<>();

    // Cooldown tracking for Combustion skill to prevent spam
    private final Map<UUID, Long> combustionLastUsed = new ConcurrentHashMap<>();

    // Location-based explosion cooldown to prevent area chain reactions
    private final Map<String, Long> explosionLocationCooldown = new ConcurrentHashMap<>();

    // Track explosions per tick to prevent server lag
    private final Map<Long, Integer> explosionsPerTick = new ConcurrentHashMap<>();

    // Track entities that have already been damaged by explosions to prevent multiple hits
    private final Map<UUID, Set<UUID>> explosionDamagedEntities = new ConcurrentHashMap<>();

    // Buff tracking for Elemental Surge (skill 26) and Stone Kill Power (skill 20)
    private final Map<UUID, Long> elementalSurgeExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> stoneKillBuffExpiry = new ConcurrentHashMap<>();

    // Resistance Break stacks (skill 18)
    private final Map<UUID, Map<UUID, Integer>> resistanceBreakStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> resistanceBreakExpiry = new ConcurrentHashMap<>();

    // === EL MD additional states ===
    private final Map<UUID, Map<UUID, Integer>> mdLightningStacks = new ConcurrentHashMap<>(); // [22]
    private final Map<UUID, Map<UUID, Integer>> mdLightningShockCount = new ConcurrentHashMap<>(); // [11]
    private final Map<UUID, Map<UUID, Integer>> mdFrostStacks = new ConcurrentHashMap<>(); // [23]
    private final Map<UUID, Map<UUID, Integer>> mdLightningVulnStacks = new ConcurrentHashMap<>(); // [18]

    private final Map<UUID, Long> mdStoneDefenseUntil = new ConcurrentHashMap<>(); // [21]
    private final Map<UUID, Long> mdStoneKillSpellBuffUntil = new ConcurrentHashMap<>(); // [20]
    private final Map<UUID, Long> mdElementalSurgeUntil = new ConcurrentHashMap<>(); // [26]
    private final Map<UUID, Double> mdLastDealtDamage = new ConcurrentHashMap<>(); // [25]

    private static final double MD_EL_LIGHTNING_BASE_PCT = 0.05;
    
    // === [MD] Elementalist (nodes 1–8) ===
    private final Map<UUID, Long> mdLightningBuffUntil = new ConcurrentHashMap<>(); // node 7 (+3% spell dmg przez 5s po LIGHTNING)

    public ElementalistSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
        // Schedule cleanup every 5 minutes
        schedulePeriodicCleanup();
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        // === [MD] Elementalist nodes dynamiczne buffy ===
        try {
            UUID pid = player.getUniqueId();
            long now = System.currentTimeMillis();

            // [26] +10% spell przez 10s po AKTYWACJI dowolnego efektu żywiołu
            if (isPurchased(pid, ID_OFFSET + 26) && mdElementalSurgeUntil.getOrDefault(pid, 0L) > now) {
                stats.multiplySpellDamageMultiplier(1.10);
            }

            // [20] Stone-kill: +7% spell / 5s
            if (isPurchased(pid, ID_OFFSET + 20) && mdStoneKillSpellBuffUntil.getOrDefault(pid, 0L) > now) {
                stats.multiplySpellDamageMultiplier(1.07);
            }

            // Node 3: Każdy AKTYWNY efekt żywiołu (Lightning/Frost/Stone) = +2% Spell DMG
            if (isPurchased(pid, ID_OFFSET + 3)) {
                int stacks = mdCountActiveElementalEffects(pid);
                if (stacks > 0) {
                    stats.multiplySpellDamageMultiplier(1.0 + (0.02 * stacks));
                }
            }

            // Node 7: Trafienia Piorunem → +3% Spell DMG przez 5s
            if (isPurchased(pid, ID_OFFSET + 7) && mdLightningBuffActive(pid)) {
                stats.multiplySpellDamageMultiplier(1.03);
            }
        } catch (Throwable ignored) { /* nie psujemy rebuildu jeśli coś nie istnieje */ }
        
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Fire Mastery
                stats.addSpellDamageBonus(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 1: Added " + (2 * purchaseCount) + " spell damage bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 1: +" + (2 * purchaseCount) + " spell damage (Fire Mastery)");
                }
                break;
            case 2: // Ice Mastery
                stats.addSpellDamageBonus(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 2: Added " + (2 * purchaseCount) + " spell damage bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 2: +" + (2 * purchaseCount) + " spell damage (Ice Mastery)");
                }
                break;
            case 3: // Lightning Mastery
                stats.addSpellDamageBonus(2 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 3: Added " + (2 * purchaseCount) + " spell damage bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 3: +" + (2 * purchaseCount) + " spell damage (Lightning Mastery)");
                }
                break;
            case 4: // Flame Burst
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 4: Will apply Flame Burst dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 4: Flame Burst enabled");
                }
                break;
            case 5: // Frost Nova
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 5: Will apply Frost Nova dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 5: Frost Nova enabled");
                }
                break;
            case 6: // Chain Lightning
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ELEMENTALIST SKILL 6: Will apply Chain Lightning dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ELEMENTALIST SKILL 6: Chain Lightning enabled");
                }
                break;
            case 7: // Heat Wave
                // Effect applied in damage handler
                break;
            case 8: // Chilling Touch
                // Effect applied in damage handler
                break;
            case 9: // Ice Barrier
                // Effect applied in damage handler
                break;
            case 10: // Static Charge
                // Effect applied in damage handler
                break;
            case 11: // Fire Shield
                // Effect applied in damage handler
                break;
            case 12: // Inferno
                // Effect applied in damage handler
                break;
            case 13: // Deep Freeze
                // Effect applied in damage handler
                break;
            case 14: // Ice Reflection
                // Effect applied in damage handler
                break;
            case 15: // Thunderstrike
                // Effect applied in damage handler
                break;
            case 16: // Electrified
                // Effect applied in damage handler
                break;
            case 17: // Phoenix Form
                // Effect applied in damage handler
                break;
            case 18: // Fire Nova
                // Effect applied in damage handler
                break;
            case 19: // Absolute Zero
                // Effect applied in damage handler
                break;
            case 20: // Overcharge
                // Effect applied in damage handler
                break;
            case 21: // Conduction
                // Effect applied in damage handler
                break;
            case 22: // Combustion
                // Effect applied in damage handler
                break;
            case 23: // Winter's Embrace
                // Effect applied in damage handler
                break;
            case 24: // Storm Shield
                // Effect applied in damage handler
                break;
            case 25: // Meteor Strike
                // Unlock spell - handled elsewhere
                break;
            case 26: // Blizzard
                // Unlock spell - handled elsewhere
                break;
            case 27: // Thunderstorm
                // Unlock spell - handled elsewhere
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        // [14] Redukcja za liczbę wrogów z efektem STONE
        if (isPurchased(playerId, ID_OFFSET + 14)) {
            int count = 0;
            Map<UUID, Long> m = stonedEnemies.get(playerId);
            if (m != null) for (Long t : m.values()) if (t != null && t > now) count++;
            if (count > 0) event.setDamage(event.getDamage() * (1 - 0.03 * Math.min(5, count)));
        }

        // [15] Każdy aktywny efekt żywiołu = +1% DR (max 5%)
        if (isPurchased(playerId, ID_OFFSET + 15)) {
            int active = 0;
            Map<UUID, Long> m1 = chargedEnemies.get(playerId); if (m1 != null) for (Long t : m1.values()) if (t != null && t > now) active++;
            Map<UUID, Long> m2 = frozenEnemies.get(playerId); if (m2 != null) for (Long t : m2.values()) if (t != null && t > now) active++;
            Map<UUID, Long> m3 = stonedEnemies.get(playerId); if (m3 != null) for (Long t : m3.values()) if (t != null && t > now) active++;
            if (active > 0) event.setDamage(event.getDamage() * (1 - 0.01 * Math.min(5, active)));
        }

        // [19] HP<30% → auto-FROST na wrogów w 4 blokach
        if (isPurchased(playerId, ID_OFFSET + 19)) {
            double hpAfter = player.getHealth() - event.getFinalDamage();
            if (hpAfter <= player.getMaxHealth() * 0.30) {
                int durTicks = 60 + (isPurchased(playerId, ID_OFFSET + 2) ? 20 : 0);
                long untilMs = System.currentTimeMillis() + durTicks * 50L;
                for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 4, 4, 4)) {
                    if (e instanceof LivingEntity && !((LivingEntity) e).isDead()) {
                        LivingEntity le = (LivingEntity) e;
                        UUID tid = le.getUniqueId();
                        frozenEnemies.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(tid, untilMs);
                        if (isPurchased(playerId, ID_OFFSET + 5)) {
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durTicks, 0, true, true, true));
                        }
                        if (isPurchased(playerId, ID_OFFSET + 8)) {
                            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, durTicks, 0, true, true, true));
                        }
                    }
                }
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                plugin.getServer().getScheduler().runTaskLater(plugin,
                        () -> plugin.getSkillEffectsHandler().refreshPlayerStats(player), durTicks);
            }
        }

        // [21] +5% DR po trafieniu kamieniem
        if (mdStoneDefenseUntil.getOrDefault(playerId, 0L) > now) {
            event.setDamage(event.getDamage() * 0.95);
        }

        // Check for Fire Shield (skill 11)
        if (isPurchased(playerId, ID_OFFSET + 11)) {
            // 15% chance to create a fire shield (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.15, player, "Fire Shield Activation", false)) {
                // Activate fire shield
                fireShieldActive.put(playerId, System.currentTimeMillis() + 3000); // 3s duration

                // Reduce damage
                event.setDamage(event.getDamage() * 0.8); // 20% damage reduction

                // Visual effect
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Fire Shield activated!");
            }
        }

        // Check for Ice Barrier (skill 9)
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            // Check if not on cooldown
            if (!iceBarrierActive.containsKey(playerId) || 
                    System.currentTimeMillis() - iceBarrierActive.get(playerId) > 30000) { // 30s cooldown

                // 20% chance to activate (improved by Elemental Affinity)
                if (rollChanceWithDebug(0.20, player, "Ice Barrier Activation", false)) {
                    // Activate ice barrier
                    iceBarrierActive.put(playerId, System.currentTimeMillis());

                    // Reduce damage
                    event.setDamage(event.getDamage() * 0.7); // 30% damage reduction

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);
                    ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Ice Barrier activated!");

                    // Check for Ice Reflection (skill 14)
                    if (isPurchased(playerId, ID_OFFSET + 14) && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                        // Reflect damage if attacker is a living entity
                        if (event instanceof EntityDamageByEntityEvent) {
                            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
                            if (edbe.getDamager() instanceof LivingEntity) {
                                LivingEntity attacker = (LivingEntity) edbe.getDamager();

                                // Deal 20% of damage back to attacker
                                double reflectDamage = event.getDamage() * 0.2;
                                double finalReflect = calculateSpellDamage(reflectDamage, player,
                                        plugin.getSkillEffectsHandler().getPlayerStats(player));
                                smartDamage(attacker, player, finalReflect, "IceBarrierReflect", true);

                                // Visual effect
                                attacker.getWorld().spawnParticle(Particle.SNOWFLAKE, attacker.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                                if (attacker instanceof Player) {
                                    ActionBarUtils.sendActionBar((Player) attacker, ChatColor.AQUA + "Damage reflected by " + player.getName() + "'s Ice Barrier!");
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check for Phoenix Form (skill 17)
        if (isPurchased(playerId, ID_OFFSET + 17) && player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.2) {
            // Check if not on cooldown
            if (!fireShieldActive.containsKey(playerId) || 
                    System.currentTimeMillis() - fireShieldActive.get(playerId) > 60000) { // 60s cooldown

                // Activate phoenix form
                fireShieldActive.put(playerId, System.currentTimeMillis());

                // Reduce damage
                event.setDamage(event.getDamage() * 0.5); // 50% damage reduction

                // Apply fire resistance
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0, false, true, true)); // 5s

                // Heal player over time
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!player.isOnline() || player.isDead()) {
                        task.cancel();
                        return;
                    }

                    // Heal 2% of max health
                    double healAmount = player.getMaxHealth() * 0.02;
                    double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
                    player.setHealth(newHealth);

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);

                    // Check if 5 seconds have passed
                    if (System.currentTimeMillis() - fireShieldActive.get(playerId) > 5000) {
                        task.cancel();
                    }
                }, 20, 20); // Every second for 5 seconds

                // Visual effect
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Phoenix Form activated!");
            }
        }

        // Check for Winter's Embrace (skill 23)
        if (isPurchased(playerId, ID_OFFSET + 23) && player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.3) {
            // Check if not on cooldown
            if (!iceBarrierActive.containsKey(playerId) || 
                    System.currentTimeMillis() - iceBarrierActive.get(playerId) > 60000) { // 60s cooldown

                // Activate winter's embrace
                iceBarrierActive.put(playerId, System.currentTimeMillis());

                // Reduce damage
                event.setDamage(event.getDamage() * 0.5); // 50% damage reduction

                // Apply resistance to slowing effects
                player.removePotionEffect(PotionEffectType.SLOW);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1, false, true, true)); // 5s, Resistance II

                // Visual effect
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 15, 1, 1, 1, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Winter's Embrace activated!");
            }
        }

        // Check for Storm Shield (skill 24)
        if (isPurchased(playerId, ID_OFFSET + 24) && player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.25) {
            // Check if not on cooldown
            if (!chargedEnemies.containsKey(playerId) || 
                    System.currentTimeMillis() - chargedEnemies.getOrDefault(playerId, new HashMap<>()).getOrDefault(playerId, 0L) > 60000) { // 60s cooldown

                // Activate storm shield
                Map<UUID, Long> charged = chargedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
                charged.put(playerId, System.currentTimeMillis());

                // Apply speed boost
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, true, true)); // 5s, Speed II

                // Increase spell damage
                SkillEffectsHandler.PlayerSkillStats playerStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                playerStats.addSpellDamageBonus(8); // 8 spell damage bonus

                // Schedule removal of spell damage bonus
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    SkillEffectsHandler.PlayerSkillStats updatedStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    updatedStats.addSpellDamageBonus(-8); // Remove the bonus
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }, 100); // 5s

                // Visual effect
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 15, 1, 1, 1, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Storm Shield activated!");
            }
        }

        // Check for Electrified (skill 16)
        if (isPurchased(playerId, ID_OFFSET + 16)) {
            // 20% chance to release a shock wave (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.20, player, "Electrified Shock Wave", false)) {
                // Find nearby enemies
                List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Deal damage based on incoming damage (Shock Wave = 75% of damage received)
                        double damage = event.getDamage() * 0.75; // 75% of damage received
                        double finalDamage = calculateSpellDamage(damage, player, stats);
                        smartDamage(target, player, finalDamage, "ShockWave", true);

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    }
                }

                // Visual effect
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 12, 1, 1, 1, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Electrified released a shock wave!");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // CRITICAL: Skip processing if we're too deep in recursion to prevent infinite loops
        if (skillRecursionDepth.get() >= MAX_RECURSION_DEPTH) {
            return;
        }

        // Check if player is the attacker
        if (event.getDamager() == player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            String damageKey = playerId + ":" + target.getUniqueId() + ":" + System.identityHashCode(event);

            // Prevent infinite recursion from AOE/chain damage
            if (processingDamage.contains(damageKey)) {
                return;
            }
            processingDamage.add(damageKey);

            // Increment recursion depth
            skillRecursionDepth.set(skillRecursionDepth.get() + 1);

            try {
                UUID targetId = target.getUniqueId();

            // === [MD] Elementalist on-hit (9–23 etc.) ===
            UUID pid = player.getUniqueId();
            UUID tid = target.getUniqueId();
            long now = System.currentTimeMillis();

            mdLastDealtDamage.put(pid, event.getDamage()); // [25]

            // [9] Trafienie celem z efektem mrozu odnawia 2% HP
            if (isPurchased(pid, ID_OFFSET + 9) && mdFrozen(pid, tid)) {
                double max = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(Math.min(max, player.getHealth() + max * 0.02));
            }

            // [13] Zamrożone cele otrzymują +7% dmg
            if (isPurchased(pid, ID_OFFSET + 13) && mdFrozen(pid, tid)) {
                event.setDamage(event.getDamage() * 1.07);
            }

            // [18] Lightning vuln -3% DR stack do 12%
            if (isPurchased(pid, ID_OFFSET + 18) && mdCharged(pid, tid)) {
                mdInc(mdLightningVulnStacks, pid, tid, 4);
            }

            if (isPurchased(pid, ID_OFFSET + 22) && mdCharged(pid, tid)) {
                int currentStacks = mdGet(mdLightningStacks, pid, tid);
                if (currentStacks < 10) { // LIMIT DO 10 STOSÓW
                    mdInc(mdLightningStacks, pid, tid, 3);
                }
            }

            // [23] Frost stacking slow +5%/stack do 20%
            if (isPurchased(pid, ID_OFFSET + 23) && mdFrozen(pid, tid) && target instanceof LivingEntity) {
                mdInc(mdFrostStacks, pid, tid, 4);
                int s = mdGet(mdFrostStacks, pid, tid);
                mdApplyMoveSpeedModifier(target, "EL_FROST_SLOW", 0.05 * s, 60);
            }

            // [21] Trafienie kamieniem: +5% DR przez 4s
            if (isPurchased(pid, ID_OFFSET + 21) && mdStoned(pid, tid)) {
                mdStoneDefenseUntil.put(pid, now + 4000);
            }

            // Node 6: Jeśli cel jest STONE → każde Twoje trafienie robi AoE = 3% aktualnego dmg (promień ~3.5)
            if (isPurchased(pid, ID_OFFSET + 6)) {
                Map<UUID, Long> stoneMap = stonedEnemies.get(pid);
                if (stoneMap != null && stoneMap.getOrDefault(tid, 0L) > now) {
                    double aoe = event.getDamage() * 0.03;
                    if (aoe > 0) {
                        mdDoAoeDamage(player, target.getLocation(), 3.5, aoe, target);
                    }
                }
            }

            // Node 1: 10% szansy na losowy efekt (Lightning / Frost / Stone), bazowo 3s
            if (isPurchased(pid, ID_OFFSET + 1) && rollChanceWithDebug(0.10, player, "Elemental Effect (Node 1)", false)) {
                int extraSec = (isPurchased(pid, ID_OFFSET + 2) ? 1 : 0);  // Node 2: +1s
                int durTicks = (3 + extraSec) * 20;
                long untilMs = System.currentTimeMillis() + (durTicks * 50L);

                int pick = (int) (Math.random() * 3); // 0:Lightning, 1:Frost, 2:Stone
                switch (pick) {
                    case 0: { // LIGHTNING
                        // Znakujemy cel jako "charged"
                        chargedEnemies.computeIfAbsent(pid, k -> new ConcurrentHashMap<>()).put(tid, untilMs);

                        // Node 7: +3% spell dmg przez 5s po LIGHTNING
                        if (isPurchased(pid, ID_OFFSET + 7)) {
                            mdLightningBuffUntil.put(pid, System.currentTimeMillis() + 5000L);
                            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                            mdRefreshStatsLater(player, 100); // po 5s
                        }

                        // Node 4: Chain na 1 dodatkowy cel w 5 blokach
        LivingEntity extra = null;
                        if (isPurchased(pid, ID_OFFSET + 4)) {
                            extra = mdFindNearestEnemy(target, 5.0, le -> !le.getUniqueId().equals(tid));
                            if (extra != null) {
                                chargedEnemies.get(pid).put(extra.getUniqueId(), untilMs);
                                extra.getWorld().spawnParticle(Particle.CRIT_MAGIC, extra.getLocation().add(0,1,0), 10, 0.5,1,0.5, 0.05);
                            }
                        }

                        // [17] Lightning chain 20% – dodatkowy skok
                        if (isPurchased(pid, ID_OFFSET + 17) && rollChanceWithDebug(0.20, player, "Lightning Chain Extra Jump", false)) {
                            LivingEntity extra2 = mdFindNearestEnemy(extra != null ? extra : target, 5.0,
                                    le -> !le.getUniqueId().equals(tid));
                            if (extra2 != null) {
                                chargedEnemies.get(pid).put(extra2.getUniqueId(), untilMs);
                            }
                        }

                        // [18] dołóż 1 stack vuln (3% DR)
                        if (isPurchased(pid, ID_OFFSET + 18)) {
                            mdInc(mdLightningVulnStacks, pid, tid, 4);
                        }

                        // [22] podbij stack lightning (0..3)
                        if (isPurchased(pid, ID_OFFSET + 22)) {
                            mdInc(mdLightningStacks, pid, tid, 3);
                        }

                        // [11] oblicz dodatkowy „zap”
                        mdInc(mdLightningShockCount, pid, tid, 999);
                        int shocks = mdGet(mdLightningShockCount, pid, tid);
                        int lst = mdGet(mdLightningStacks, pid, tid);
                        int vuln = mdGet(mdLightningVulnStacks, pid, tid);
                        double mult = 1.0 + (Math.max(0, shocks - 1) * 0.05) + (lst * 0.04) + (vuln * 0.03);
                        double zap = event.getDamage() * MD_EL_LIGHTNING_BASE_PCT * mult;
                        if (zap > 0) {
                            smartDamage(target, player, zap, "LightningZap", true);
                        }

                        // [26] aktywacja dowolnego efektu → +10% spell /10s
                        if (isPurchased(pid, ID_OFFSET + 26)) {
                            mdElementalSurgeUntil.put(pid, System.currentTimeMillis() + 10_000);
                            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                            mdRefreshStatsLater(player, 200);
                        }

                        // odśwież staty teraz i po wygaśnięciu (dla node 3)
                        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                        mdRefreshStatsLater(player, durTicks);
                        break;
                    }
                    case 1: { // FROST
                        frozenEnemies.computeIfAbsent(pid, k -> new ConcurrentHashMap<>()).put(tid, untilMs);

                        // Node 5: -10% movespeed (prostym potionem SLOW I jako bezpieczny fallback)
                        if (isPurchased(pid, ID_OFFSET + 5)) {
                            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.SLOW, durTicks, 0, true, true, true));
                        }

                        // Node 8: -10% attack speed (fallback: SLOW_DIGGING I)
                        if (isPurchased(pid, ID_OFFSET + 8)) {
                            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.SLOW_DIGGING, durTicks, 0, true, true, true));
                        }

                        // [26] aktywacja dowolnego efektu → +10% spell /10s
                        if (isPurchased(pid, ID_OFFSET + 26)) {
                            mdElementalSurgeUntil.put(pid, System.currentTimeMillis() + 10_000);
                            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                            mdRefreshStatsLater(player, 200);
                        }

                        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                        mdRefreshStatsLater(player, durTicks);
                        break;
                    }
                    default: { // STONE
                        stonedEnemies.computeIfAbsent(pid, k -> new ConcurrentHashMap<>()).put(tid, untilMs);

                        // [24] AoE 5% tego ciosu
                        mdDoAoeDamage(player, target.getLocation(), 3.5, event.getDamage() * 0.05, target);

                        // [21] DR +5% /4s
                        mdStoneDefenseUntil.put(pid, System.currentTimeMillis() + 4000);

                        // [10] 10% szansy na ogłuszenie 1s
                        if (isPurchased(pid, ID_OFFSET + 10) && rollChanceWithDebug(0.10, player, "Stone Stun", false)) {
                            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.SLOW, 20, 10, true, true, true));
                            target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                    org.bukkit.potion.PotionEffectType.JUMP, 20, 128, true, false, false));
                        }

                        // [26] aktywacja dowolnego efektu → +10% spell /10s
                        if (isPurchased(pid, ID_OFFSET + 26)) {
                            mdElementalSurgeUntil.put(pid, System.currentTimeMillis() + 10_000);
                            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                            mdRefreshStatsLater(player, 200);
                        }

                        // (node 6 AoE obsługujemy wyżej na każdym trafieniu)
                        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                        mdRefreshStatsLater(player, durTicks);
                        break;
                    }
                }
            }
            // === [/MD] Elementalist nodes 1–8 ===

            // Determine if this is a spell attack (for demonstration, we'll assume all player attacks are spells)
            boolean isSpellAttack = true;

            if (isSpellAttack) {
                // Determine spell type (fire, ice, lightning) - for demonstration, we'll randomly choose
                String spellType = getSpellType(player);

                // Increment spell counter
                incrementSpellCounter(playerId, spellType);

                // Apply spell effects based on type
                if ("fire".equals(spellType)) {
                    applyFireSpellEffects(player, target, event);
                } else if ("ice".equals(spellType)) {
                    applyIceSpellEffects(player, target, event);
                } else if ("lightning".equals(spellType)) {
                    applyLightningSpellEffects(player, target, event);
                } else if ("stone".equals(spellType)) {
                    applyStoneEffect(player, target);
                }
            }
            } finally {
                processingDamage.remove(damageKey);
                skillRecursionDepth.set(skillRecursionDepth.get() - 1);
            }
        }
    }

    private String getSpellType(Player player) {
        // For demonstration, we'll randomly choose a spell type
        // In a real implementation, this would be determined by the actual spell being cast
        int random = (int) (Math.random() * 4);
        switch (random) {
            case 0: return "fire";
            case 1: return "ice";
            case 2: return "lightning";
            default: return "stone";
        }
    }

    private void incrementSpellCounter(UUID playerId, String spellType) {
        if ("fire".equals(spellType)) {
            int count = fireSpellCounter.getOrDefault(playerId, 0) + 1;
            fireSpellCounter.put(playerId, count);

            // Check for Fire Nova (skill 18)
            if (isPurchased(playerId, ID_OFFSET + 18) && count >= 5) {
                fireSpellCounter.put(playerId, 0); // Reset counter

                // Get player
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    // Create fire nova
                    List<Entity> nearbyEntities = player.getNearbyEntities(8, 8, 8);
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof LivingEntity && entity != player) {
                            LivingEntity target = (LivingEntity) entity;

                            // Deal damage based on recent damage dealt (Fire Nova = 200% of last hit)
                            double lastDamage = mdLastDealtDamage.getOrDefault(playerId, 8.0);
                            double damage = lastDamage * 0.75; // 75% of last damage dealt
                            SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                            double finalDamage = calculateSpellDamage(damage, player, ps);
                            smartDamage(target, player, finalDamage, "FlameLash", true);

                            // Apply burning
                            applyBurning(player, target);

                            // Visual effect
                            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                        }
                    }

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 20, 2, 1, 2, 0.1);
                    ActionBarUtils.sendActionBar(player, ChatColor.RED + "Fire Nova released!");
                }
            }
        } else if ("ice".equals(spellType)) {
            int count = iceSpellCounter.getOrDefault(playerId, 0) + 1;
            iceSpellCounter.put(playerId, count);
        } else if ("lightning".equals(spellType)) {
            int count = lightningSpellCounter.getOrDefault(playerId, 0) + 1;
            lightningSpellCounter.put(playerId, count);
        } else if ("stone".equals(spellType)) {
            int count = stoneSpellCounter.getOrDefault(playerId, 0) + 1;
            stoneSpellCounter.put(playerId, count);
        }

        // Check for Elemental Mastery (skill 25) - combining all 3 elements
        if (isPurchased(playerId, ID_OFFSET + 25)) {
            checkElementalMastery(playerId);
        }
    }

    private void applyFireSpellEffects(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Check for Flame Burst (skill 4)
        if (isPurchased(playerId, ID_OFFSET + 4)) {
            // 10% chance to create explosion (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.10, player, "Flame Burst Explosion", false)) {
                // Deal additional damage
                double explosionDamage = event.getDamage() * 0.5; // 50% of original damage
                SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                double finalDamage = calculateSpellDamage(explosionDamage, player, ps);
                smartDamage(target, player, finalDamage, "FlameBurst", true);

                // Visual effect
                target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                if (target instanceof Player) {
                    ActionBarUtils.sendActionBar((Player) target, ChatColor.RED + "Hit by " + player.getName() + "'s Flame Burst!");
                }
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Flame Burst activated!");
            }
        }

        // Check for Heat Wave (skill 7)
        if (isPurchased(playerId, ID_OFFSET + 7)) {
            // Apply burning effect
            applyBurning(player, target);
        }

        // Check for Inferno (skill 12)
        if (isPurchased(playerId, ID_OFFSET + 12) && isBurning(playerId, targetId)) {
            // Increase damage against burning enemies
            event.setDamage(event.getDamage() * 1.15); // 15% more damage

            // Visual effect
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Inferno increased damage!");
        }

        // Check for Combustion (skill 22)
        if (isPurchased(playerId, ID_OFFSET + 22) && event.getDamage() > target.getHealth()) {
            // Check cooldown - 2 second cooldown to prevent spam
            if (target.isDead() || target.getHealth() <= 0) {
                return; // Nie eksploduj martwych mobów
            }
            long now = System.currentTimeMillis();
            Long lastUsed = combustionLastUsed.get(playerId);
            if (lastUsed != null && (now - lastUsed) < 2000) { // 2 second cooldown
                return; // Skip explosion due to cooldown
            }

            // Prevent chain reactions: check if this target is already exploding
            if (currentlyExploding.contains(targetId)) {
                return; // Skip explosion to prevent infinite chains
            }

            // Prevent area chain reactions: check location-based cooldown
            String locationKey = target.getWorld().getName() + ":" +
                               (int)(target.getLocation().getX() / 5) * 5 + ":" +
                               (int)(target.getLocation().getZ() / 5) * 5;
            Long lastExplosion = explosionLocationCooldown.get(locationKey);
            if (lastExplosion != null && (now - lastExplosion) < 1000) { // 1 second area cooldown
                return; // Skip explosion due to area cooldown
            }
            explosionLocationCooldown.put(locationKey, now);

            // Check explosions per tick limit to prevent server lag
            long currentTick = now / 50; // 50ms per tick
            int currentExplosions = explosionsPerTick.getOrDefault(currentTick, 0);
            if (currentExplosions >= 5) { // Max 5 explosions per tick
                return; // Skip explosion due to tick limit
            }
            explosionsPerTick.put(currentTick, currentExplosions + 1);

            // Clean up old tick data
            explosionsPerTick.entrySet().removeIf(entry -> entry.getKey() < currentTick - 20);

            // Update cooldown
            combustionLastUsed.put(playerId, now);

            // Mark this target as currently exploding
            currentlyExploding.add(targetId);

            // Create unique explosion ID to track which entities have been damaged
            UUID explosionId = UUID.randomUUID();
            explosionDamagedEntities.put(explosionId, new HashSet<>());

            // Schedule removal of explosion flag and cleanup
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                currentlyExploding.remove(targetId);
                explosionDamagedEntities.remove(explosionId);
            }, 10); // 10 ticks = 0.5 seconds - longer to prevent chain reactions

            // Enemy will die from this hit, cause explosion with SMALLER RADIUS
            List<Entity> nearbyEntities = target.getNearbyEntities(MAX_EXPLOSION_RADIUS, MAX_EXPLOSION_RADIUS, MAX_EXPLOSION_RADIUS); // Use constant
            Set<UUID> damagedInThisExplosion = explosionDamagedEntities.get(explosionId);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player && entity != target) {
                    LivingEntity nearbyTarget = (LivingEntity) entity;
                    UUID nearbyId = nearbyTarget.getUniqueId();
                    // Sprawdź czy cel żyje
                    if (nearbyTarget.isDead() || nearbyTarget.getHealth() <= 0) {
                        continue;
                    }
                    // Don't damage targets that are already exploding to prevent chains
                    if (currentlyExploding.contains(nearbyId)) {
                        continue;
                    }

                    // Don't damage the same entity multiple times in one explosion
                    if (damagedInThisExplosion.contains(nearbyId)) {
                        continue;
                    }

                    // Mark this entity as damaged by this explosion
                    damagedInThisExplosion.add(nearbyId);

                    // Explosion damage is percentage of ORIGINAL player damage, not mob HP
                    double originalDamage = event.getDamage(); // Get the original damage that killed the target
                    double explosionDamage = originalDamage * EXPLOSION_DAMAGE_PERCENT; // 20% of original damage

                    SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    double finalDamage = calculateSpellDamage(explosionDamage, player, ps);

                    // Use smart damage with recursion protection - kills will be attributed properly
                    smartDamage(nearbyTarget, player, finalDamage, "Combustion", true);

                    // Apply burning (but lighter effect)
                    applyBurning(player, nearbyTarget);

                    // Reduced visual effect
                    nearbyTarget.getWorld().spawnParticle(Particle.FLAME, nearbyTarget.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.03);
                }
            }

            // Reduced visual effect
            target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.8, 0.8, 0.8, 0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Combustion explosion!");
        }
    }

    private void applyIceSpellEffects(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Check for Frost Nova (skill 5)
        if (isPurchased(playerId, ID_OFFSET + 5)) {
            // 10% chance to freeze nearby enemies (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.10, player, "Frost Nova Freeze", false)) {
                // Freeze target
                applyFreezing(player, target, 1000); // 1s freeze

                // Freeze nearby enemies
                List<Entity> nearbyEntities = target.getNearbyEntities(3, 3, 3);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity nearbyTarget = (LivingEntity) entity;

                        // Apply freezing
                        applyFreezing(player, nearbyTarget, 1000); // 1s freeze

                        // Visual effect
                        nearbyTarget.getWorld().spawnParticle(Particle.SNOWFLAKE, nearbyTarget.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    }
                }

                // Visual effect
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Frost Nova activated!");
            }
        }

        // Check for Chilling Touch (skill 8)
        if (isPurchased(playerId, ID_OFFSET + 8)) {
            // Apply slowing effect
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, true, true)); // 3s, Slowness II
                ActionBarUtils.sendActionBar(targetPlayer, ChatColor.AQUA + "Slowed by " + player.getName() + "'s Chilling Touch!");
            } else {
                // For non-player entities, we'll just apply a visual effect
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            }
        }

        // Frost Heal (skill 9)
        if (isPurchased(playerId, ID_OFFSET + 9) && isFrozen(playerId, targetId)) {
            double heal = player.getMaxHealth() * 0.02;
            player.setHealth(Math.min(player.getHealth() + heal, player.getMaxHealth()));
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0,1,0), 5, 0.5,1,0.5,0.05);
        }

        // Check for Deep Freeze (skill 13)
        if (isPurchased(playerId, ID_OFFSET + 13) && isFrozen(playerId, targetId)) {
            // Increase damage against frozen enemies
            event.setDamage(event.getDamage() * 1.25); // 25% more damage

            // Visual effect
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Deep Freeze increased damage!");
        }

        // Check for Absolute Zero (skill 19)
        if (isPurchased(playerId, ID_OFFSET + 19)) {
            // 15% chance to instantly freeze (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.15, player, "Absolute Zero Instant Freeze", false)) {
                // Apply freezing
                applyFreezing(player, target, 2000); // 2s freeze

                // Visual effect
                target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 40, 1, 1, 1, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Absolute Zero froze the target!");
            }
        }
    }

    private void applyLightningSpellEffects(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Apply existing Resistance Break stacks
        Map<UUID, Integer> rbMap = resistanceBreakStacks.get(playerId);
        if (rbMap != null) {
            int stacks = rbMap.getOrDefault(targetId, 0);
            if (stacks > 0) {
                event.setDamage(event.getDamage() * (1 + 0.03 * stacks));
            }
        }

        // Check for Chain Lightning (skill 6)
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            // 15% chance to chain (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.15, player, "Chain Lightning", false)) {
                // Find nearby entity to chain to
                List<Entity> nearbyEntities = target.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player && entity != target) {
                        LivingEntity chainTarget = (LivingEntity) entity;

                        // Deal damage
                        double chainDamage = event.getDamage() * 0.5; // 50% of original damage
                        double finalChain = calculateSpellDamage(chainDamage, player,
                                plugin.getSkillEffectsHandler().getPlayerStats(player));
                        smartDamage(chainTarget, player, finalChain, "ChainLightning", true);

                        // Apply charging
                        applyCharging(player, chainTarget);

                        // Visual effect
                        chainTarget.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, chainTarget.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                        if (chainTarget instanceof Player) {
                            ActionBarUtils.sendActionBar((Player) chainTarget, ChatColor.YELLOW + "Hit by " + player.getName() + "'s Chain Lightning!");
                        }

                        // Only chain to one target
                        break;
                    }
                }

                // Visual effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Chain Lightning activated!");
            }
        }

        // Check for Static Charge (skill 10)
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            // Apply charging effect
            applyCharging(player, target);
        }

        // Check for Thunderstrike (skill 15)
        if (isPurchased(playerId, ID_OFFSET + 15)) {
            // 10% chance to call down thunderbolt (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.10, player, "Thunderstrike Thunderbolt", false)) {
                // Deal additional damage
                double thunderDamage = event.getDamage() * 0.4; // 40% of original damage
                SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                double finalDamage = calculateSpellDamage(thunderDamage, player, ps);
                smartDamage(target, player, finalDamage, "ThunderStrike", true);

                // Visual effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
                target.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Thunderstrike called down a thunderbolt!");
            }
        }

        // Check for Overcharge (skill 20)
        if (isPurchased(playerId, ID_OFFSET + 20)) {
            // 20% chance to critically strike (improved by Elemental Affinity)
            if (rollChanceWithDebug(0.20, player, "Overcharge Critical Strike", false)) {
                // Increase damage
                event.setDamage(event.getDamage() * 1.5); // 50% more damage

                // Visual effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Overcharge critical strike!");
            }
        }

        // Shock Spread (skill 17)
        if (isPurchased(playerId, ID_OFFSET + 17)) {
            if (rollChanceWithDebug(0.20, player, "Shock Spread", false)) {
                for (Entity entity : target.getNearbyEntities(4, 4, 4)) {
                    if (entity instanceof LivingEntity && entity != player && entity != target) {
                        LivingEntity near = (LivingEntity) entity;
                        double spreadDamage = event.getDamage() * 0.3;
                        double finalDmg = calculateSpellDamage(spreadDamage, player,
                                plugin.getSkillEffectsHandler().getPlayerStats(player));
                        // Use proper damage method for kill attribution
                        safelyDamageEntity(near, player, finalDmg, "ShockSpread");
                        applyCharging(player, near);
                    }
                }
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 40, 1, 1, 1, 0.2);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Shock Spread triggered!");
            }
        }

        // Apply/update Resistance Break stacks (skill 18)
        if (isPurchased(playerId, ID_OFFSET + 18)) {
            Map<UUID, Integer> map = resistanceBreakStacks.computeIfAbsent(playerId, k -> new HashMap<>());
            Map<UUID, Long> timers = resistanceBreakExpiry.computeIfAbsent(playerId, k -> new HashMap<>());
            int newStacks = Math.min(map.getOrDefault(targetId, 0) + 1, 4);
            map.put(targetId, newStacks);
            timers.put(targetId, System.currentTimeMillis() + 5000);
        }

        // Check for Conduction (skill 21)
        if (isPurchased(playerId, ID_OFFSET + 21) && isCharged(playerId, targetId)) {
            // Spread damage to nearby enemies
            List<Entity> nearbyEntities = target.getNearbyEntities(5, 5, 5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player && entity != target) {
                    LivingEntity nearbyTarget = (LivingEntity) entity;

                    // Deal damage
                    double conductionDamage = event.getDamage() * 0.3; // 30% of original damage
                    SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    double finalDamage = calculateSpellDamage(conductionDamage, player, ps);
                    smartDamage(nearbyTarget, player, finalDamage, "FrostNova", true);

                    // Apply charging
                    applyCharging(player, nearbyTarget);

                    // Visual effect
                    nearbyTarget.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, nearbyTarget.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                }
            }

            // Visual effect
            target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Conduction spread damage!");
        }
    }

    private void applyBurning(Player player, LivingEntity target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to burning enemies map
        Map<UUID, Long> burning = burningEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        long duration = 3000;
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            duration += 1000; // Lingering Elements extends duration
        }
        burning.put(targetId, System.currentTimeMillis() + duration);

        // Apply fire effect
        target.setFireTicks((int) (duration / 50));

        // Visual effect
        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        if (target instanceof Player) {
            ActionBarUtils.sendActionBar((Player) target, ChatColor.RED + "Burning from " + player.getName() + "'s spell!");
        }

        activateElementalSurge(player);
    }

    private void applyFreezing(Player player, LivingEntity target, long duration) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to frozen enemies map
        Map<UUID, Long> frozen = frozenEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        long finalDuration = duration;
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            finalDuration += 1000;
        }
        frozen.put(targetId, System.currentTimeMillis() + finalDuration);

        // Apply slowness effect
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (finalDuration / 50), 4, false, true, true)); // Extreme slowness
            ActionBarUtils.sendActionBar(targetPlayer, ChatColor.AQUA + "Frozen by " + player.getName() + "'s spell!");
        }

        // Visual effect
        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);

        activateElementalSurge(player);
    }

    private void applyCharging(Player player, LivingEntity target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to charged enemies map
        Map<UUID, Long> charged = chargedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        long duration = 5000;
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            duration += 1000;
        }
        charged.put(targetId, System.currentTimeMillis() + duration);

        // Visual effect
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        if (target instanceof Player) {
            ActionBarUtils.sendActionBar((Player) target, ChatColor.YELLOW + "Charged by " + player.getName() + "'s spell!");
        }

        activateElementalSurge(player);
    }

    private void applyStoneEffect(Player player, LivingEntity target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        Map<UUID, Long> stoned = stonedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        long duration = 3000;
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            duration += 1000;
        }
        stoned.put(targetId, System.currentTimeMillis() + duration);

        // Stone Burst (skill 6)
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            double lastDamage = mdLastDealtDamage.getOrDefault(playerId, 6.0);
            double splash = lastDamage * 0.5; // 50% of last damage dealt
            SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double finalSplash = calculateSpellDamage(splash, player, ps);
            for (Entity e : target.getNearbyEntities(2, 2, 2)) {
                if (e instanceof LivingEntity && e != player && e != target) {
                    // Use proper damage method for kill attribution
                    LivingEntity le = (LivingEntity) e;
                    safelyDamageEntity(le, player, finalSplash, "StoneSplash");
                }
            }
        }

        // Stone Stun (skill 10)
        if (isPurchased(playerId, ID_OFFSET + 10) && rollChanceWithDebug(0.10, player, "Stone Effect Stun", false)) {
            if (target instanceof Player) {
                ((Player) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 5, false, true, true));
            }
        }

        activateElementalSurge(player);

        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0,1,0), 10,0.5,1,0.5,0.05);
    }

    private boolean isBurning(UUID playerId, UUID targetId) {
        Map<UUID, Long> burning = burningEnemies.get(playerId);
        if (burning == null) return false;

        Long expiry = burning.get(targetId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private boolean isFrozen(UUID playerId, UUID targetId) {
        Map<UUID, Long> frozen = frozenEnemies.get(playerId);
        if (frozen == null) return false;

        Long expiry = frozen.get(targetId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private boolean isCharged(UUID playerId, UUID targetId) {
        Map<UUID, Long> charged = chargedEnemies.get(playerId);
        if (charged == null) return false;

        Long expiry = charged.get(targetId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    private boolean isStoned(UUID playerId, UUID targetId) {
        Map<UUID, Long> stoned = stonedEnemies.get(playerId);
        if (stoned == null) return false;

        Long expiry = stoned.get(targetId);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        LivingEntity dead = event.getEntity();
        UUID pid = player.getUniqueId();
        UUID tid = dead.getUniqueId();
        long now = System.currentTimeMillis();

        // [12] Kill z LIGHTNING → +10% MS /4s
        if (isPurchased(pid, ID_OFFSET + 12) && mdCharged(pid, tid)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, true, true, true));
        }

        // [16] Kill z FROST → heal 5% max HP
        if (isPurchased(pid, ID_OFFSET + 16) && mdFrozen(pid, tid)) {
            double max = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(max, player.getHealth() + max * 0.05));
        }

        // [20] Kill z STONE → +7% spell dmg /5s
        if (isPurchased(pid, ID_OFFSET + 20) && mdStoned(pid, tid)) {
            mdStoneKillSpellBuffUntil.put(pid, now + 5000);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> plugin.getSkillEffectsHandler().refreshPlayerStats(player), 100);
        }
    }

    /**
     * Handle player quit - cleanup data
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        clearPlayerData(playerId);
        clearAllPlayerData(playerId); // Clear unified effect system data
    }

    /**
     * Handle Elemental Explosion when the player dies
     */
    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID id = player.getUniqueId();

        if (isPurchased(id, ID_OFFSET + 25)) {
            double base = mdLastDealtDamage.getOrDefault(id, 6.0) * 0.15;
            Location c = player.getLocation();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (Entity e : c.getWorld().getNearbyEntities(c, 5, 5, 5)) {
                    if (e instanceof LivingEntity && !e.equals(player)) {
                        LivingEntity le = (LivingEntity) e;
                        smartDamage(le, player, base, "StormBarrier", true);
                    }
                }
                c.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, c, 1, .2,.2,.2, 0.0);
            });
        }

        // Remove any lingering buffs
        elementalSurgeExpiry.remove(id);
        stoneKillBuffExpiry.remove(id);
    }

    public void applyPeriodicEffects() {
        // Process burning enemies
        for (UUID playerId : burningEnemies.keySet()) {
            Map<UUID, Long> burning = burningEnemies.get(playerId);
            if (burning == null) continue;

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                burningEnemies.remove(playerId);
                continue;
            }

            // Process each burning enemy
            for (UUID targetId : new HashSet<>(burning.keySet())) {
                Long expiry = burning.get(targetId);
                if (expiry == null || expiry <= System.currentTimeMillis()) {
                    burning.remove(targetId);
                    continue;
                }

                // Find the entity
                for (Entity entity : player.getWorld().getEntities()) {
                    if (entity.getUniqueId().equals(targetId) && entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;

                        // Deal damage over time based on last damage dealt (Burning = 10% per tick)
                        double lastDamage = mdLastDealtDamage.getOrDefault(playerId, 4.0);
                        double damage = lastDamage * 0.10; // 10% of last damage per tick
                        SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                        double finalDamage = calculateSpellDamage(damage, player, ps);
                        smartDamage(target, player, finalDamage, "FrozenBurnDoT", true);

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);
                        break;
                    }
                }
            }
        }

        // Clean up stoned enemies
        for (UUID playerId : stonedEnemies.keySet()) {
            Map<UUID, Long> stoned = stonedEnemies.get(playerId);
            if (stoned == null) continue;

            for (UUID targetId : new HashSet<>(stoned.keySet())) {
                Long expiry = stoned.get(targetId);
                if (expiry == null || expiry <= System.currentTimeMillis()) {
                    stoned.remove(targetId);
                }
            }

            if (stoned.isEmpty()) {
                stonedEnemies.remove(playerId);
            }
        }

        // Remove expired Elemental Surge and Stone Kill buffs
        long now = System.currentTimeMillis();
        for (UUID id : new HashSet<>(elementalSurgeExpiry.keySet())) {
            if (elementalSurgeExpiry.get(id) < now) {
                elementalSurgeExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addSpellDamageBonus(-4);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                }
            }
        }

        for (UUID id : new HashSet<>(stoneKillBuffExpiry.keySet())) {
            if (stoneKillBuffExpiry.get(id) < now) {
                stoneKillBuffExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addSpellDamageBonus(-3);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                }
            }
        }

        // Remove expired Resistance Break stacks
        for (UUID pid : new HashSet<>(resistanceBreakExpiry.keySet())) {
            Map<UUID, Long> timers = resistanceBreakExpiry.get(pid);
            Map<UUID, Integer> stacks = resistanceBreakStacks.get(pid);
            if (timers == null) continue;
            for (UUID tid : new HashSet<>(timers.keySet())) {
                if (timers.get(tid) <= now) {
                    timers.remove(tid);
                    if (stacks != null) stacks.remove(tid);
                }
            }
            if (timers.isEmpty()) resistanceBreakExpiry.remove(pid);
            if (stacks != null && stacks.isEmpty()) resistanceBreakStacks.remove(pid);
        }
    }

    // Note: calculateSpellDamage is now inherited from BaseSkillEffectsHandler

    private boolean rollChance(Player player, double baseChance) {
        // Use unified proc chance calculation with Elementalist Affinity (skill 27)
        return rollProc(player, baseChance, null, ID_OFFSET + 27);
    }
    
    // Enhanced debug logging for skill activations
    private void logSkillActivation(Player player, String skillName, String details) {
        if (chanceDebugFlag == 1) {
            String msg = "[ELEMENTALIST] " + skillName + ": " + details;
            ChatNotificationUtils.send(player, ChatColor.GOLD + msg);
            plugin.getLogger().info("Player " + player.getName() + " - " + msg);
        }
    }
    
    // Override rollChanceWithDebug to use our chanceDebugFlag instead of base debuggingFlag
    protected boolean rollChanceWithDebug(double chance, Player player, String mechanicName, boolean usePercentage) {
        double normalizedChance = usePercentage ? chance / 100.0 : chance;
        boolean success = Math.random() < normalizedChance;
        
        if (chanceDebugFlag == 1 && player != null && mechanicName != null) {
            String msg = String.format("[ELEMENTALIST] %s: %.1f%% chance = %s", 
                mechanicName, normalizedChance * 100, success ? "SUCCESS" : "FAIL");
            ChatNotificationUtils.send(player, ChatColor.GOLD + msg);
        }
        
        return success;
    }

    private void activateElementalSurge(Player player) {
        UUID id = player.getUniqueId();
        if (!isPurchased(id, ID_OFFSET + 26)) {
            return;
        }

        long now = System.currentTimeMillis();
        long expiry = elementalSurgeExpiry.getOrDefault(id, 0L);
        SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
        if (expiry <= now) {
            ps.addSpellDamageBonus(4);
        }
        elementalSurgeExpiry.put(id, now + 10000); // 10s duration
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    private void applyStoneKillBuff(Player player) {
        UUID id = player.getUniqueId();
        if (!isPurchased(id, ID_OFFSET + 20)) {
            return;
        }

        long now = System.currentTimeMillis();
        long expiry = stoneKillBuffExpiry.getOrDefault(id, 0L);
        SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
        if (expiry <= now) {
            ps.addSpellDamageBonus(3);
        }
        stoneKillBuffExpiry.put(id, now + 5000); // 5s duration
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);
    }

    // Note: isPurchased is now inherited from BaseSkillEffectsHandler

    /**
     * Check if player has used all three element types and trigger Elemental Mastery if so
     * @param playerId The player's UUID
     */
    private void checkElementalMastery(UUID playerId) {
        if (playerId == null) {
            plugin.getLogger().warning("Attempted to check Elemental Mastery for null player ID");
            return;
        }

        // Get counters for each element
        int fireCount = fireSpellCounter.getOrDefault(playerId, 0);
        int iceCount = iceSpellCounter.getOrDefault(playerId, 0);
        int lightningCount = lightningSpellCounter.getOrDefault(playerId, 0);
        int stoneCount = stoneSpellCounter.getOrDefault(playerId, 0);

        // Check if player has used all three elements (at least 1 of each)
        if (fireCount > 0 && iceCount > 0 && lightningCount > 0 && stoneCount > 0) {
            // Reset counters
            fireSpellCounter.put(playerId, 0);
            iceSpellCounter.put(playerId, 0);
            lightningSpellCounter.put(playerId, 0);
            stoneSpellCounter.put(playerId, 0);

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Create elemental fusion effect
                List<Entity> nearbyEntities = player.getNearbyEntities(10, 10, 10);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Deal combined elemental damage based on recent damage (Mastery = 400% of last hit)
                        double lastDamage = mdLastDealtDamage.getOrDefault(playerId, 10.0);
                        double damage = lastDamage * 1.5; // 150% of last damage dealt
                        SkillEffectsHandler.PlayerSkillStats ps = plugin.getSkillEffectsHandler().getPlayerStats(player);
                        double finalDamage = calculateSpellDamage(damage, player, ps);
                        smartDamage(target, player, finalDamage, "MeteorStrike", true);

                        // Apply all three elemental effects
                        applyBurning(player, target);
                        applyFreezing(player, target, 2000); // 2s freeze
                        applyCharging(player, target);

                        // Visual effects - combine all three elements
                        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                    }
                }

                // Visual effect around player
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 8, 1, 1, 1, 0.1);
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 8, 1, 1, 1, 0.1);
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 8, 1, 1, 1, 0.1);

                // Buff player
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addSpellDamageBonus(12); // 12 spell damage bonus
                stats.addSpellCriticalChance(15); // 15% spell critical chance

                // Apply temporary resistance effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1, false, true, true)); // 10s, Resistance II
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, true, true)); // 10s

                // Schedule removal of spell damage bonus
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    SkillEffectsHandler.PlayerSkillStats updatedStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    updatedStats.addSpellDamageBonus(-12); // Remove the bonus
                    updatedStats.addSpellCriticalChance(-15); // Remove the bonus
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }, 200); // 10s

                // Notify player
                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Elemental Mastery activated!");
                ChatNotificationUtils.send(player, ChatColor.GOLD + "You have combined the power of all three elements!");
            }
        }
    }

    private int mdCountActiveElementalEffects(UUID playerId) {
        int count = 0;
        long now = System.currentTimeMillis();
        Map<UUID, Long> m;
        m = chargedEnemies.get(playerId); if (m != null) for (Long t : m.values()) if (t != null && t > now) count++;
        m = frozenEnemies.get(playerId);  if (m != null) for (Long t : m.values()) if (t != null && t > now) count++;
        m = stonedEnemies.get(playerId);  if (m != null) for (Long t : m.values()) if (t != null && t > now) count++;
        return count;
    }

    private boolean mdLightningBuffActive(UUID playerId) {
        Long until = mdLightningBuffUntil.get(playerId);
        return until != null && until > System.currentTimeMillis();
    }

    private void mdRefreshStatsLater(Player player, int delayTicks) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
        }, delayTicks);
    }

    // === helpers (EL MD) ===
    private void mdInc(Map<UUID, Map<UUID, Integer>> map, UUID pid, UUID tid, int max) {
        map.computeIfAbsent(pid, k -> new ConcurrentHashMap<>())
           .merge(tid, 1, Integer::sum);
        int v = map.get(pid).get(tid);
        if (v > max) map.get(pid).put(tid, max);
    }

    private int mdGet(Map<UUID, Map<UUID, Integer>> map, UUID pid, UUID tid) {
        return map.getOrDefault(pid, Collections.emptyMap()).getOrDefault(tid, 0);
    }

    private boolean mdHas(Map<UUID, Long> m, UUID tid) {
        return m != null && m.getOrDefault(tid, 0L) > System.currentTimeMillis();
    }

    private boolean mdFrozen(UUID pid, UUID tid) { return mdHas(frozenEnemies.get(pid), tid); }
    private boolean mdCharged(UUID pid, UUID tid) { return mdHas(chargedEnemies.get(pid), tid); }
    private boolean mdStoned(UUID pid, UUID tid) { return mdHas(stonedEnemies.get(pid), tid); }

    private void mdApplyMoveSpeedModifier(LivingEntity le, String key, double pct, int ticks) {
        AttributeInstance attr = le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return;
        UUID id = UUID.nameUUIDFromBytes(("EL_" + key + "_" + le.getUniqueId()).getBytes());
        for (AttributeModifier m : new ArrayList<>(attr.getModifiers())) {
            if (m.getName().equals(key)) {
                attr.removeModifier(m);
            }
        }
        AttributeModifier mod = new AttributeModifier(id, key, -pct,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        attr.addModifier(mod);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            AttributeInstance a = le.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (a != null) {
                a.getModifiers().stream()
                        .filter(mm -> mm.getName().equals(key))
                        .forEach(a::removeModifier);
            }
        }, ticks);
    }

    private void mdApplyAttackSpeedModifier(LivingEntity le, String key, double pct, int ticks) {
        AttributeInstance attr = le.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr == null) return;
        UUID id = UUID.nameUUIDFromBytes(("EL_" + key + "_" + le.getUniqueId()).getBytes());
        for (AttributeModifier m : new ArrayList<>(attr.getModifiers())) {
            if (m.getName().equals(key)) {
                attr.removeModifier(m);
            }
        }
        AttributeModifier mod = new AttributeModifier(id, key, -pct,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        attr.addModifier(mod);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            AttributeInstance a = le.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            if (a != null) {
                a.getModifiers().stream()
                        .filter(mm -> mm.getName().equals(key))
                        .forEach(a::removeModifier);
            }
        }, ticks);
    }

    private LivingEntity mdFindNearestEnemy(LivingEntity origin, double radius, java.util.function.Predicate<LivingEntity> filter) {
        // Add null safety checks
        if (origin == null || origin.isDead()) {
            return null;
        }
        
        Location loc = origin.getLocation();
        if (loc == null || loc.getWorld() == null) {
            return null;
        }
        
        LivingEntity best = null; 
        double bestD2 = Double.MAX_VALUE;
        
        try {
            for (Entity e : origin.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                if (!(e instanceof LivingEntity)) continue;
                LivingEntity le = (LivingEntity) e;
                if (le.isDead() || le.equals(origin)) continue;
                if (filter != null && !filter.test(le)) continue;
                
                Location leLocation = le.getLocation();
                if (leLocation == null) continue;
                
                double d2 = leLocation.distanceSquared(loc);
                if (d2 < bestD2) { 
                    bestD2 = d2; 
                    best = le; 
                }
            }
        } catch (Exception e) {
            // Log the exception for debugging but don't crash
            plugin.getLogger().warning("Exception in mdFindNearestEnemy: " + e.getMessage());
            return null;
        }
        
        return best;
    }

    /**
     * FIXED version of AoE damage that properly attributes kills
     */
    private void mdDoAoeDamage(Player source, Location center, double radius, double damage, LivingEntity exclude) {
        if (source == null || center == null || center.getWorld() == null || damage <= 0) {
            return;
        }

        // Cap on AoE damage - max 10% of target's health per hit
        // No absolute damage cap - let percentage scaling work

        try {
            int hitCount = 0;
            int maxHits = 5; // Maximum 5 targets for AoE

            for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (hitCount >= maxHits) break;

                if (!(e instanceof LivingEntity)) continue;
                LivingEntity le = (LivingEntity) e;

                if (le.isDead() || le.getHealth() <= 0 || le.equals(exclude) || le.equals(source)) {
                    continue;
                }

                // AoE damage is percentage of original damage, not mob HP
                double aoeDamage = damage * AOE_DAMAGE_PERCENT; // 10% of original damage

                // Use smart damage system - damage() with recursion protection
                smartDamage(le, source, aoeDamage, "AoE", true);
                hitCount++;

                // Visual effect
                if (!le.isDead()) {
                    le.getWorld().spawnParticle(Particle.CRIT, le.getLocation().add(0, 1, 0), 3);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Exception in mdDoAoeDamage: " + e.getMessage());
        }
    }

    /**
     * Safely damage an entity with proper attribution and damage caps
     */
    private void safelyDamageEntity(LivingEntity target, Player damager, double damage, String source) {
        smartDamage(target, damager, damage, source, true);
    }

    /**
     * Smart damage system that prevents recursion while maintaining kill attribution
     * @param target The entity to damage
     * @param damager The player dealing damage
     * @param damage Amount of damage
     * @param source Source of damage for logging
     * @param allowRecursion Whether to use damage() (true) or setHealth() (false)
     */
    private void smartDamage(LivingEntity target, Player damager, double damage, String source, boolean allowRecursion) {
        if (target == null || target.isDead() || damage <= 0) {
            return;
        }

        // Safety cap - never more than 50% of target's CURRENT health in one hit
        double safetyCap = target.getHealth() * MAX_SINGLE_HIT_PERCENT;
        damage = Math.min(damage, safetyCap);

        try {
            UUID targetId = target.getUniqueId();
            long now = System.currentTimeMillis();

            // Check cooldown to prevent spam
            Long lastTime = lastSkillDamageTime.get(targetId);
            if (lastTime != null && (now - lastTime) < SKILL_DAMAGE_COOLDOWN) {
                return; // Skip damage due to cooldown
            }

            lastSkillDamageTime.put(targetId, now);

            // ALWAYS use damage() for proper kill attribution, but with recursion protection
            if (skillRecursionDepth.get() < MAX_RECURSION_DEPTH) {
                // Increment depth before calling damage()
                skillRecursionDepth.set(skillRecursionDepth.get() + 1);
                try {
                    target.damage(damage, damager);
                } finally {
                    skillRecursionDepth.set(skillRecursionDepth.get() - 1);
                }
            }
            // If too deep in recursion, skip to prevent infinite loops
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to damage entity (" + source + "): " + e.getMessage());
        }
    }
    public void clearPlayerData(UUID playerId) {
        burningEnemies.remove(playerId);
        frozenEnemies.remove(playerId);
        chargedEnemies.remove(playerId);
        stonedEnemies.remove(playerId);
        fireShieldActive.remove(playerId);
        iceBarrierActive.remove(playerId);
        fireSpellCounter.remove(playerId);
        iceSpellCounter.remove(playerId);
        lightningSpellCounter.remove(playerId);
        stoneSpellCounter.remove(playerId);
        elementalSurgeExpiry.remove(playerId);
        stoneKillBuffExpiry.remove(playerId);
        resistanceBreakStacks.remove(playerId);
        resistanceBreakExpiry.remove(playerId);
        mdLightningBuffUntil.remove(playerId);
        combustionLastUsed.remove(playerId);
        lastSkillDamageTime.remove(playerId);

        // Clean ThreadLocal
        skillRecursionDepth.remove();

        // Clean up any explosion flags for entities this player might have been killing
        currentlyExploding.clear(); // Clear all explosion flags when any player leaves (safe approach)

        // Clear any damage processing flags for this player
        processingDamage.removeIf(key -> key.startsWith(playerId.toString() + ":"));

        // Clean up old location cooldowns (keep only recent ones)
        long cutoff = System.currentTimeMillis() - 10000; // Remove entries older than 10 seconds
        explosionLocationCooldown.entrySet().removeIf(entry -> entry.getValue() < cutoff);

        plugin.getLogger().info("Cleared all Elementalist data for player ID: " + playerId);
    }
}
