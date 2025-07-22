package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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
    private final int debuggingFlag = 1;
    
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

    // Maps to track sanctuary
    private final Map<UUID, Location> sanctuaryLocation = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sanctuaryExpiry = new ConcurrentHashMap<>();

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
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 1: +" + (5 * purchaseCount) + "% defense (Arcane Shield)");
                }
                break;
            case 2: // Arcane Warding
                // Magic resistance handled dynamically
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 2: Will apply Arcane Warding dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 2: Arcane Warding enabled");
                }
                break;
            case 3: // Protective Aura
                // Aura effects handled dynamically
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 3: Will apply Protective Aura dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 3: Protective Aura enabled");
                }
                break;
            case 4: // Mana Barrier
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 4: Will apply Mana Barrier dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 4: Mana Barrier enabled");
                }
                break;
            case 7: // Arcane Reflection
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 7: Will apply Arcane Reflection dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 7: Arcane Reflection enabled");
                }
                break;
            case 8: // Arcane Armor
                // Effect applied in damage handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("ARCANE PROTECTOR SKILL 8: Will apply Arcane Armor dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] ARCANE PROTECTOR SKILL 8: Arcane Armor enabled");
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

        // Check for Spell Ward (skill 17)
        if (isPurchased(playerId, ID_OFFSET + 17) && event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            // Reduce spell damage by 20%
            event.setDamage(damage * 0.8);

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

                // Apply damage to ally
                ally.damage(sharedDamage);

                // Visual effect
                player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Damage shared with " + ally.getName() + "!");
                ActionBarUtils.sendActionBar(ally, ChatColor.BLUE + "Sharing damage with " + player.getName() + "!");
            }
        }

        // Check for Mana Shield (skill 16)
        if (isPurchased(playerId, ID_OFFSET + 16)) {
            // Check for nearby allies
            List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof Player) {
                    Player ally = (Player) entity;
                    String allyClass = plugin.getClassManager().getPlayerAscendancy(ally.getUniqueId());

                    // Only apply to allies (not enemies)
                    if (allyClass != null && !allyClass.isEmpty()) {
                        // Convert 20% of ally's damage to player's mana
                        double convertedDamage = damage * 0.2;
                        // In a real implementation, this would reduce player's mana
                        manaBarrierValues.put(playerId, manaBarrierValues.getOrDefault(playerId, 0.0) + convertedDamage);

                        // Visual effect
                        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                        ally.getWorld().spawnParticle(Particle.SPELL_MOB, ally.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                        ActionBarUtils.sendActionBar(ally, ChatColor.BLUE + player.getName() + " is shielding you with mana!");
                    }
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
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if player is the attacker
        if (event.getDamager() == player && event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Check for Arcane Reflection (skill 7)
            if (isPurchased(playerId, ID_OFFSET + 7) && arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                // Reflect 15% of damage
                double reflectedDamage = event.getDamage() * 0.15;
                target.damage(reflectedDamage, player);

                // Visual effect
                target.getWorld().spawnParticle(Particle.SPELL_MOB, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Reflected " + String.format("%.1f", reflectedDamage) + " damage!");
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
        }

        // Check if player is being attacked
        if (event.getEntity() == player && event.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();

            // Check for Arcane Reflection (skill 7)
            if (isPurchased(playerId, ID_OFFSET + 7) && arcaneShieldExpiry.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
                // Reflect 15% of damage
                double reflectedDamage = event.getDamage() * 0.15;
                attacker.damage(reflectedDamage, player);

                // Visual effect
                attacker.getWorld().spawnParticle(Particle.SPELL_MOB, attacker.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Reflected " + String.format("%.1f", reflectedDamage) + " damage!");
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // No specific death effects for ArcaneProtector
    }

    public void applyPeriodicEffects() {
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

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Shield activated!");
    }

    public void activateArcaneAegis(Player player) {
        UUID playerId = player.getUniqueId();

        // Set aegis duration (5 seconds)
        arcaneAegisExpiry.put(playerId, System.currentTimeMillis() + 5000);

        // Visual effect
        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
        ActionBarUtils.sendActionBar(player, ChatColor.BLUE + "Arcane Aegis activated!");
    }

    public void activateArcaneArmor(Player player) {
        UUID playerId = player.getUniqueId();

        // Set armor duration (20 seconds)
        arcaneArmorExpiry.put(playerId, System.currentTimeMillis() + 20000);

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

    private int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
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

        plugin.getLogger().info("Cleared all ArcaneProtector data for player ID: " + playerId);
    }
}
