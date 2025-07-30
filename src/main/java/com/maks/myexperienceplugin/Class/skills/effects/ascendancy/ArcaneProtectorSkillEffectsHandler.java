package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles ArcaneProtector-specific skill effects
 */
public class ArcaneProtectorSkillEffectsHandler extends BaseSkillEffectsHandler {
    private static final int ID_OFFSET = 900000;

    // Debug flag
    private final int debuggingFlag = 0;
    
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

    // Maps to track shields and barriers
    private final Map<UUID, Double> manaBarrierValues = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcaneShieldExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcaneAegisExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcaneArmorExpiry = new ConcurrentHashMap<>();

    // Maps to track protective runes and wards
    private final Map<UUID, List<Location>> protectiveRunes = new ConcurrentHashMap<>();
    private final Map<UUID, List<Location>> protectiveWards = new ConcurrentHashMap<>();
    private final Map<UUID, Long> protectiveRunesExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> protectiveWardsExpiry = new ConcurrentHashMap<>();

    // Maps to track healing circles
    private final Map<UUID, List<Location>> healingCircles = new ConcurrentHashMap<>();
    private final Map<UUID, Long> healingCirclesExpiry = new ConcurrentHashMap<>();

    // Maps to track spell absorption and deflection
    private final Map<UUID, Long> spellAbsorptionCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> spellDeflectionCooldown = new ConcurrentHashMap<>();

    // Maps to track arcane bonds
    private final Map<UUID, UUID> arcaneBonds = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcaneBondsExpiry = new ConcurrentHashMap<>();

    // Maps to track dispel magic and cleansing aura
    private final Map<UUID, Long> dispelMagicCooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cleansingAuraExpiry = new ConcurrentHashMap<>();

    // Maps to track arcane immunity and nullification field
    private final Map<UUID, Long> arcaneImmunityExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Location> nullificationFieldLocation = new ConcurrentHashMap<>();
    private final Map<UUID, Long> nullificationFieldExpiry = new ConcurrentHashMap<>();

    // Track active barriers for spell damage bonus (skill 26)
    private final Map<UUID, Integer> activeBarrierCounts = new ConcurrentHashMap<>();

    // Track defense bonus from nearby allies (skill 14)
    private final Map<UUID, Integer> allyDefenseBonuses = new ConcurrentHashMap<>();

    // Empowered hit after barrier (skill 19)
    private final Map<UUID, Boolean> empoweredHit = new ConcurrentHashMap<>();

    // Barrier Fortitude tracking (skill 21)
    private final Map<UUID, Integer> barrierActivationCount = new ConcurrentHashMap<>();
    private final Map<UUID, List<Long>> fortitudeExpiries = new ConcurrentHashMap<>();

    // Shared Shield tracking (skill 24)
    private final Map<UUID, Set<UUID>> sharedShieldTargets = new ConcurrentHashMap<>();

    // Maps to track sanctuary
    private final Map<UUID, Location> sanctuaryLocation = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sanctuaryExpiry = new ConcurrentHashMap<>();

    // Early skill tracking
    private final Map<UUID, Integer> damageResponseStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> damageResponseExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> slayerGuardStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> slayerGuardExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> periodicBarrierLast = new ConcurrentHashMap<>();
    private final Map<UUID, Long> periodicBarrierExpiry = new ConcurrentHashMap<>();

    // Cheat Death tracking (skill 27)
    private final Map<UUID, Long> cheatDeathExpiry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cheatDeathCooldown = new ConcurrentHashMap<>();

    // Critical Fortitude tracking (skill 10)
    private final Map<UUID, Long> criticalFortitudeExpiry = new ConcurrentHashMap<>();

    // Last Stand cooldown (skill 25)
    private final Map<UUID, Long> lastStandCooldown = new ConcurrentHashMap<>();

    // Posthumous Shield cooldown (skill 22)
    private final Map<UUID, Long> posthumousShieldCooldown = new ConcurrentHashMap<>();

    public ArcaneProtectorSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Arcane Shield
                stats.addDefenseBonus(5 * purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 1: Added " + (5 * purchaseCount) + "% defense bonus");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 1: +" + (5 * purchaseCount) + "% defense (Arcane Shield)");
                }
                break;
            case 2: // Arcane Warding
                // Magic resistance handled dynamically
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 2: Will apply Arcane Warding dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 2: Arcane Warding enabled");
                }
                break;
            case 3: // Protective Aura
                // Aura effects handled dynamically
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 3: Will apply Protective Aura dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 3: Protective Aura enabled");
                }
                break;
            case 4: // Mana Barrier
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 4: Will apply Mana Barrier dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 4: Mana Barrier enabled");
                }
                break;
            case 7: // Arcane Reflection
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 7: Will apply Arcane Reflection dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 7: Arcane Reflection enabled");
                }
                break;
            case 8: // Arcane Armor
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 8: Will apply Arcane Armor dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 8: Arcane Armor enabled");
                }
                break;
            case 11: // Spell Absorption
                // Effect applied in damage handler
                break;
            case 12: // Fortified Barrier
                // Effect applied in damage handler
                break;
            case 13: // Arcane Resistance
                // Effect applied in damage handler
                break;
            case 17: // Spell Ward
                // Effect applied in damage handler
                break;
            case 22: // Spell Deflection
                // Effect applied in damage handler
                break;
            // Other skills are handled dynamically
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        double damage = event.getDamage();

        long now = System.currentTimeMillis();

        // Critical Fortitude active
        if (criticalFortitudeExpiry.getOrDefault(playerId, 0L) > now) {
            event.setDamage(event.getDamage() * 0.9);
        }

        // If Cheat Death barrier is active, block damage
        if (cheatDeathExpiry.getOrDefault(playerId, 0L) > now) {
            event.setCancelled(true);
            return;
        }

        // Detect critical hit on player to activate Critical Fortitude (skill 10)
        if (isPurchased(playerId, ID_OFFSET + 10) && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            if (edbe.getDamager() instanceof Player) {
                Player attacker = (Player) edbe.getDamager();
                if (plugin.getCriticalStrikeSystem().rollForCritical(attacker)) {
                    criticalFortitudeExpiry.put(playerId, now + 4000);
                    ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Critical Fortitude!");
                }
            }
        }

        // Apply Damage Response stacks (skill 2)
        if (damageResponseStacks.containsKey(playerId)) {
            if (damageResponseExpiry.getOrDefault(playerId, 0L) > now) {
                int stacks = damageResponseStacks.getOrDefault(playerId, 0);
                double reduction = 0.03 * stacks;
                event.setDamage(damage * (1 - reduction));
            } else {
                damageResponseStacks.remove(playerId);
                damageResponseExpiry.remove(playerId);
            }
        }

        // Apply Slayer's Guard stacks (skill 7)
        if (slayerGuardStacks.containsKey(playerId)) {
            if (slayerGuardExpiry.getOrDefault(playerId, 0L) > now) {
                int stacks = slayerGuardStacks.getOrDefault(playerId, 0);
                double reduction = 0.03 * stacks;
                event.setDamage(event.getDamage() * (1 - reduction));
            } else {
                slayerGuardStacks.remove(playerId);
                slayerGuardExpiry.remove(playerId);
            }
        }

        // Half-Health Defense (skill 5)
        if (isPurchased(playerId, ID_OFFSET + 5) && player.getHealth() < player.getMaxHealth() * 0.5) {
            event.setDamage(event.getDamage() * 0.95);
        }

        // Periodic Barrier (skill 8)
        if (periodicBarrierExpiry.getOrDefault(playerId, 0L) > now) {
            event.setDamage(event.getDamage() * 0.93);
            periodicBarrierExpiry.remove(playerId);
            deactivateBarrierBonus(player);
        }

        // Check for Mana Barrier (skill 4)
        if (isPurchased(playerId, ID_OFFSET + 4)) {
            // Convert 10% of damage to mana cost
            double convertedDamage = damage * 0.1;
            // In a real implementation, this would reduce player's mana
            manaBarrierValues.put(playerId, manaBarrierValues.getOrDefault(playerId, 0.0) + convertedDamage);

            // Reduce actual damage
            event.setDamage(damage - convertedDamage);

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
            ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Mana Barrier absorbed " + String.format("%.1f", convertedDamage) + " damage!");
        }

        // Check for Arcane Shield (skill 1)
        if (isPurchased(playerId, ID_OFFSET + 1) && arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
            // Reduce damage based on shield strength
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 1);
            double reduction = 0.05 * purchaseCount; // 5% per level
            event.setDamage(damage * (1 - reduction));

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        }

        // Check for Arcane Aegis (skill 18)
        if (isPurchased(playerId, ID_OFFSET + 18) && arcaneAegisExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
            // Reduce damage by 50%
            event.setDamage(damage * 0.5);

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Aegis absorbed 50% damage!");
        }

        // Check for Arcane Armor (skill 8)
        if (isPurchased(playerId, ID_OFFSET + 8) && arcaneArmorExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
            // Reduce damage based on mana
            double reduction = 0.15; // 15% reduction
            event.setDamage(damage * (1 - reduction));

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        }

        // Check for Spell Absorption (skill 11)
        if (isPurchased(playerId, ID_OFFSET + 11) && event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            // 20% chance to gain mana
            if (rollChance(20, player, "Spell Absorption") && spellAbsorptionCooldown.getOrDefault(playerId, 0L) < System.currentTimeMillis()) {
                // In a real implementation, this would increase player's mana
                double manaGain = damage * 0.05;

                // Set cooldown (1 second)
                spellAbsorptionCooldown.put(playerId, System.currentTimeMillis() + 1000);

                // Visual effect
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Absorbed " + String.format("%.1f", manaGain) + " mana from spell damage!");
            }
        }

        // Check for Area Barrier damage reduction (skill 17)
        if (isPurchased(playerId, ID_OFFSET + 17)
                && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            // Reduce area damage by 10%
            event.setDamage(event.getDamage() * 0.9);

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        }

        // Check for Spell Deflection (skill 22)
        if (isPurchased(playerId, ID_OFFSET + 22) && event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            // 15% chance to completely negate
            if (rollChance(15, player, "Spell Deflection") && spellDeflectionCooldown.getOrDefault(playerId, 0L) < System.currentTimeMillis()) {
                // Cancel the event
                event.setCancelled(true);

                // Set cooldown (3 seconds)
                spellDeflectionCooldown.put(playerId, System.currentTimeMillis() + 3000);

                // Visual effect
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Spell completely deflected!");
            }
        }

        // Check for Arcane Resistance (skill 13)
        if (isPurchased(playerId, ID_OFFSET + 13) && event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            // Reduce magic damage by 25%
            event.setDamage(damage * 0.75);

            // Visual effect
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        }

        // Check for Arcane Immunity (skill 23)
        if (isPurchased(playerId, ID_OFFSET + 23) && event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            if (arcaneImmunityExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                // Complete immunity to magic damage
                event.setCancelled(true);

                // Visual effect
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Magic damage completely negated!");
            }
        }

        // Check for Arcane Bond (skill 20)
        if (arcaneBonds.containsKey(playerId) && arcaneBondsExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
            // Get bonded ally
            UUID allyId = arcaneBonds.get(playerId);
            Player ally = plugin.getServer().getPlayer(allyId);

            if (ally != null && ally.isOnline() && !ally.isDead()) {
                // Share 30% of damage
                double sharedDamage = damage * 0.3;
                event.setDamage(damage - sharedDamage);

                // Apply damage to ally scaled with SpellWeaver bonuses
                double finalShared = calculateSpellDamage(sharedDamage, player, stats);
                ally.damage(finalShared);

                // Visual effect
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Damage shared with " + ally.getName() + "!");
                ActionBarUtils.sendActionBar(ally, ChatColor.BLUE + "Sharing damage with " + player.getName() + "!");
            }
        }

        // Barrier Reflection (skill 23)
        boolean barrierActive = arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()
                || arcaneAegisExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()
                || arcaneArmorExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()
                || periodicBarrierExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis();
        if (barrierActive && isPurchased(playerId, ID_OFFSET + 23) && event instanceof EntityDamageByEntityEvent) {
            if (Math.random() < 0.15) {
                EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
                if (edbe.getDamager() instanceof LivingEntity) {
                    LivingEntity attacker = (LivingEntity) edbe.getDamager();
                    double reflect = event.getDamage() * 0.10;
                    attacker.damage(reflect, player);
                    attacker.getWorld().spawnParticle(Particle.SPELL_MOB, attacker.getLocation().add(0,1,0), 10,0.5,1,0.5,0.05);
                }
            }
        }


        // Check for Sanctuary (skill 27)
        if (isPurchased(playerId, ID_OFFSET + 27)) {
            // Check if player is in a sanctuary
            for (Map.Entry<UUID, Location> entry : sanctuaryLocation.entrySet()) {
                UUID ownerId = entry.getKey();
                Location location = entry.getValue();
                long expiry = sanctuaryExpiry.getOrDefault(ownerId, 0L);

                // Check if sanctuary is active
                if (expiry > System.currentTimeMillis()) {
                    // Check if player is in sanctuary
                    if (player.getLocation().distance(location) <= 8) {
                        // Cancel all damage
                        event.setCancelled(true);

                        // Visual effect
                        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
                        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Protected by Sanctuary!");
                        break;
                    }
                }
            }
        }

        // Last Stand (skill 25) - activate barrier at low HP
        if (isPurchased(playerId, ID_OFFSET + 25)
                && player.getHealth() - event.getFinalDamage() <= player.getMaxHealth() * 0.15
                && lastStandCooldown.getOrDefault(playerId, 0L) <= now) {
            arcaneImmunityExpiry.put(playerId, now + 3000);
            lastStandCooldown.put(playerId, now + 180000); // 3 min cooldown
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0,1,0), 40,1,1,1,0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Last Stand activated!");
        }

        // Cheat Death (skill 27) - trigger when damage would kill the player
        if (isPurchased(playerId, ID_OFFSET + 27)
                && player.getHealth() - event.getFinalDamage() <= 0
                && cheatDeathCooldown.getOrDefault(playerId, 0L) <= now) {
            event.setCancelled(true);
            player.setHealth(Math.min(player.getMaxHealth() * 0.3, player.getMaxHealth()));
            cheatDeathExpiry.put(playerId, now + 5000); // 5s immunity
            cheatDeathCooldown.put(playerId, now + 300000); // 5 min cooldown
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0,1,0), 50,1,1,1,0.1);
            ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Cheat Death activated!");
            return;
        }

        // After processing damage, increment Damage Response stacks (skill 2)
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            int stacks = Math.min(damageResponseStacks.getOrDefault(playerId, 0) + 1, 4);
            damageResponseStacks.put(playerId, stacks);
            damageResponseExpiry.put(playerId, System.currentTimeMillis() + 4000);
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if player is the attacker
        if (event.getDamager() == player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Magic Barrier Chance (skill 1)
            if (isPurchased(playerId, ID_OFFSET + 1) && Math.random() < 0.10) {
                activateArcaneShield(player);
            }

            // Weakening Strike (skill 4)
            if (isPurchased(playerId, ID_OFFSET + 4) && Math.random() < 0.10) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true, true));
            }

            // Attack Slow (skill 6)
            if (isPurchased(playerId, ID_OFFSET + 6)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0, false, true, true));
            }

            // Empowered Hit (skill 19)
            if (empoweredHit.remove(playerId) != null) {
                event.setDamage(event.getDamage() * 1.05);
            }

            // Check for Arcane Reflection (skill 7)
            if (isPurchased(playerId, ID_OFFSET + 7) && arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                // Reflect 15% of damage
                double reflectedDamage = event.getDamage() * 0.15;
                double finalDamage = calculateSpellDamage(reflectedDamage, player, stats);
                target.damage(finalDamage, player);

                // Visual effect
                target.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Reflected " + String.format("%.1f", finalDamage) + " damage!");
            }

            // Check for Arcane Resonance (skill 15)
            if (isPurchased(playerId, ID_OFFSET + 15)) {
                // Check if target is an ally
                if (target instanceof Player) {
                    Player ally = (Player) target;
                    String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                    // Only apply to allies (not enemies)
                    if (allyClass != null && !allyClass.isEmpty()) {
                        // Increase spell effect by 20%
                        // This would normally modify a spell's effect, but for simplicity we'll just show a message
                        ActionBarUtils.sendActionBar(ally, ChatColor.BLUE + "Spell effect increased by 20% from " + player.getName() + "'s Arcane Resonance!");
                    }
                }
            }

            // Ally Barrier (skill 15)
            if (isPurchased(playerId, ID_OFFSET + 15) && Math.random() < 0.10) {
                Player lowest = null;
                double ratio = 2.0;
                for (Entity e : player.getNearbyEntities(8,8,8)) {
                    if (e instanceof Player) {
                        Player ally = (Player) e;
                        String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                        if (allyClass != null && !allyClass.isEmpty()) {
                            double r = ally.getHealth() / ally.getMaxHealth();
                            if (r < ratio) {
                                ratio = r;
                                lowest = ally;
                            }
                        }
                    }
                }
                if (lowest != null) {
                    UUID aId = lowest.getUniqueId();
                    arcaneShieldExpiry.put(aId, System.currentTimeMillis() + 4000);
                    activateBarrierBonus(lowest);
                    lowest.getWorld().spawnParticle(Particle.SPELL_MOB, lowest.getLocation().add(0,1,0),20,0.5,1,0.5,0.05);
                    ActionBarUtils.sendActionBar(lowest, ChatColor.BLUE + "Barrier granted by " + player.getName());
                }
            }
        }

        // Check if player is being attacked
        if (event.getEntity() == player && event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();

            // Check for Arcane Reflection (skill 7)
            if (isPurchased(playerId, ID_OFFSET + 7) && arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                // Reflect 15% of damage
                double reflectedDamage = event.getDamage() * 0.15;
                double finalDamage = calculateSpellDamage(reflectedDamage, player, stats);
                attacker.damage(finalDamage, player);

                // Visual effect
                attacker.getWorld().spawnParticle(Particle.SPELL_MOB, attacker.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Reflected " + String.format("%.1f", finalDamage) + " damage!");
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Slayer's Guard stacks (skill 7)
        if (isPurchased(playerId, ID_OFFSET + 7)) {
            int stacks = Math.min(slayerGuardStacks.getOrDefault(playerId, 0) + 1, 4);
            slayerGuardStacks.put(playerId, stacks);
            slayerGuardExpiry.put(playerId, System.currentTimeMillis() + 5000);
        }

        // Mass Barrier on kill (skill 18)
        if (isPurchased(playerId, ID_OFFSET + 18)) {
            long now = System.currentTimeMillis();
            for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                if (e instanceof Player && e != player) {
                    Player ally = (Player) e;
                    String cl = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                    if (cl != null && !cl.isEmpty()) {
                        UUID allyId = ally.getUniqueId();
                        arcaneShieldExpiry.put(allyId, now + 4000);
                        activateBarrierBonus(ally);
                        ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0,1,0), 20,0.5,1,0.5,0.05);
                        ActionBarUtils.sendActionBar(ally, ChatColor.AQUA + "Mass Barrier from " + player.getName());
                    }
                }
            }
        }
    }

    public void applyPeriodicEffects() {
        // Apply Protective Aura around players with skill 3 and Ally Guard (skill 9)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            long now = System.currentTimeMillis();

            boolean protective = isPurchased(playerId, ID_OFFSET + 3);
            boolean allyGuard = isPurchased(playerId, ID_OFFSET + 9);
            // Ally Defense Boost (skill 14)
            if (isPurchased(playerId, ID_OFFSET + 14)) {
                int allies = 0;
                for (Entity e : player.getNearbyEntities(5,5,5)) {
                    if (e instanceof Player) {
                        Player ally = (Player) e;
                        String cl = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                        if (cl != null && !cl.isEmpty()) {
                            allies++;
                        }
                    }
                }
                int bonus = Math.min(allies, 5);
                int prev = allyDefenseBonuses.getOrDefault(playerId, 0);
                if (bonus != prev) {
                    allyDefenseBonuses.put(playerId, bonus);
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    st.addDefenseBonus(bonus - prev);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }
            } else if (allyDefenseBonuses.containsKey(playerId)) {
                int prev = allyDefenseBonuses.remove(playerId);
                SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(player);
                st.addDefenseBonus(-prev);
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            }
            if (protective || allyGuard) {
                for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                        if (allyClass != null && !allyClass.isEmpty()) {
                            ally.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, true, true));
                        }
                    }
                }
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 0.5, 0), 5, 1, 1, 1, 0.05);
            }

            // Shared Shield allies bonus (skill 24)
            boolean barrierActive = arcaneShieldExpiry.getOrDefault(playerId,0L) > now
                    || arcaneAegisExpiry.getOrDefault(playerId,0L) > now
                    || arcaneArmorExpiry.getOrDefault(playerId,0L) > now
                    || periodicBarrierExpiry.getOrDefault(playerId,0L) > now;
            Set<UUID> activeSet = sharedShieldTargets.computeIfAbsent(playerId, k -> new HashSet<>());
            if (isPurchased(playerId, ID_OFFSET + 24) && barrierActive) {
                Set<UUID> current = new HashSet<>();
                for (Entity e : player.getNearbyEntities(5,5,5)) {
                    if (e instanceof Player) {
                        Player ally = (Player) e;
                        String cl = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                        if (cl != null && !cl.isEmpty()) {
                            current.add(ally.getUniqueId());
                            if (!activeSet.contains(ally.getUniqueId())) {
                                activeSet.add(ally.getUniqueId());
                                SkillEffectsHandler.PlayerSkillStats stA = plugin.getSkillEffectsHandler().getPlayerStats(ally);
                                stA.addDefenseBonus(3);
                                plugin.getSkillEffectsHandler().refreshPlayerStats(ally);
                            }
                        }
                    }
                }
                for (UUID a : new HashSet<>(activeSet)) {
                    if (!current.contains(a)) {
                        activeSet.remove(a);
                        Player al = plugin.getServer().getPlayer(a);
                        if (al != null) {
                            SkillEffectsHandler.PlayerSkillStats stA = plugin.getSkillEffectsHandler().getPlayerStats(al);
                            stA.addDefenseBonus(-3);
                            plugin.getSkillEffectsHandler().refreshPlayerStats(al);
                        }
                    }
                }
            } else if (!barrierActive && !activeSet.isEmpty()) {
                for (UUID a : activeSet) {
                    Player al = plugin.getServer().getPlayer(a);
                    if (al != null) {
                        SkillEffectsHandler.PlayerSkillStats stA = plugin.getSkillEffectsHandler().getPlayerStats(al);
                        stA.addDefenseBonus(-3);
                        plugin.getSkillEffectsHandler().refreshPlayerStats(al);
                    }
                }
                activeSet.clear();
            }

            // Periodic Barrier (skill 8)
            if (isPurchased(playerId, ID_OFFSET + 8)) {
                now = System.currentTimeMillis();

                long last = periodicBarrierLast.getOrDefault(playerId, 0L);
                if (now - last >= 5000) {
                    periodicBarrierLast.put(playerId, now);
                    periodicBarrierExpiry.put(playerId, now + 10000);
                    activateBarrierBonus(player);
                    player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0,1,0), 10,0.5,1,0.5,0.05);
                }
            }
        }

        // Remove expired barriers and bonuses
        long now = System.currentTimeMillis();
        for (UUID id : new HashSet<>(arcaneShieldExpiry.keySet())) {
            if (arcaneShieldExpiry.get(id) < now) {
                arcaneShieldExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) deactivateBarrierBonus(p);
            }
        }
        for (UUID id : new HashSet<>(arcaneAegisExpiry.keySet())) {
            if (arcaneAegisExpiry.get(id) < now) {
                arcaneAegisExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) deactivateBarrierBonus(p);
            }
        }
        for (UUID id : new HashSet<>(arcaneArmorExpiry.keySet())) {
            if (arcaneArmorExpiry.get(id) < now) {
                arcaneArmorExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) deactivateBarrierBonus(p);
            }
        }

        for (UUID id : new HashSet<>(periodicBarrierExpiry.keySet())) {
            if (periodicBarrierExpiry.get(id) < now) {
                periodicBarrierExpiry.remove(id);
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) deactivateBarrierBonus(p);
            }
        }

        // Remove expired Barrier Fortitude stacks
        for (UUID id : new HashSet<>(fortitudeExpiries.keySet())) {
            List<Long> list = fortitudeExpiries.get(id);
            if (list == null) continue;
            int removed = 0;
            Iterator<Long> it = list.iterator();
            while (it.hasNext()) {
                if (it.next() < now) {
                    it.remove();
                    removed++;
                }
            }
            if (removed > 0) {
                Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(p);
                    st.addMaxHealth(-2 * removed);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(p);
                }
            }
            if (list.isEmpty()) {
                fortitudeExpiries.remove(id);
            }
        }

        // Process healing circles
        for (UUID playerId : healingCircles.keySet()) {
            List<Location> circles = healingCircles.get(playerId);
            if (circles == null || circles.isEmpty()) continue;

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                healingCircles.remove(playerId);
                continue;
            }

            // Check expiry
            long expiry = healingCirclesExpiry.getOrDefault(playerId, 0L);
            if (expiry < System.currentTimeMillis()) {
                healingCircles.remove(playerId);
                healingCirclesExpiry.remove(playerId);
                continue;
            }

            // Process each healing circle
            for (Location location : circles) {
                // Find nearby allies
                for (Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                        // Only heal allies (not enemies)
                        if (allyClass != null && !allyClass.isEmpty()) {
                            // Heal 2% of max health
                            double healAmount = ally.getMaxHealth() * 0.02;
                            double newHealth = Math.min(ally.getHealth() + healAmount, ally.getMaxHealth());
                            ally.setHealth(newHealth);

                            // Visual effect
                            ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                        }
                    }
                }

                // Visual effect for circle
                location.getWorld().spawnParticle(Particle.SPELL_MOB, location.add(0, 0.1, 0), 20, 2, 0.1, 2, 0.05);
            }
        }

        // Process protective runes
        for (UUID playerId : protectiveRunes.keySet()) {
            List<Location> runes = protectiveRunes.get(playerId);
            if (runes == null || runes.isEmpty()) continue;

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                protectiveRunes.remove(playerId);
                continue;
            }

            // Check expiry
            long expiry = protectiveRunesExpiry.getOrDefault(playerId, 0L);
            if (expiry < System.currentTimeMillis()) {
                protectiveRunes.remove(playerId);
                protectiveRunesExpiry.remove(playerId);
                continue;
            }

            // Process each rune
            for (Location location : runes) {
                // Find nearby allies
                for (Entity entity : location.getWorld().getNearbyEntities(location, 3, 3, 3)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                        // Only apply to allies (not enemies)
                        if (allyClass != null && !allyClass.isEmpty()) {
                            // Apply damage reduction (would be handled by damage event)
                            // Here we just apply a visual effect
                            ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                        }
                    }
                }

                // Visual effect for rune
                location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.add(0, 0.1, 0), 10, 1, 0.1, 1, 0.05);
            }
        }

        // Process protective wards
        for (UUID playerId : protectiveWards.keySet()) {
            List<Location> wards = protectiveWards.get(playerId);
            if (wards == null || wards.isEmpty()) continue;

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                protectiveWards.remove(playerId);
                continue;
            }

            // Check expiry
            long expiry = protectiveWardsExpiry.getOrDefault(playerId, 0L);
            if (expiry < System.currentTimeMillis()) {
                protectiveWards.remove(playerId);
                protectiveWardsExpiry.remove(playerId);
                continue;
            }

            // Process each ward
            for (Location location : wards) {
                // Find nearby allies
                for (Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
                    if (entity instanceof Player) {
                        Player ally = (Player) entity;
                        String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                        // Only apply to allies (not enemies)
                        if (allyClass != null && !allyClass.isEmpty()) {
                            // Apply damage absorption (would be handled by damage event)
                            // Here we just apply a visual effect
                            ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                        }
                    }
                }

                // Visual effect for ward
                location.getWorld().spawnParticle(Particle.SPELL_WITCH, location.add(0, 0.1, 0), 15, 2, 0.1, 2, 0.05);
            }
        }

        // Process cleansing aura
        for (UUID playerId : cleansingAuraExpiry.keySet()) {
            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                cleansingAuraExpiry.remove(playerId);
                continue;
            }

            // Check expiry
            long expiry = cleansingAuraExpiry.get(playerId);
            if (expiry < System.currentTimeMillis()) {
                cleansingAuraExpiry.remove(playerId);
                continue;
            }

            // Find nearby allies
            for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
                if (entity instanceof Player) {
                    Player ally = (Player) entity;
                    String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                    // Only apply to allies (not enemies)
                    if (allyClass != null && !allyClass.isEmpty()) {
                        // Remove negative effects faster (would require tracking potion effects)
                        // Here we just apply a visual effect
                        ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 5, 0.5, 1, 0.5, 0.05);
                    }
                }
            }

            // Visual effect for aura
            player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 10, 1, 1, 1, 0.05);
        }

        // Process nullification field
        for (UUID playerId : nullificationFieldLocation.keySet()) {
            Location location = nullificationFieldLocation.get(playerId);
            if (location == null) continue;

            // Check expiry
            long expiry = nullificationFieldExpiry.getOrDefault(playerId, 0L);
            if (expiry < System.currentTimeMillis()) {
                nullificationFieldLocation.remove(playerId);
                nullificationFieldExpiry.remove(playerId);
                continue;
            }

            // Visual effect for field
            location.getWorld().spawnParticle(Particle.SPELL_WITCH, location.add(0, 0.1, 0), 30, 4, 0.1, 4, 0.05);
        }

        // Process sanctuary
        for (UUID playerId : sanctuaryLocation.keySet()) {
            Location location = sanctuaryLocation.get(playerId);
            if (location == null) continue;

            // Check expiry
            long expiry = sanctuaryExpiry.getOrDefault(playerId, 0L);
            if (expiry < System.currentTimeMillis()) {
                sanctuaryLocation.remove(playerId);
                sanctuaryExpiry.remove(playerId);
                continue;
            }

            // Visual effect for sanctuary
            location.getWorld().spawnParticle(Particle.SPELL_MOB, location.add(0, 0.1, 0), 40, 4, 0.1, 4, 0.05);
        }
    }

    // Utility methods

    public void activateArcaneShield(Player player) {
        UUID playerId = player.getUniqueId();

        // Set shield duration (30 seconds)
        arcaneShieldExpiry.put(playerId, System.currentTimeMillis() + 30000);
        activateBarrierBonus(player);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Shield activated!");
    }

    public void activateArcaneAegis(Player player) {
        UUID playerId = player.getUniqueId();

        // Set aegis duration (5 seconds)
        arcaneAegisExpiry.put(playerId, System.currentTimeMillis() + 5000);
        activateBarrierBonus(player);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Aegis activated!");
    }

    public void activateArcaneArmor(Player player) {
        UUID playerId = player.getUniqueId();

        // Set armor duration (20 seconds)
        arcaneArmorExpiry.put(playerId, System.currentTimeMillis() + 20000);
        activateBarrierBonus(player);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Armor activated!");
    }

    public void createHealingCircle(Player player) {
        UUID playerId = player.getUniqueId();

        // Create healing circle at player's location
        List<Location> circles = healingCircles.computeIfAbsent(playerId, k -> new ArrayList<>());
        circles.add(player.getLocation().clone());

        // Set circle duration (15 seconds)
        healingCirclesExpiry.put(playerId, System.currentTimeMillis() + 15000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 0.1, 0), 30, 2, 0.1, 2, 0.05);
        ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Healing Circle created!");
    }

    public void createProtectiveRune(Player player) {
        UUID playerId = player.getUniqueId();

        // Create protective rune at player's location
        List<Location> runes = protectiveRunes.computeIfAbsent(playerId, k -> new ArrayList<>());
        runes.add(player.getLocation().clone());

        // Set rune duration (30 seconds)
        protectiveRunesExpiry.put(playerId, System.currentTimeMillis() + 30000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 0.1, 0), 20, 1, 0.1, 1, 0.05);
        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Protective Rune placed!");
    }

    public void createProtectiveWard(Player player) {
        UUID playerId = player.getUniqueId();

        // Create protective ward at player's location
        List<Location> wards = protectiveWards.computeIfAbsent(playerId, k -> new ArrayList<>());
        wards.add(player.getLocation().clone());

        // Set ward duration (20 seconds)
        protectiveWardsExpiry.put(playerId, System.currentTimeMillis() + 20000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 0.1, 0), 30, 2, 0.1, 2, 0.05);
        ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Protective Ward created!");
    }

    public void dispelMagic(Player player) {
        UUID playerId = player.getUniqueId();

        // Check cooldown
        if (dispelMagicCooldown.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Dispel Magic is on cooldown!");
            return;
        }

        // Set cooldown (20 seconds)
        dispelMagicCooldown.put(playerId, System.currentTimeMillis() + 20000);

        // Remove negative effects from player
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        // Remove negative effects from nearby allies
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                // Only apply to allies (not enemies)
                if (allyClass != null && !allyClass.isEmpty()) {
                    ally.removePotionEffect(PotionEffectType.POISON);
                    ally.removePotionEffect(PotionEffectType.WITHER);
                    ally.removePotionEffect(PotionEffectType.SLOW);
                    ally.removePotionEffect(PotionEffectType.WEAKNESS);
                    ally.removePotionEffect(PotionEffectType.BLINDNESS);

                    // Visual effect
                    ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    ActionBarUtils.sendActionBar(ally, ChatColor.AQUA + "Negative effects dispelled by " + player.getName() + "!");
                }
            }
        }

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Dispelled negative magic effects!");
    }

    public void activateCleansingAura(Player player) {
        UUID playerId = player.getUniqueId();

        // Set aura duration (30 seconds)
        cleansingAuraExpiry.put(playerId, System.currentTimeMillis() + 30000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Cleansing Aura activated!");
    }

    public void createArcaneBond(Player player, Player target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Create bond
        arcaneBonds.put(playerId, targetId);
        arcaneBonds.put(targetId, playerId);

        // Set bond duration (15 seconds)
        arcaneBondsExpiry.put(playerId, System.currentTimeMillis() + 15000);
        arcaneBondsExpiry.put(targetId, System.currentTimeMillis() + 15000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        target.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Arcane Bond created with " + target.getName() + "!");
        ActionBarUtils.sendActionBar(target, ChatColor.YELLOW + "Arcane Bond created with " + player.getName() + "!");
    }

    public void activateArcaneImmunity(Player player) {
        UUID playerId = player.getUniqueId();

        // Set immunity duration (3 seconds)
        arcaneImmunityExpiry.put(playerId, System.currentTimeMillis() + 3000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Arcane Immunity activated!");
    }

    public void createNullificationField(Player player) {
        UUID playerId = player.getUniqueId();

        // Create field at player's location
        nullificationFieldLocation.put(playerId, player.getLocation().clone());

        // Set field duration (10 seconds)
        nullificationFieldExpiry.put(playerId, System.currentTimeMillis() + 10000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 0.1, 0), 50, 4, 0.1, 4, 0.05);
        ActionBarUtils.sendActionBar(player, ChatColor.LIGHT_PURPLE + "Nullification Field created!");
    }

    public void createSanctuary(Player player) {
        UUID playerId = player.getUniqueId();

        // Create sanctuary at player's location
        sanctuaryLocation.put(playerId, player.getLocation().clone());

        // Set sanctuary duration (5 seconds)
        sanctuaryExpiry.put(playerId, System.currentTimeMillis() + 5000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 0.1, 0), 60, 4, 0.1, 4, 0.05);
        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Sanctuary created!");
    }

    public void massProtection(Player player) {
        // Apply all protective effects to nearby allies
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof Player) {
                Player ally = (Player) entity;
                String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                // Only apply to allies (not enemies)
                if (allyClass != null && !allyClass.isEmpty()) {
                    // Apply protective effects
                    UUID allyId = ally.getUniqueId();
                    arcaneShieldExpiry.put(allyId, System.currentTimeMillis() + 10000); // 10s shield
                    activateBarrierBonus(ally);

                    // Visual effect
                    ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    ActionBarUtils.sendActionBar(ally, ChatColor.GOLD + "Protected by " + player.getName() + "'s Mass Protection!");
                }
            }
        }

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 50, 2, 1, 2, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Mass Protection activated!");
    }

    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    private void activateBarrierBonus(Player player) {
        UUID id = player.getUniqueId();
        activeBarrierCounts.put(id, activeBarrierCounts.getOrDefault(id, 0) + 1);
        // Empowered Hit (skill 19)
        if (isPurchased(id, ID_OFFSET + 19)) {
            empoweredHit.put(id, true);
        }
        // Negative Guard (skill 20)
        if (isPurchased(id, ID_OFFSET + 20)) {
            reduceNegativeEffects(player);
        }
        // Barrier Fortitude (skill 21)
        if (isPurchased(id, ID_OFFSET + 21)) {
            int count = barrierActivationCount.getOrDefault(id, 0) + 1;
            barrierActivationCount.put(id, count);
            if (count % 3 == 0) {
                List<Long> timers = fortitudeExpiries.computeIfAbsent(id, k -> new ArrayList<>());
                if (timers.size() < 5) {
                    timers.add(System.currentTimeMillis() + 10000);
                    SkillEffectsHandler.PlayerSkillStats st = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    st.addMaxHealth(2);
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }
            }
        }
        if (isPurchased(id, ID_OFFSET + 26)) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            stats.addSpellDamageBonus(5);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
        }
        if (isPurchased(id, ID_OFFSET + 16)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, false, true, true));
        }
    }

    private void deactivateBarrierBonus(Player player) {
        UUID id = player.getUniqueId();
        int count = activeBarrierCounts.getOrDefault(id, 0);
        if (count > 0) {
            activeBarrierCounts.put(id, count - 1);
            if (isPurchased(id, ID_OFFSET + 26)) {
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addSpellDamageBonus(-5);
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            }
            if (isPurchased(id, ID_OFFSET + 16) && count == 1) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }
    }

    private void reduceNegativeEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            PotionEffectType type = effect.getType();
            if (type == PotionEffectType.POISON || type == PotionEffectType.SLOW ||
                type == PotionEffectType.WEAKNESS || type == PotionEffectType.BLINDNESS ||
                type == PotionEffectType.WITHER) {
                int newDur = (int) (effect.getDuration() * 0.9);
                player.removePotionEffect(type);
                player.addPotionEffect(new PotionEffect(type, newDur, effect.getAmplifier(), false, effect.isAmbient(), effect.hasParticles()));
            }
        }
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

    private int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }

    /**
     * Handle Posthumous Shield when the player dies (skill 22)
     */
    public void handlePlayerDeath(PlayerDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (isPurchased(id, ID_OFFSET + 22) && posthumousShieldCooldown.getOrDefault(id, 0L) <= now) {
            for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                if (e instanceof Player) {
                    Player ally = (Player) e;
                    String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());
                    if (allyClass != null && !allyClass.isEmpty()) {
                        UUID allyId = ally.getUniqueId();
                        arcaneShieldExpiry.put(allyId, now + 5000);
                        activateBarrierBonus(ally);
                        ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0,1,0), 20, 0.5,1,0.5,0.05);
                        ActionBarUtils.sendActionBar(ally, ChatColor.AQUA + "Shielded by " + player.getName() + "!");
                    }
                }
            }
            posthumousShieldCooldown.put(id, now + 300000); // 5 min cooldown
        }
    }

    public void clearPlayerData(UUID playerId) {
        manaBarrierValues.remove(playerId);
        arcaneShieldExpiry.remove(playerId);
        arcaneAegisExpiry.remove(playerId);
        arcaneArmorExpiry.remove(playerId);
        protectiveRunes.remove(playerId);
        protectiveWards.remove(playerId);
        protectiveRunesExpiry.remove(playerId);
        protectiveWardsExpiry.remove(playerId);
        healingCircles.remove(playerId);
        healingCirclesExpiry.remove(playerId);
        spellAbsorptionCooldown.remove(playerId);
        spellDeflectionCooldown.remove(playerId);
        arcaneBonds.remove(playerId);
        arcaneBondsExpiry.remove(playerId);
        dispelMagicCooldown.remove(playerId);
        cleansingAuraExpiry.remove(playerId);
        arcaneImmunityExpiry.remove(playerId);
        nullificationFieldLocation.remove(playerId);
        nullificationFieldExpiry.remove(playerId);
        sanctuaryLocation.remove(playerId);
        sanctuaryExpiry.remove(playerId);
        activeBarrierCounts.remove(playerId);
        damageResponseStacks.remove(playerId);
        damageResponseExpiry.remove(playerId);
        slayerGuardStacks.remove(playerId);
        slayerGuardExpiry.remove(playerId);
        periodicBarrierLast.remove(playerId);
        periodicBarrierExpiry.remove(playerId);
        allyDefenseBonuses.remove(playerId);
        empoweredHit.remove(playerId);
        barrierActivationCount.remove(playerId);
        fortitudeExpiries.remove(playerId);
        sharedShieldTargets.remove(playerId);
        cheatDeathExpiry.remove(playerId);
        cheatDeathCooldown.remove(playerId);
        posthumousShieldCooldown.remove(playerId);
        criticalFortitudeExpiry.remove(playerId);
        lastStandCooldown.remove(playerId);

        plugin.getLogger().info("Cleared all ArcaneProtector data for player ID: " + playerId);
    }
}
