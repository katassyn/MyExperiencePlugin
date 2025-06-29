package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Elementalist-specific skill effects
 */
public class ElementalistSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
    private static final int ID_OFFSET = 700000;

    // Maps to track burning enemies
    private final Map<UUID, Map<UUID, Long>> burningEnemies = new ConcurrentHashMap<>();

    // Maps to track frozen enemies
    private final Map<UUID, Map<UUID, Long>> frozenEnemies = new ConcurrentHashMap<>();

    // Maps to track charged enemies
    private final Map<UUID, Map<UUID, Long>> chargedEnemies = new ConcurrentHashMap<>();

    // Maps to track fire shield
    private final Map<UUID, Long> fireShieldActive = new ConcurrentHashMap<>();

    // Maps to track ice barrier
    private final Map<UUID, Long> iceBarrierActive = new ConcurrentHashMap<>();

    // Maps to track spell counters
    private final Map<UUID, Integer> fireSpellCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> iceSpellCounter = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lightningSpellCounter = new ConcurrentHashMap<>();

    public ElementalistSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        int originalId = skillId - ID_OFFSET;

        switch (originalId) {
            case 1: // Fire Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 2: // Ice Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 3: // Lightning Mastery
                stats.addSpellDamageBonus(5 * purchaseCount);
                break;
            case 4: // Flame Burst
                // Effect applied in damage handler
                break;
            case 5: // Frost Nova
                // Effect applied in damage handler
                break;
            case 6: // Chain Lightning
                // Effect applied in damage handler
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

        // Check for Fire Shield (skill 11)
        if (isPurchased(playerId, ID_OFFSET + 11)) {
            // 15% chance to create a fire shield
            if (Math.random() < 0.15) {
                // Activate fire shield
                fireShieldActive.put(playerId, System.currentTimeMillis() + 3000); // 3s duration

                // Reduce damage
                event.setDamage(event.getDamage() * 0.8); // 20% damage reduction

                // Visual effect
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Fire Shield activated!");
            }
        }

        // Check for Ice Barrier (skill 9)
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            // Check if not on cooldown
            if (!iceBarrierActive.containsKey(playerId) || 
                    System.currentTimeMillis() - iceBarrierActive.get(playerId) > 30000) { // 30s cooldown

                // 20% chance to activate
                if (Math.random() < 0.2) {
                    // Activate ice barrier
                    iceBarrierActive.put(playerId, System.currentTimeMillis());

                    // Reduce damage
                    event.setDamage(event.getDamage() * 0.7); // 30% damage reduction

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
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
                                attacker.damage(reflectDamage, player);

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
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
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
                playerStats.addSpellDamageBonus(20); // 20% spell damage bonus

                // Schedule removal of spell damage bonus
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    SkillEffectsHandler.PlayerSkillStats updatedStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    updatedStats.addSpellDamageBonus(-20); // Remove the bonus
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }, 100); // 5s

                // Visual effect
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.2);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Storm Shield activated!");
            }
        }

        // Check for Electrified (skill 16)
        if (isPurchased(playerId, ID_OFFSET + 16)) {
            // 20% chance to release a shock wave
            if (Math.random() < 0.2) {
                // Find nearby enemies
                List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Deal damage
                        double damage = player.getMaxHealth() * 0.15; // 15% of player's max health
                        target.damage(damage, player);

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                    }
                }

                // Visual effect
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 40, 1, 1, 1, 0.2);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Electrified released a shock wave!");
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
                }
            }
        }
    }

    private String getSpellType(Player player) {
        // For demonstration, we'll randomly choose a spell type
        // In a real implementation, this would be determined by the actual spell being cast
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0: return "fire";
            case 1: return "ice";
            default: return "lightning";
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

                            // Deal damage
                            double damage = player.getMaxHealth() * 0.3; // 30% of player's max health
                            target.damage(damage, player);

                            // Apply burning
                            applyBurning(player, target);

                            // Visual effect
                            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                        }
                    }

                    // Visual effect
                    player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 60, 2, 1, 2, 0.2);
                    ActionBarUtils.sendActionBar(player, ChatColor.RED + "Fire Nova released!");
                }
            }
        } else if ("ice".equals(spellType)) {
            int count = iceSpellCounter.getOrDefault(playerId, 0) + 1;
            iceSpellCounter.put(playerId, count);
        } else if ("lightning".equals(spellType)) {
            int count = lightningSpellCounter.getOrDefault(playerId, 0) + 1;
            lightningSpellCounter.put(playerId, count);
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
            // 10% chance to create explosion
            if (Math.random() < 0.1) {
                // Deal additional damage
                double explosionDamage = event.getDamage() * 0.5; // 50% of original damage
                target.damage(explosionDamage, player);

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
            // Enemy will die from this hit, cause explosion
            List<Entity> nearbyEntities = target.getNearbyEntities(5, 5, 5);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player && entity != target) {
                    LivingEntity nearbyTarget = (LivingEntity) entity;

                    // Deal damage
                    double explosionDamage = event.getDamage() * 0.4; // 40% of original damage
                    nearbyTarget.damage(explosionDamage, player);

                    // Apply burning
                    applyBurning(player, nearbyTarget);

                    // Visual effect
                    nearbyTarget.getWorld().spawnParticle(Particle.FLAME, nearbyTarget.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
                }
            }

            // Visual effect
            target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 40, 1, 1, 1, 0.2);
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Combustion caused an explosion!");
        }
    }

    private void applyIceSpellEffects(Player player, LivingEntity target, EntityDamageByEntityEvent event) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Check for Frost Nova (skill 5)
        if (isPurchased(playerId, ID_OFFSET + 5)) {
            // 10% chance to freeze nearby enemies
            if (Math.random() < 0.1) {
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
            // 15% chance to instantly freeze
            if (Math.random() < 0.15) {
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

        // Check for Chain Lightning (skill 6)
        if (isPurchased(playerId, ID_OFFSET + 6)) {
            // 15% chance to chain
            if (Math.random() < 0.15) {
                // Find nearby entity to chain to
                List<Entity> nearbyEntities = target.getNearbyEntities(5, 5, 5);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player && entity != target) {
                        LivingEntity chainTarget = (LivingEntity) entity;

                        // Deal damage
                        double chainDamage = event.getDamage() * 0.5; // 50% of original damage
                        chainTarget.damage(chainDamage, player);

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
            // 10% chance to call down thunderbolt
            if (Math.random() < 0.1) {
                // Deal additional damage
                double thunderDamage = event.getDamage() * 0.4; // 40% of original damage
                target.damage(thunderDamage, player);

                // Visual effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
                target.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Thunderstrike called down a thunderbolt!");
            }
        }

        // Check for Overcharge (skill 20)
        if (isPurchased(playerId, ID_OFFSET + 20)) {
            // 20% chance to critically strike
            if (Math.random() < 0.2) {
                // Increase damage
                event.setDamage(event.getDamage() * 1.5); // 50% more damage

                // Visual effect
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                ActionBarUtils.sendActionBar(player, ChatColor.YELLOW + "Overcharge critical strike!");
            }
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
                    nearbyTarget.damage(conductionDamage, player);

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
        burning.put(targetId, System.currentTimeMillis() + 3000); // 3s duration

        // Apply fire effect
        target.setFireTicks(60); // 3s of fire

        // Visual effect
        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        if (target instanceof Player) {
            ActionBarUtils.sendActionBar((Player) target, ChatColor.RED + "Burning from " + player.getName() + "'s spell!");
        }
    }

    private void applyFreezing(Player player, LivingEntity target, long duration) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to frozen enemies map
        Map<UUID, Long> frozen = frozenEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        frozen.put(targetId, System.currentTimeMillis() + duration);

        // Apply slowness effect
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (duration / 50), 4, false, true, true)); // Extreme slowness
            ActionBarUtils.sendActionBar(targetPlayer, ChatColor.AQUA + "Frozen by " + player.getName() + "'s spell!");
        }

        // Visual effect
        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
    }

    private void applyCharging(Player player, LivingEntity target) {
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Add to charged enemies map
        Map<UUID, Long> charged = chargedEnemies.computeIfAbsent(playerId, k -> new HashMap<>());
        charged.put(targetId, System.currentTimeMillis() + 5000); // 5s duration

        // Visual effect
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.05);
        if (target instanceof Player) {
            ActionBarUtils.sendActionBar((Player) target, ChatColor.YELLOW + "Charged by " + player.getName() + "'s spell!");
        }
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

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // No specific death effects for Elementalist
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

                        // Deal damage over time
                        double damage = player.getMaxHealth() * 0.02; // 2% of player's max health per second
                        target.damage(damage, player);

                        // Visual effect
                        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 10, 0.5, 1, 0.5, 0.05);
                        break;
                    }
                }
            }
        }
    }

    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

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

        // Check if player has used all three elements (at least 1 of each)
        if (fireCount > 0 && iceCount > 0 && lightningCount > 0) {
            // Reset counters
            fireSpellCounter.put(playerId, 0);
            iceSpellCounter.put(playerId, 0);
            lightningSpellCounter.put(playerId, 0);

            // Get player
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Create elemental fusion effect
                List<Entity> nearbyEntities = player.getNearbyEntities(10, 10, 10);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Deal combined elemental damage
                        double damage = player.getMaxHealth() * 0.5; // 50% of player's max health
                        target.damage(damage, player);

                        // Apply all three elemental effects
                        applyBurning(player, target);
                        applyFreezing(player, target, 2000); // 2s freeze
                        applyCharging(player, target);

                        // Visual effects - combine all three elements
                        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.05);
                        target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.05);
                        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target.getLocation().add(0, 1, 0), 15, 0.5, 1, 0.5, 0.05);
                    }
                }

                // Visual effect around player
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.2);
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.2);
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.2);

                // Buff player
                SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                stats.addSpellDamageBonus(30); // 30% spell damage bonus
                stats.addSpellCriticalChance(15); // 15% spell critical chance

                // Apply temporary resistance effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1, false, true, true)); // 10s, Resistance II
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, true, true)); // 10s

                // Schedule removal of spell damage bonus
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    SkillEffectsHandler.PlayerSkillStats updatedStats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    updatedStats.addSpellDamageBonus(-30); // Remove the bonus
                    updatedStats.addSpellCriticalChance(-15); // Remove the bonus
                    plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                }, 200); // 10s

                // Notify player
                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Elemental Mastery activated!");
                player.sendMessage(ChatColor.GOLD + "You have combined the power of all three elements!");
            }
        }
    }

    public void clearPlayerData(UUID playerId) {
        burningEnemies.remove(playerId);
        frozenEnemies.remove(playerId);
        chargedEnemies.remove(playerId);
        fireShieldActive.remove(playerId);
        iceBarrierActive.remove(playerId);
        fireSpellCounter.remove(playerId);
        iceSpellCounter.remove(playerId);
        lightningSpellCounter.remove(playerId);

        plugin.getLogger().info("Cleared all Elementalist data for player ID: " + playerId);
    }
}
