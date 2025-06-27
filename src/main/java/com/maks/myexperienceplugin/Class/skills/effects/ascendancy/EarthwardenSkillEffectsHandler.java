package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
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

    // Debug flag
    private final int debuggingFlag = 1;

    // Random for chance calculations
    private final Random random = new Random();

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
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // +3% defense in grassy areas
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 1: Will add 3% defense in grassy areas");
                }
                break;

            case 2: // Heal 1 hp after killing an enemy
                // This is handled in the EntityDeathEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 2: Will heal 1 hp after killing an enemy");
                }
                break;

            case 3: // +5% resistance to environmental damage
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 3: Will add 5% resistance to environmental damage");
                }
                break;

            case 4: // +2 hp per level
                // This is a fixed bonus based on skill level (not player level)
                // Each purchase (skill level) adds +2 HP
                double hpBonus = 2 * purchaseCount;
                stats.addMaxHealth(hpBonus);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 4: Added " + hpBonus + " HP (skill level " + purchaseCount + "/3)");
                }
                break;

            case 5: // After standing still for 3 seconds, gain +10% defense
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 5: Will add 10% defense after standing still for 3 seconds");
                }
                break;

            case 6: // +5% luck when below 50% hp
                // This is handled dynamically during gameplay
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 6: Will add 5% luck when below 50% hp");
                }
                break;

            case 7: // When hp<50%, gain +5% defense for 3 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 7: Will add 5% defense for 3 seconds when hp<50%");
                }
                break;

            case 8: // Every 10 seconds, your next attack roots the enemy
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 8: Will root enemies every 10 seconds");
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

            case 15: // When below 30% hp, gain +20% defense and +10% damage
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 15: Will add 20% defense and 10% damage when below 30% hp");
                }
                break;

            case 16: // Taking damage has 5% chance to grant immunity for 2 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 16: Will grant immunity for 2 seconds with 5% chance");
                }
                break;

            case 17: // +3% defense for each enemy within 8 blocks (max +15%)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 17: Will add 3% defense for each nearby enemy");
                }
                break;

            case 18: // Reduce knockback taken by 50% and gain +20% knockback dealt
                // We'll use defense as a proxy for knockback resistance
                stats.addDefenseBonus(0.5);
                stats.addBonusDamage(0.2); // Using damage as a proxy for knockback dealt
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 18: Added 50% knockback resistance and 20% knockback dealt (using proxies)");
                }
                break;

            case 19: // Gain +5% damage for every 30% of health you're missing
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 19: Will add damage based on missing health");
                }
                break;

            case 20: // While above 80% hp, gain +15% attack speed
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 20: Will add 15% attack speed when above 80% hp");
                }
                break;

            case 21: // When an ally takes damage, gain +10% movement speed and +5% damage for 5 seconds
                // This is handled in a separate event listener
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 21: Will add movement speed and damage when ally takes damage");
                }
                break;

            case 22: // Attacks have 25% chance to restore 2% of your maximum health
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 22: Will restore 2% max health with 25% chance on attacks");
                }
                break;

            case 23: // +15% defense when surrounded by 3+ enemies
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 23: Will add 15% defense when surrounded by 3+ enemies");
                }
                break;

            case 24: // After not taking damage for 5 seconds, your next attack deals +30% damage
                // This is handled in the EntityDamageByEntityEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 24: Will add 30% damage after not taking damage for 5 seconds");
                }
                break;

            case 25: // When hp<20%, gain +25% evade chance and +15% movement speed for 5 seconds
                // This is handled in the EntityDamageEvent
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 25: Will add evade chance and movement speed when hp<20%");
                }
                break;

            case 26: // +2% defense and +1% damage for each nearby ally (max 5 allies)
                // This is handled dynamically in the periodic effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN SKILL 26: Will add defense and damage based on nearby allies");
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
        if (isPurchased(playerId, ID_OFFSET + 10) && random.nextDouble() < (0.15 * getPurchaseCount(playerId, ID_OFFSET + 10))) {
            reactiveDefenseEndTimes.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
            stats.addDefenseBonus(0.15); // 15% defense
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Reactive Defense activated!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Reactive Defense for " + player.getName());
            }
        }

        // Counterattack (Skill 12)
        if (isPurchased(playerId, ID_OFFSET + 12) && player.isBlocking()) {
            counterattackEndTimes.put(playerId, System.currentTimeMillis() + 3000); // 3 seconds
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, false, true)); // 20% speed for 3 seconds
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
            if (random.nextDouble() < 0.05) {
                // Cancel the damage
                event.setCancelled(true);

                // Apply immunity for 2 seconds using custom immunity system
                divineProtectionCooldowns.put(playerId, System.currentTimeMillis());
                immunityEndTimes.put(playerId, System.currentTimeMillis() + 2000); // 2 seconds

                ActionBarUtils.sendActionBar(player, ChatColor.GOLD + "Divine Protection activated!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Applied Divine Protection for " + player.getName());
                }
            }
        }

        // Desperate Escape (Skill 25)
        if (isPurchased(playerId, ID_OFFSET + 25) && player.getHealth() < player.getMaxHealth() * 0.2) {
            desperateEscapeEndTimes.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
            stats.addEvadeChance(25); // 25% evade
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, true)); // 15% speed for 5 seconds
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
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, true)); // 10% speed for 5 seconds
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
        if (isPurchased(playerId, ID_OFFSET + 14) && random.nextDouble() < 0.1) {
            double healAmount = event.getFinalDamage() * 0.05; // 5% of damage dealt
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
            ActionBarUtils.sendActionBar(player, ChatColor.RED + "Lifesteal: +" + String.format("%.1f", healAmount) + " HP");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Lifesteal for " + player.getName() + ", healed " + healAmount);
            }
        }

        // Healing Strikes (Skill 22)
        if (isPurchased(playerId, ID_OFFSET + 22) && random.nextDouble() < 0.25) {
            double healAmount = player.getMaxHealth() * 0.02; // 2% of max health
            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Healing Strike: +" + String.format("%.1f", healAmount) + " HP");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Healing Strike for " + player.getName() + ", healed " + healAmount);
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

        boolean inGrassyArea = isGrassyBlock(below.getType());

        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        if (inGrassyArea) {
            double defenseBonus = 0.03 * getPurchaseCount(playerId, ID_OFFSET + 1); // 3% per purchase
            stats.addDefenseBonus(defenseBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Grassy Area Defense for " + player.getName() + ", " + defenseBonus + " defense");
            }
        }
    }

    /**
     * Check if a block type is considered "grassy"
     */
    private boolean isGrassyBlock(Material material) {
        return material == Material.GRASS_BLOCK || 
               material == Material.DIRT_PATH || 
               material == Material.PODZOL || 
               material == Material.MYCELIUM ||
               material == Material.MOSS_BLOCK;
    }

    /**
     * Check and update Entangling Strike cooldown
     */
    private void checkEntanglingStrikeCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        long lastUse = entanglingStrikeCooldowns.getOrDefault(playerId, 0L);

        if (System.currentTimeMillis() - lastUse >= 10000) { // 10 seconds
            if (!entanglingStrikeReady.getOrDefault(playerId, false)) {
                entanglingStrikeReady.put(playerId, true);
                ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Entangling Strike ready!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("EARTHWARDEN: Entangling Strike ready for " + player.getName());
                }
            }
        }
    }

    /**
     * Apply Strength in Numbers effect
     */
    private void applyStrengthInNumbers(Player player) {
        UUID playerId = player.getUniqueId();
        int allyCount = countNearbyAllies(player, 10);
        double defenseBonus = Math.min(0.05, allyCount * 0.01); // 1% per ally, max 5%

        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDefenseBonus(defenseBonus);
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);

        if (debuggingFlag == 1 && defenseBonus > 0) {
            plugin.getLogger().info("EARTHWARDEN: Applied Strength in Numbers for " + player.getName() + ", " + defenseBonus + " defense from " + allyCount + " allies");
        }
    }

    /**
     * Apply Allied Strength effect
     */
    private void applyAlliedStrength(Player player) {
        UUID playerId = player.getUniqueId();
        int allyCount = Math.min(5, countNearbyAllies(player, 10)); // Max 5 allies
        double defenseBonus = allyCount * 0.02 * getPurchaseCount(playerId, ID_OFFSET + 26); // 2% per ally per purchase
        double damageBonus = allyCount * 0.01 * getPurchaseCount(playerId, ID_OFFSET + 26); // 1% per ally per purchase

        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDefenseBonus(defenseBonus);
        stats.addBonusDamage(damageBonus);
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);

        if (debuggingFlag == 1 && allyCount > 0) {
            plugin.getLogger().info("EARTHWARDEN: Applied Allied Strength for " + player.getName() + ", " + defenseBonus + " defense and " + damageBonus + " damage from " + allyCount + " allies");
        }
    }

    /**
     * Apply Surrounded Defense effect
     */
    private void applySurroundedDefense(Player player) {
        UUID playerId = player.getUniqueId();
        int enemyCount = countNearbyEnemies(player, 8);
        double defenseBonus = Math.min(0.15, enemyCount * 0.03); // 3% per enemy, max 15%

        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDefenseBonus(defenseBonus);
        plugin.getSkillEffectsHandler().refreshPlayerStats(player);

        if (debuggingFlag == 1 && defenseBonus > 0) {
            plugin.getLogger().info("EARTHWARDEN: Applied Surrounded Defense for " + player.getName() + ", " + defenseBonus + " defense from " + enemyCount + " enemies");
        }
    }

    /**
     * Apply Surrounded Strength effect
     */
    private void applySurroundedStrength(Player player) {
        UUID playerId = player.getUniqueId();
        int enemyCount = countNearbyEnemies(player, 8);

        if (enemyCount >= 3) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            stats.addDefenseBonus(0.15); // 15% defense
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Surrounded Strength for " + player.getName() + ", 15% defense from " + enemyCount + " enemies");
            }
        }
    }

    /**
     * Apply Healthy Speed effect
     */
    private void applyHealthySpeed(Player player) {
        UUID playerId = player.getUniqueId();

        if (player.getHealth() > player.getMaxHealth() * 0.8) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double speedBonus = 0.15 * getPurchaseCount(playerId, ID_OFFSET + 20); // 15% per purchase
            // Using damage multiplier as a proxy for attack speed
            stats.addDamageMultiplier(speedBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Healthy Speed for " + player.getName() + ", " + speedBonus + " attack speed");
            }
        }
    }

    /**
     * Apply Desperate Strength effect
     */
    private void applyDesperateStrength(Player player) {
        UUID playerId = player.getUniqueId();
        double healthPercent = player.getHealth() / player.getMaxHealth();
        int missingHealthTiers = (int) ((1 - healthPercent) / 0.3); // Every 30% missing health

        if (missingHealthTiers > 0) {
            SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
            double damageBonus = missingHealthTiers * 0.05; // 5% per 30% missing health
            stats.addBonusDamage(damageBonus);
            plugin.getSkillEffectsHandler().refreshPlayerStats(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("EARTHWARDEN: Applied Desperate Strength for " + player.getName() + ", " + damageBonus + " damage from " + missingHealthTiers + " tiers of missing health");
            }
        }
    }

    /**
     * Count nearby allies (other players)
     */
    private int countNearbyAllies(Player player, double radius) {
        int count = 0;
        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player) && other.getLocation().distance(player.getLocation()) <= radius) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count nearby enemies (hostile mobs)
     */
    private int countNearbyEnemies(Player player, double radius) {
        int count = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check if a skill is purchased
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    /**
     * Get the purchase count for a skill
     */
    private int getPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }

    /**
     * Check if a skill is on cooldown
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownDuration) {
        long lastUse = cooldownMap.getOrDefault(playerId, 0L);
        return System.currentTimeMillis() - lastUse < cooldownDuration;
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
     * Clear player data when they disconnect
     */
    public void clearPlayerData(UUID playerId) {
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
    }
}
