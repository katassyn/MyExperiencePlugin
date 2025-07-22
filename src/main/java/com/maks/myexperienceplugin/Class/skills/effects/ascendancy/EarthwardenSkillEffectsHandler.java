package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EarthwardenSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {

    // ID offset for Earthwarden skills
    private static final int ID_OFFSET = 600000;

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

    // Maps to track player states and cooldowns
    private final Map<UUID, Long> lastMovementTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDamageTakenTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> divineProtectionCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> entanglingStrikeCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> entanglingStrikeReady = new ConcurrentHashMap<>();
    private final Map<UUID, Long> secondChanceCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> playerAttackers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> reactiveDefenseEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> survivalInstinctEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastStandEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> desperateEscapeEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> protectiveInstinctEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> counterattackEndTimes = new ConcurrentHashMap<>();

    // Custom immunity system to avoid using player.setInvulnerable(true)
    private final Map<UUID, Long> immunityEndTimes = new ConcurrentHashMap<>();

    // Task IDs
    private BukkitTask periodicEffectsTask;
    
    /**
     * Clean up all player data
     */
    public void clearPlayerData(UUID playerId) {
        // Clear all maps
        lastMovementTime.remove(playerId);
        lastDamageTakenTime.remove(playerId);
        divineProtectionCooldowns.remove(playerId);
        entanglingStrikeCooldowns.remove(playerId);
        entanglingStrikeReady.remove(playerId);
        secondChanceCooldowns.remove(playerId);
        playerAttackers.remove(playerId);
        reactiveDefenseEndTimes.remove(playerId);
        survivalInstinctEndTimes.remove(playerId);
        lastStandEndTimes.remove(playerId);
        desperateEscapeEndTimes.remove(playerId);
        protectiveInstinctEndTimes.remove(playerId);
        counterattackEndTimes.remove(playerId);
        immunityEndTimes.remove(playerId);
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all Earthwarden data for player ID: " + playerId);
        }
    }

    public EarthwardenSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
        // Register this class as a listener for events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Start periodic effects task
        startPeriodicEffectsTask();
    }

    /**
     * Start the periodic effects task
     */
    private void startPeriodicEffectsTask() {
        periodicEffectsTask = Bukkit.getScheduler().runTaskTimer(plugin, this::applyPeriodicEffects, 20L, 20L);
    }

    /**
     * Stop the periodic effects task
     */
    public void stopPeriodicEffectsTask() {
        if (periodicEffectsTask != null) {
            periodicEffectsTask.cancel();
            periodicEffectsTask = null;
        }
    }

    /**
     * Apply periodic effects for all online Earthwarden players
     */
    public void applyPeriodicEffects() {
        // Cleanup expired effects
        long currentTime = System.currentTimeMillis();

        // Cleanup expired effects
        reactiveDefenseEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        survivalInstinctEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        lastStandEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        desperateEscapeEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        protectiveInstinctEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        counterattackEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());
        immunityEndTimes.entrySet().removeIf(entry -> currentTime > entry.getValue());

        for (Player player : Bukkit.getOnlinePlayers()) {
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            if (!"Earthwarden".equals(ascendancy)) {
                continue;
            }

            // Check for standing still (Steadfast Defense)
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 5)) {
                checkSteadfastDefense(player);
            }

            // Check for Entangling Strike cooldown
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 8)) {
                checkEntanglingStrikeCooldown(player);
            }

            // Check for Strength in Numbers
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 11)) {
                applyStrengthInNumbers(player);
            }

            // Check for Allied Strength
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 26)) {
                applyAlliedStrength(player);
            }

            // Check for Surrounded Defense
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 17)) {
                applySurroundedDefense(player);
            }

            // Check for Surrounded Strength
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 23)) {
                applySurroundedStrength(player);
            }

            // Check for Healthy Speed
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 20)) {
                applyHealthySpeed(player);
            }

            // Check for Desperate Strength
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 19)) {
                applyDesperateStrength(player);
            }

            // Check for grassy area defense bonus
            if (isPurchased(player.getUniqueId(), ID_OFFSET + 1)) {
                checkGrassyAreaDefense(player);
            }
        }
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // +3% defense in grassy areas
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 1: Will add 3% defense in grassy areas");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 1: +3% defense in grassy areas enabled");
                }
                break;

            case 2: // Heal 1 hp after killing an enemy
                // This is handled in the EntityDeathEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 2: Will heal 1 hp after killing an enemy");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 2: Heal 1 HP after killing enemy enabled");
                }
                break;

            case 3: // +5% resistance to environmental damage
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 3: Will add 5% resistance to environmental damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 3: +5% resistance to environmental damage enabled");
                }
                break;

            case 4: // +2 hp per level
                // This is a fixed bonus based on skill level (not player level)
                // Each purchase (skill level) adds +2 HP
                double hpBonus = 2 * purchaseCount;
                stats.addMaxHealth(hpBonus);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 4: Added " + hpBonus + " HP (skill level " + purchaseCount + "/3)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 4: +" + hpBonus + " HP (level " + purchaseCount + "/3)");
                }
                break;

            case 5: // After standing still for 3 seconds, gain +10% defense
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 5: Will add 10% defense after standing still for 3 seconds");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 5: +10% defense after standing still for 3s enabled");
                }
                break;

            case 6: // +5% luck when below 50% hp
                // This is handled dynamically during gameplay
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 6: Will add 5% luck when below 50% hp");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 6: +5% luck when HP < 50% enabled");
                }
                break;

            case 7: // When hp<50%, gain +5% defense for 3 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 7: Will add 5% defense for 3 seconds when hp<50%");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 7: +5% defense for 3s when HP < 50% enabled");
                }
                break;

            case 8: // Every 10 seconds, your next attack roots the enemy
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 8: Will root enemies every 10 seconds");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] EARTHWARDEN SKILL 8: Root enemies every 10s enabled");
                }
                break;

            case 9: // +10% hp regen from all sources
                // This affects all healing received - we'll use a different approach since there's no healing multiplier
                stats.addDefenseBonus(0.1 * purchaseCount); // Using defense as a proxy for healing
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 9: Added 10% hp regen from all sources (using defense as proxy)");
                }
                break;

            case 10: // When taking damage, 15% chance to gain +15% defense for 5 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 10: Will add 15% defense for 5 seconds with 15% chance when taking damage");
                }
                break;

            case 11: // +1% defense for each nearby ally within 10 blocks (max +5%)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 11: Will add 1% defense for each nearby ally");
                }
                break;

            case 12: // After blocking attack, gain +20% movement speed for 3 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 12: Will add 20% movement speed for 3 seconds after blocking");
                }
                break;

            case 13: // +8% damage against enemies that have attacked you
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 13: Will add 8% damage against enemies that have attacked you");
                }
                break;

            case 14: // Attacks have 10% chance to heal you for 5% of damage dealt
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 14: Will heal 5% of damage dealt with 10% chance");
                }
                break;

            case 15: // When hp<30%, gain +20% defense and +10% damage for 5 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 15: Will add 20% defense and 10% damage for 5 seconds when hp<30%");
                }
                break;

            case 16: // 5% chance to negate all damage and gain immunity for 2 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 16: Will negate all damage with 5% chance");
                }
                break;

            case 17: // +3% defense for each enemy within 5 blocks (max +15%)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 17: Will add 3% defense for each nearby enemy");
                }
                break;

            case 18: // +5% defense for each piece of armor worn
                // This is a fixed bonus based on armor
                int armorCount = 0;
                if (player.getInventory().getHelmet() != null) armorCount++;
                if (player.getInventory().getChestplate() != null) armorCount++;
                if (player.getInventory().getLeggings() != null) armorCount++;
                if (player.getInventory().getBoots() != null) armorCount++;
                stats.addDefenseBonus(0.05 * armorCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 18: Added " + (0.05 * armorCount) + " defense for " + armorCount + " armor pieces");
                }
                break;

            case 19: // When hp<20%, gain +15% damage for 5 seconds
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 19: Will add 15% damage for 5 seconds when hp<20%");
                }
                break;

            case 20: // When hp>80%, gain +10% movement speed
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 20: Will add 10% movement speed when hp>80%");
                }
                break;

            case 21: // When an ally within 15 blocks is attacked, gain +5% damage and +10% movement speed for 5 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 21: Will add 5% damage and 10% movement speed when ally is attacked");
                }
                break;

            case 22: // Attacks have 25% chance to heal you for 2% of your max health
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 22: Will heal 2% of max health with 25% chance");
                }
                break;

            case 23: // +2% damage for each enemy within 5 blocks (max +10%)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 23: Will add 2% damage for each nearby enemy");
                }
                break;

            case 24: // +30% damage if you haven't taken damage in the last 5 seconds
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 24: Will add 30% damage if no damage taken in 5 seconds");
                }
                break;

            case 25: // When hp<20%, gain +25% evade chance and +15% movement speed for 5 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 25: Will add 25% evade and 15% speed when hp<20%");
                }
                break;

            case 26: // +2% damage for each nearby ally within 10 blocks (max +10%)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 26: Will add 2% damage for each nearby ally");
                }
                break;

            case 27: // When killed, you survive with 10% hp and gain immunity for 2 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 27: Will provide second chance on death");
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if player has immunity from custom immunity system
        if (immunityEndTimes.containsKey(playerId) && System.currentTimeMillis() < immunityEndTimes.get(playerId)) {
            event.setCancelled(true);
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Cancelled damage for " + player.getName() + " due to immunity");
            }
            return;
        }

        // Update last damage taken time
        lastDamageTakenTime.put(playerId, System.currentTimeMillis());

        // Environmental Resistance (Skill 3)
        if (isPurchased(playerId, ID_OFFSET + 3) && isEnvironmentalDamage(event.getCause())) {
            double reduction = 0.05; // 5% reduction
            event.setDamage(event.getDamage() * (1 - reduction));
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied 5% environmental damage reduction for " + player.getName());
            }
        }

        // Survival Instinct (Skill 7)
        if (isPurchased(playerId, ID_OFFSET + 7) && player.getHealth() < player.getMaxHealth() * 0.5) {
            survivalInstinctEndTimes.put(playerId, System.currentTimeMillis() + 3000); // 3 seconds
            stats.addDefenseBonus(0.05); // 5% defense
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Survival Instinct activated!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Survival Instinct for " + player.getName());
            }
        }

        // Reactive Defense (Skill 10)
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            double chance = 0.15 * getPurchaseCount(playerId, ID_OFFSET + 10);
            boolean success = rollChance(chance * 100, player, "Reactive Defense");
            
            if (success) {
                reactiveDefenseEndTimes.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
                stats.addDefenseBonus(0.15); // 15% defense
                plugin.getSkillEffectsHandler().refreshPlayerStats(player);
                ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Reactive Defense activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Applied +15% defense for 5 seconds");
                    plugin.getLogger().info("EARTHWARDEN: Applied Reactive Defense for " + player.getName());
                }
            }
        }

        // Counterattack (Skill 12)
        if (isPurchased(playerId, ID_OFFSET + 12) && player.isBlocking()) {
            counterattackEndTimes.put(playerId, System.currentTimeMillis() + 3000); // 3 seconds
            applyMovementSpeedEffects(player); // Apply movement speed bonuses
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Counterattack activated!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Counterattack for " + player.getName());
            }
        }

        // Last Stand (Skill 15)
        if (isPurchased(playerId, ID_OFFSET + 15) && player.getHealth() < player.getMaxHealth() * 0.3) {
            lastStandEndTimes.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
            stats.addDefenseBonus(0.2); // 20% defense
            stats.addBonusDamage(0.1); // 10% damage
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Last Stand activated!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Last Stand for " + player.getName());
            }
        }

        // Divine Protection (Skill 16)
        if (isPurchased(playerId, ID_OFFSET + 16) && !isOnCooldown(playerId, divineProtectionCooldowns, 30000)) {
            boolean success = rollChance(5.0, player, "Divine Protection");
            
            if (success) {
                // Cancel the damage
                event.setCancelled(true);

                // Apply immunity for 2 seconds using custom immunity system
                divineProtectionCooldowns.put(playerId, System.currentTimeMillis());
                immunityEndTimes.put(playerId, System.currentTimeMillis() + 2000); // 2 seconds

                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Divine Protection activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Cancelled damage and granted 2s immunity");
                    plugin.getLogger().info("EARTHWARDEN: Applied Divine Protection for " + player.getName());
                }
            }
        }

        // Desperate Escape (Skill 25)
        if (isPurchased(playerId, ID_OFFSET + 25) && player.getHealth() < player.getMaxHealth() * 0.2) {
            desperateEscapeEndTimes.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
            stats.addEvadeChance(25); // 25% evade
            applyMovementSpeedEffects(player); // Apply movement speed bonuses
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Desperate Escape activated!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Desperate Escape for " + player.getName());
            }
        }

        // Second Chance (Skill 27)
        if (isPurchased(playerId, ID_OFFSET + 27) && !isOnCooldown(playerId, secondChanceCooldowns, 300000)) { // 5 minute cooldown
            if (event.getFinalDamage() >= player.getHealth()) {
                // Cancel the damage
                event.setCancelled(true);

                // Set health to 10% of max
                player.setHealth(Math.max(1, player.getMaxHealth() * 0.1));

                // Apply immunity for 2 seconds using custom immunity system
                secondChanceCooldowns.put(playerId, System.currentTimeMillis());
                immunityEndTimes.put(playerId, System.currentTimeMillis() + 2000); // 2 seconds

                player.sendMessage(ChatColor.GOLD + "Your Second Chance has saved you from death!");
                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Second Chance activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Applied Second Chance for " + player.getName());
                }
            }
        }

        // If the damage is from an entity, track the attacker for Vengeance (Skill 13)
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = entityEvent.getDamager();

            if (attacker instanceof LivingEntity) {
                // Add attacker to the player's attackers set
                playerAttackers.computeIfAbsent(playerId, k -> new HashSet<>()).add(attacker.getUniqueId());

                // Trigger Protective Instinct for nearby allies (Skill 21)
                if (attacker instanceof Player) {
                    for (Player nearbyPlayer : player.getWorld().getPlayers()) {
                        if (nearbyPlayer.equals(player)) continue;

                        UUID nearbyId = nearbyPlayer.getUniqueId();
                        String nearbyAscendancy = plugin.getClassManager().getPlayerAscendancy(nearbyId);

                        if ("Earthwarden".equals(nearbyAscendancy) && isPurchased(nearbyId, ID_OFFSET + 21)) {
                            if (nearbyPlayer.getLocation().distance(player.getLocation()) <= 15) {
                                protectiveInstinctEndTimes.put(nearbyId, System.currentTimeMillis() + 5000); // 5 seconds
                                SkillEffectsHandler.PlayerSkillStats nearbyStats = plugin.getSkillEffectsHandler().getPlayerStats(nearbyPlayer);
                                nearbyStats.addBonusDamage(0.05); // 5% damage
                                plugin.getSkillEffectsHandler().refreshPlayerStats(nearbyPlayer);
                                applyMovementSpeedEffects(nearbyPlayer); // Apply movement speed bonuses
                                ActionBarUtils.sendActionBar(nearbyPlayer, ChatColor.GREEN + "Protective Instinct activated!");
                                if (debuggingFlag == 1) {
                                    plugin.getLogger().info("EARTHWARDEN: Applied Protective Instinct for " + nearbyPlayer.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle entity damage events
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

        if (!"Earthwarden".equals(ascendancy)) {
            return;
        }

        // Get player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        // Delegate to the handler method
        handleEntityDamage(event, player, stats);
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        Entity target = event.getEntity();

        // Entangling Strike (Skill 8)
        if (isPurchased(playerId, ID_OFFSET + 8) && entanglingStrikeReady.getOrDefault(playerId, false)) {
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1, false, false, true)); // 30% slow for 3 seconds
                entanglingStrikeReady.put(playerId, false);
                entanglingStrikeCooldowns.put(playerId, System.currentTimeMillis());
                ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Entangling Strike activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Applied Entangling Strike for " + player.getName());
                }
            }
        }

        // Vengeance (Skill 13)
        if (isPurchased(playerId, ID_OFFSET + 13) && target instanceof LivingEntity) {
            Set<UUID> attackers = playerAttackers.getOrDefault(playerId, new HashSet<>());
            if (attackers.contains(target.getUniqueId())) {
                double damageBonus = 0.08 * getPurchaseCount(playerId, ID_OFFSET + 13); // 8% per purchase
                event.setDamage(event.getDamage() * (1 + damageBonus));
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Applied Vengeance for " + player.getName() + " against " + target.getType());
                }
            }
        }

        // Lifesteal (Skill 14)
        if (isPurchased(playerId, ID_OFFSET + 14)) {
            boolean success = rollChance(10.0, player, "Lifesteal");
            
            if (success) {
                double healAmount = event.getFinalDamage() * 0.05; // 5% of damage dealt
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Lifesteal: +" + String.format("%.1f", healAmount) + " HP");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Healed " + String.format("%.1f", healAmount) + " HP (5% of damage dealt)");
                    plugin.getLogger().info("EARTHWARDEN: Applied Lifesteal for " + player.getName() + ", healed " + healAmount);
                }
            }
        }

        // Healing Strikes (Skill 22)
        if (isPurchased(playerId, ID_OFFSET + 22)) {
            boolean success = rollChance(25.0, player, "Healing Strikes");
            
            if (success) {
                double healAmount = player.getMaxHealth() * 0.02; // 2% of max health
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Healing Strike: +" + String.format("%.1f", healAmount) + " HP");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Healed " + String.format("%.1f", healAmount) + " HP (2% of max health)");
                    plugin.getLogger().info("EARTHWARDEN: Applied Healing Strike for " + player.getName() + ", healed " + healAmount);
                }
            }
        }

        // Ambush (Skill 24)
        if (isPurchased(playerId, ID_OFFSET + 24)) {
            long lastDamage = lastDamageTakenTime.getOrDefault(playerId, 0L);
            if (System.currentTimeMillis() - lastDamage >= 5000) { // 5 seconds
                event.setDamage(event.getDamage() * 1.3); // 30% more damage
                ActionBarUtils.sendActionBar(player, ChatColor.RED + "Ambush activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Applied Ambush for " + player.getName());
                }
            }
        }
    }

    /**
     * Handle entity damage by entity events
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Handle player attacking
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UUID playerId = player.getUniqueId();
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

            if (!"Earthwarden".equals(ascendancy)) {
                return;
            }

            // Get player stats
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

            // Delegate to the handler method
            handleEntityDamageByEntity(event, player, stats);
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Kill Heal (Skill 2)
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            int healAmount = getPurchaseCount(playerId, ID_OFFSET + 2); // 1 HP per purchase
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Kill Heal for " + player.getName() + ", healed " + healAmount);
            }
        }
    }

    /**
     * Handle entity death events
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        UUID playerId = killer.getUniqueId();
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

        if (!"Earthwarden".equals(ascendancy)) {
            return;
        }

        // Get player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(killer);

        // Delegate to the handler method
        handleEntityDeath(event, killer, stats);
    }

    /**
     * Handle player movement events
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

        if (!"Earthwarden".equals(ascendancy)) {
            return;
        }

        // Update last movement time if the player has moved significantly
        if (event.getFrom().distanceSquared(event.getTo()) > 0.001) {
            lastMovementTime.put(playerId, System.currentTimeMillis());
        }
    }

    /**
     * Check if the player is standing still for Steadfast Defense
     */
    private void checkSteadfastDefense(Player player) {
        UUID playerId = player.getUniqueId();
        long lastMove = lastMovementTime.getOrDefault(playerId, 0L);

        if (System.currentTimeMillis() - lastMove >= 3000) { // 3 seconds
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double defenseBonus = 0.1 * getPurchaseCount(playerId, ID_OFFSET + 5); // 10% per purchase
            stats.setDefenseBonus(defenseBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Steadfast Defense for " + player.getName() + ", " + defenseBonus + " defense");
            }
        }
    }

    /**
     * Check if the player is in a grassy area for Defense in Grassy Areas
     */
    private void checkGrassyAreaDefense(Player player) {
        UUID playerId = player.getUniqueId();
        Location loc = player.getLocation();
        Block block = loc.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);

        if (isGrassyArea(below)) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double defenseBonus = 0.03 * getPurchaseCount(playerId, ID_OFFSET + 1); // 3% per purchase
            stats.addDefenseBonus(defenseBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Grassy Area Defense for " + player.getName() + ", " + defenseBonus + " defense");
            }
        }
    }

    /**
     * Check for Entangling Strike cooldown
     */
    private void checkEntanglingStrikeCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long lastUse = entanglingStrikeCooldowns.getOrDefault(playerId, 0L);

        if (System.currentTimeMillis() - lastUse >= 10000) { // 10 seconds
            entanglingStrikeReady.put(playerId, true);
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Entangling Strike ready for " + player.getName());
            }
        }
    }

    /**
     * Apply Strength in Numbers (Skill 11)
     */
    private void applyStrengthInNumbers(Player player) {
        UUID playerId = player.getUniqueId();
        int nearbyAllies = 0;

        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            if (otherPlayer.getLocation().distance(player.getLocation()) <= 10) {
                nearbyAllies++;
            }
        }

        // Cap at 5 allies
        nearbyAllies = Math.min(nearbyAllies, 5);

        if (nearbyAllies > 0) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double defenseBonus = 0.01 * nearbyAllies * getPurchaseCount(playerId, ID_OFFSET + 11); // 1% per ally per purchase
            stats.addDefenseBonus(defenseBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Strength in Numbers for " + player.getName() + ", " + defenseBonus + " defense for " + nearbyAllies + " allies");
            }
        }
    }

    /**
     * Apply Allied Strength (Skill 26)
     */
    private void applyAlliedStrength(Player player) {
        UUID playerId = player.getUniqueId();
        int nearbyAllies = 0;

        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            if (otherPlayer.getLocation().distance(player.getLocation()) <= 10) {
                nearbyAllies++;
            }
        }

        // Cap at 5 allies
        nearbyAllies = Math.min(nearbyAllies, 5);

        if (nearbyAllies > 0) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double damageBonus = 0.02 * nearbyAllies * getPurchaseCount(playerId, ID_OFFSET + 26); // 2% per ally per purchase
            stats.addBonusDamage(damageBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Allied Strength for " + player.getName() + ", " + damageBonus + " damage for " + nearbyAllies + " allies");
            }
        }
    }

    /**
     * Apply Surrounded Defense (Skill 17)
     */
    private void applySurroundedDefense(Player player) {
        UUID playerId = player.getUniqueId();
        int nearbyEnemies = 0;

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                nearbyEnemies++;
            }
        }

        // Cap at 5 enemies
        nearbyEnemies = Math.min(nearbyEnemies, 5);

        if (nearbyEnemies > 0) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double defenseBonus = 0.03 * nearbyEnemies * getPurchaseCount(playerId, ID_OFFSET + 17); // 3% per enemy per purchase
            stats.addDefenseBonus(defenseBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Surrounded Defense for " + player.getName() + ", " + defenseBonus + " defense for " + nearbyEnemies + " enemies");
            }
        }
    }

    /**
     * Apply Surrounded Strength (Skill 23)
     */
    private void applySurroundedStrength(Player player) {
        UUID playerId = player.getUniqueId();
        int nearbyEnemies = 0;

        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                nearbyEnemies++;
            }
        }

        // Cap at 5 enemies
        nearbyEnemies = Math.min(nearbyEnemies, 5);

        if (nearbyEnemies > 0) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double damageBonus = 0.02 * nearbyEnemies * getPurchaseCount(playerId, ID_OFFSET + 23); // 2% per enemy per purchase
            stats.addBonusDamage(damageBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Surrounded Strength for " + player.getName() + ", " + damageBonus + " damage for " + nearbyEnemies + " enemies");
            }
        }
    }

    /**
     * Apply Healthy Speed (Skill 20)
     */
    private void applyHealthySpeed(Player player) {
        UUID playerId = player.getUniqueId();

        if (player.getHealth() > player.getMaxHealth() * 0.8) {
            applyMovementSpeedEffects(player);
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Healthy Speed for " + player.getName());
            }
        }
    }
    
    /**
     * Apply all movement speed effects to a player
     * This centralizes all movement speed bonuses to ensure they work together properly
     */
    private void applyMovementSpeedEffects(Player player) {
        UUID playerId = player.getUniqueId();
        float baseSpeed = 0.2f; // Base Minecraft walk speed
        float totalBonus = 1.0f;
        
        // Get player stats for base movement speed bonus
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        totalBonus += ((float)stats.getMovementSpeedBonus() / 100.0f);
        
        // Skill 20: When hp>80%, gain +10% movement speed
        if (isPurchased(playerId, ID_OFFSET + 20) && player.getHealth() > player.getMaxHealth() * 0.8) {
            int purchaseCount = getPurchaseCount(playerId, ID_OFFSET + 20);
            totalBonus += 0.1f * purchaseCount; // 10% per purchase
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Added Healthy Speed bonus: +" + 
                        (10 * purchaseCount) + "% movement speed");
            }
        }
        
        // Skill 25: When hp<20%, gain +15% movement speed for 5 seconds
        if (isPurchased(playerId, ID_OFFSET + 25) && 
                desperateEscapeEndTimes.containsKey(playerId) && 
                System.currentTimeMillis() < desperateEscapeEndTimes.get(playerId)) {
            totalBonus += 0.15f; // 15% bonus
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Added Desperate Escape bonus: +15% movement speed");
            }
        }
        
        // Skill 12: After blocking attack, gain +20% movement speed for 3 seconds
        if (isPurchased(playerId, ID_OFFSET + 12) && 
                counterattackEndTimes.containsKey(playerId) && 
                System.currentTimeMillis() < counterattackEndTimes.get(playerId)) {
            totalBonus += 0.2f; // 20% bonus
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Added Counterattack bonus: +20% movement speed");
            }
        }
        
        // Skill 21: When an ally is attacked, gain +10% movement speed for 5 seconds
        if (isPurchased(playerId, ID_OFFSET + 21) && 
                protectiveInstinctEndTimes.containsKey(playerId) && 
                System.currentTimeMillis() < protectiveInstinctEndTimes.get(playerId)) {
            totalBonus += 0.1f; // 10% bonus
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Added Protective Instinct bonus: +10% movement speed");
            }
        }
        
        // Apply the total movement speed bonus
        player.setWalkSpeed(Math.min(baseSpeed * totalBonus, 1.0f)); // Cap at 1.0
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("EARTHWARDEN: Applied total movement speed for " + player.getName() + 
                    ": " + (totalBonus * 100) + "% (walk speed: " + (baseSpeed * totalBonus) + ")");
        }
    }

    /**
     * Apply Desperate Strength (Skill 19)
     */
    private void applyDesperateStrength(Player player) {
        UUID playerId = player.getUniqueId();

        if (player.getHealth() < player.getMaxHealth() * 0.2) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double damageBonus = 0.15 * getPurchaseCount(playerId, ID_OFFSET + 19); // 15% per purchase
            stats.addBonusDamage(damageBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Desperate Strength for " + player.getName() + ", " + damageBonus + " damage");
            }
        }
    }

    /**
     * Check if a skill is on cooldown
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownDuration) {
        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }

        long lastUse = cooldownMap.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownDuration;
    }

    /**
     * Get the number of times a skill has been purchased
     */
    private int getPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }

    /**
     * Check if a skill has been purchased
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    /**
     * Check if damage is from environmental sources
     */
    private boolean isEnvironmentalDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FALL ||
               cause == EntityDamageEvent.DamageCause.FIRE ||
               cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
               cause == EntityDamageEvent.DamageCause.LAVA ||
               cause == EntityDamageEvent.DamageCause.DROWNING ||
               cause == EntityDamageEvent.DamageCause.SUFFOCATION ||
               cause == EntityDamageEvent.DamageCause.LIGHTNING;
    }

    /**
     * Check if a block is in a grassy area
     */
    private boolean isGrassyArea(Block block) {
        Material material = block.getType();
        return material == Material.GRASS_BLOCK ||
               material == Material.DIRT ||
               material == Material.PODZOL ||
               material == Material.MYCELIUM ||
               material == Material.FARMLAND;
    }
}