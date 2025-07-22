package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handler for Beastmaster-specific skill effects
 */
public class BeastmasterSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
    // Constants
    private static final int WOLF_SUMMON_ID = 100001;
    private static final int BOAR_SUMMON_ID = 100002;
    private static final int BEAR_SUMMON_ID = 100003;
    private static final long WOLF_SUMMON_COOLDOWN = 60000; // 60 seconds
    private static final long BOAR_SUMMON_COOLDOWN = 120000; // 120 seconds (2 minutes)
    private static final long BEAR_SUMMON_COOLDOWN = 120000; // 120 seconds (2 minutes)

    // Track active summons by player
    private final Map<UUID, UUID> playerWolf = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerBoar = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerBear = new ConcurrentHashMap<>();

    // Track active summon skills for each player (for 2-summon limit)
    private final Map<UUID, Set<Integer>> activeSummonSkills = new ConcurrentHashMap<>();

    // Track additional wolves for Wolf Pack skill
    private final Map<UUID, List<UUID>> additionalWolves = new ConcurrentHashMap<>();

    // Track summon stats
    private final Map<UUID, Double> summonDamageMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> summonHealthMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> summonDefenseMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> summonAttackSpeedMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> summonMovementSpeedMultiplier = new ConcurrentHashMap<>();
    private final Map<UUID, Double> summonCritChance = new ConcurrentHashMap<>();

    // Track cooldowns for summon respawns
    private final Map<UUID, Long> wolfRespawnCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> boarRespawnCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> bearRespawnCooldowns = new ConcurrentHashMap<>();

    // Track cooldown notification tasks
    private final Map<UUID, BukkitTask> cooldownNotificationTasks = new ConcurrentHashMap<>();

    // Track bear guardian buffs
    private final Map<UUID, Boolean> bearGuardianActive = new ConcurrentHashMap<>();

    // Track boar frenzy stacks
    private final Map<UUID, Map<UUID, Long>> boarFrenzyExpiration = new ConcurrentHashMap<>();

    // Random for critical hit calculations
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

    public BeastmasterSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
        // Register this class as a listener for events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - 100000; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // Wolf summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 1: Wolf summon unlocked");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 1: Wolf summon unlocked (50 dmg/50 hp)");
                }
                break;
            case 2: // Boar summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 2: Boar summon unlocked");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 2: Boar summon unlocked (80 dmg/20 hp)");
                }
                break;
            case 3: // Bear summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 3: Bear summon unlocked");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 3: Bear summon unlocked (20 dmg/80 hp)");
                }
                break;
            case 4: // Wolves gain +5% ms
                // Will be applied when wolf is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 4: Will add 5% movement speed to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 4: Wolf +5% movement speed enabled");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 4: Wolves will gain +5% movement speed");
                }
                break;
            case 5: // Boars gain +15% dmg
                // Will be applied when boar is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 5: Will add 15% damage to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 5: Boars will gain +15% damage");
                }
                break;
            case 6: // Bears gain +10% hp
                // Will be applied when bear is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 6: Will add 10% health to bears when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 6: Bears will gain +10% health");
                }
                break;
            case 7: // Wolves gain +5% as
                // Will be applied when wolf is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 7: Will add 5% attack speed to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 7: Wolves will gain +5% attack speed");
                }
                break;
            case 8: // Boars gain +10% as
                // Will be applied when boar is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 8: Will add 10% attack speed to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 8: Boars will gain +10% attack speed");
                }
                break;
            case 9: // Summons gain +5% dmg
                // Will be applied to all active summons
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 9: Will add 5% damage to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 9: All summons will gain +5% damage");
                }
                break;
            case 10: // Bears gain +50% def
                // Will be applied when bear is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 10: Will add 50% defense to bears when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 10: Bears will gain +50% defense");
                }
                break;
            case 11: // Wolves gain 10% chance to crit
                // Will be applied when wolf is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 11: Will add 10% critical chance to wolves when summoned");
                }
                break;
            case 12: // Wolves gain +100hp
                // Will be applied when wolf is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 12: Will add 100 HP to wolves when summoned");
                }

                // Wolf Vitality skill (ID 12)
                break;
            case 13: // Boars gain 15% chance to crit
                // Will be applied when boar is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 13: Will add 15% critical chance to boars when summoned");
                }
                break;
            case 14: // Summons gain +10% dmg
                // Will be applied to all active summons
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 14: Will add 10% damage to all summons");
                }
                break;
            case 15: // When Bears hp<50% u and ur summons gain +10% def
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 15: Will handle bear guardian effect dynamically");
                }
                break;
            case 16: // Bears gain +200hp
                // Will be applied when bear is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 16: Will add 200 HP to bears when summoned");
                }
                break;
            case 17: // Wolves heal's u for 5% of dmg dealt
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 17: Will handle wolf healing dynamically");
                }
                break;
            case 18: // Summons gain +10% ms
                // Will be applied to all active summons
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 18: Will add 10% movement speed to all summons");
                }
                break;
            case 19: // Boars after killing enemy gains +7% as for 3s
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 19: Will handle boar frenzy dynamically");
                }
                break;
            case 20: // Bears heal for 10% hp each 10s
                // This is handled via periodic task
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 20: Will handle bear regeneration via periodic task");
                }
                break;
            case 21: // Summons gain +30% hp
                // Will be applied to all active summons
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 21: Will add 30% health to all summons");
                }
                break;
            case 22: // Wolves gain +10% hp
                // Will be applied when wolf is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 22: Will add 10% health to wolves when summoned");
                }
                break;
            case 23: // Boars gain 20% ms
                // Will be applied when boar is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 23: Will add 20% movement speed to boars when summoned");
                }
                break;
            case 24: // Summons gain +25% def
                // Will be applied to all active summons
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 24: Will add 25% defense to all summons");
                }
                break;
            case 25: // U summon 1 more wolf
                // Handled by the summon wolf method
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 25: Will summon an additional wolf");
                }
                break;
            case 26: // Boars gain +15% dmg and +15% as
                // Will be applied when boar is summoned
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 26: Will add 15% damage and 15% attack speed to boars when summoned");
                }
                break;
            case 27: // Heals ur summons for 5% of yours dmg dealt
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 27: Will handle summon healing dynamically");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown Beastmaster skill ID: " + skillId);
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Check if this is our summon taking damage
        Entity entity = event.getEntity();
        if (isSummonedEntity(playerId, entity) && entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // Handle summon defense bonuses
            applyDefenseToSummon(entity, event);

            // Update the nametag immediately after damage
            if (isWolfSummon(playerId, entity)) {
                updateSummonNameTag(livingEntity, player, "Wolf");
            } else if (isBoarSummon(playerId, entity)) {
                updateSummonNameTag(livingEntity, player, "Boar");
            } else if (isBearSummon(playerId, entity)) {
                updateSummonNameTag(livingEntity, player, "Bear");
            }

            // Check for Bear Guardian skill (ID 15)
            if (isPurchased(playerId, 100015) && isBearSummon(playerId, entity)) {
                double healthPercent = livingEntity.getHealth() / livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 100;

                if (healthPercent < 50 && !bearGuardianActive.getOrDefault(playerId, false)) {
                    // Activate Bear Guardian buff
                    bearGuardianActive.put(playerId, true);

                    // Apply defense bonus to player and all summons
                    applyBearGuardianBuff(player);

                    // Important defensive buff - show notification
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.GOLD + "Bear Guardian active! +10% DEF");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Bear Guardian activated for " + player.getName() +
                                " (+10% defense to player and all summons)");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bear Guardian activated (+10% DEF)");
                    }
                }
                else if (healthPercent >= 50 && bearGuardianActive.getOrDefault(playerId, false)) {
                    // Deactivate Bear Guardian buff
                    bearGuardianActive.put(playerId, false);

                    // Remove defense bonus
                    removeBearGuardianBuff(player);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Bear Guardian deactivated for " + player.getName());
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bear Guardian deactivated");
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        Entity attacker = event.getDamager();
        Entity target = event.getEntity();

        if (debuggingFlag == 1) {
            // Debug general damage event information
            if (isSummonedEntity(playerId, attacker) || isSummonedEntity(playerId, target)) {
                plugin.getLogger().info("[BEASTMASTER DAMAGE] Event: " + 
                        getEntityName(attacker) + " attacking " + getEntityName(target) + 
                        ", damage: " + event.getDamage() + 
                        ", final damage: " + event.getFinalDamage());
            }
        }

        // Check if this player is attacking their own summon (this is a SEPARATE check)
        if (isSummonedEntity(playerId, target) && attacker instanceof Player && attacker.getUniqueId().equals(playerId)) {
            // Cancel the event to prevent damage
            event.setCancelled(true);
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Prevented " + player.getName() + " from damaging their own summon");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] You cannot damage your own summons");
            }
            return;
        }

        // Check if our summons are attacking
        if (isSummonedEntity(playerId, attacker)) {
            // Apply damage bonuses
            applyDamageToSummonAttack(player, attacker, event);

            // Handle wolf lifesteal (ID 17)
            if (isPurchased(playerId, 100017) && isWolfSummon(playerId, attacker)) {
                double damage = event.getFinalDamage();
                double healAmount = damage * 0.05; // 5% of damage dealt

                // Heal the player
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
                player.setHealth(newHealth);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Wolf Healing: " + player.getName() + " healed for " +
                            healAmount + " HP from wolf attack dealing " + damage + " damage");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Wolf Healing: +" +
                            String.format("%.1f", healAmount) + " HP");
                }
            }
        }

        // Check if our summon is being damaged by another entity
        if (isSummonedEntity(playerId, target) && target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;

            // Update the nametag immediately after damage
            if (isWolfSummon(playerId, target)) {
                updateSummonNameTag(livingEntity, player, "Wolf");
            } else if (isBoarSummon(playerId, target)) {
                updateSummonNameTag(livingEntity, player, "Boar");
            } else if (isBearSummon(playerId, target)) {
                updateSummonNameTag(livingEntity, player, "Bear");
            }
        }

        // Check if player is attacking - handle summon healing (ID 27)
        if (attacker.equals(player) && isPurchased(playerId, 100027)) {
            double damage = event.getFinalDamage();
            double healAmount = damage * 0.05; // 5% of damage dealt

            // Heal all summons
            healAllSummons(player, healAmount);

            // Update nametags after healing
            updateAllSummonNameTags(player);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Pack Healing: " + player.getName() + "'s summons healed for " +
                        healAmount + " HP from player's attack dealing " + damage + " damage");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Pack Healing: +" +
                        String.format("%.1f", healAmount) + " HP to all summons");
            }
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        Entity killer = event.getEntity().getKiller();

        // Check if our summons killed something
        if (killer != null && isSummonedEntity(playerId, killer)) {
            // Award XP to the player for the kill
            String mobType = event.getEntityType().toString();
            double baseXpReward = plugin.getXpForMob(mobType);

            if (baseXpReward > 0) {
                // Apply bonus XP multiplier if enabled
                double bonusMultiplier = plugin.isBonusExpEnabled() ? plugin.getBonusExpValue() / 100.0 : 1.0;
                double finalXpReward = baseXpReward * bonusMultiplier;

                // Give XP to the player
                plugin.addXP(player, finalXpReward);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Player " + player.getName() + " received " + finalXpReward + 
                            " XP from summon kill of " + mobType);
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Received " + finalXpReward + 
                            " XP from summon kill");
                }
            }

            // Handle boar frenzy (ID 19)
            if (isPurchased(playerId, 100019) && isBoarSummon(playerId, killer)) {
                // Apply attack speed buff to this boar
                applyBoarFrenzy(player, killer);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Boar Frenzy activated for " + player.getName() +
                            "'s boar after killing " + event.getEntity().getType());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Boar Frenzy activated (+7% attack speed for 3s)");
                }
            }
        }

        // Check if one of our summons died
        if (isSummonedEntity(playerId, event.getEntity())) {
            Entity deadEntity = event.getEntity();

            // Set respawn cooldown based on summon type
            if (isWolfSummon(playerId, deadEntity)) {
                UUID deadEntityId = deadEntity.getUniqueId();

                // Check if it's the primary wolf or an additional wolf
                boolean isPrimaryWolf = playerWolf.containsKey(playerId) && 
                                       playerWolf.get(playerId).equals(deadEntityId);

                boolean isAdditionalWolf = false;
                if (additionalWolves.containsKey(playerId)) {
                    List<UUID> wolves = additionalWolves.get(playerId);
                    isAdditionalWolf = wolves.contains(deadEntityId);
                }

                // Handle primary wolf death
                if (isPrimaryWolf) {
                    wolfRespawnCooldowns.put(playerId, System.currentTimeMillis());
                    playerWolf.remove(playerId);

                    // Only untrack if there are no additional wolves left
                    if (!additionalWolves.containsKey(playerId) || additionalWolves.get(playerId).isEmpty()) {
                        untrackSummonSkill(playerId, WOLF_SUMMON_ID);
                    }

                    // Notify player that wolf died
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Your primary wolf has died! (60s respawn)");

                    // Schedule automatic respawn after 60 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && !playerWolf.containsKey(playerId)) {
                            // Only summon a new primary wolf
                            Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
                            wolf.setTamed(true);
                            wolf.setOwner(player);

                            // Apply wolf-specific bonuses
                            setWolfStats(player, wolf);

                            // Set custom name with player name and HP
                            updateSummonNameTag(wolf, player, "Wolf");

                            // Store reference to the primary wolf
                            playerWolf.put(playerId, wolf.getUniqueId());

                            // Track this summon type if not already tracked
                            if (!activeSummonSkills.containsKey(playerId) || 
                                !activeSummonSkills.get(playerId).contains(WOLF_SUMMON_ID)) {
                                trackSummonSkill(playerId, WOLF_SUMMON_ID);
                            }

                            // Show notification
                            ActionBarUtils.sendActionBar(player,
                                    ChatColor.GREEN + "Primary wolf respawned");
                        }
                    }, 20 * 60); // 60 seconds

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Primary wolf summon died for " + player.getName() +
                                " (respawn cooldown: 60s)");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Primary wolf died (60s respawn)");
                    }
                }

                // Handle additional wolf death
                if (isAdditionalWolf) {
                    // Remove this wolf from the additional wolves list
                    List<UUID> wolves = additionalWolves.get(playerId);
                    wolves.remove(deadEntityId);

                    // If no wolves left, remove the entry
                    if (wolves.isEmpty()) {
                        additionalWolves.remove(playerId);

                        // If primary wolf is also gone, untrack the skill
                        if (!playerWolf.containsKey(playerId)) {
                            untrackSummonSkill(playerId, WOLF_SUMMON_ID);
                        }
                    } else {
                        // Update the list
                        additionalWolves.put(playerId, wolves);
                    }

                    // Notify player that additional wolf died
                    ActionBarUtils.sendActionBar(player,
                            ChatColor.RED + "Your additional wolf has died! (60s respawn)");

                    // Schedule automatic respawn after 60 seconds
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && isPurchased(playerId, 100025)) {
                            // Check if we need to respawn an additional wolf
                            // Only respawn if there are no additional wolves at all
                            if (!additionalWolves.containsKey(playerId) || 
                                additionalWolves.get(playerId).isEmpty()) {

                                // Spawn a new additional wolf
                                Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
                                wolf.setTamed(true);
                                wolf.setOwner(player);

                                // Apply wolf-specific bonuses
                                setWolfStats(player, wolf);

                                // Set custom name with player name and HP
                                updateSummonNameTag(wolf, player, "Wolf");

                                // Add to additional wolves
                                List<UUID> newWolves = additionalWolves.getOrDefault(
                                    playerId, new ArrayList<>());
                                newWolves.add(wolf.getUniqueId());
                                additionalWolves.put(playerId, newWolves);

                                // Track this summon type if not already tracked
                                if (!activeSummonSkills.containsKey(playerId) || 
                                    !activeSummonSkills.get(playerId).contains(WOLF_SUMMON_ID)) {
                                    trackSummonSkill(playerId, WOLF_SUMMON_ID);
                                }

                                // Show notification
                                ActionBarUtils.sendActionBar(player,
                                        ChatColor.GREEN + "Additional wolf respawned");
                            }
                        }
                    }, 20 * 60); // 60 seconds

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Additional wolf summon died for " + player.getName() +
                                " (respawn cooldown: 60s)");
                        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Additional wolf died (60s respawn)");
                    }
                }
            }
            else if (isBoarSummon(playerId, deadEntity)) {
                boarRespawnCooldowns.put(playerId, System.currentTimeMillis());
                playerBoar.remove(playerId);
                untrackSummonSkill(playerId, BOAR_SUMMON_ID);

                // Notify player that boar died
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Your boar has died! (60s respawn)");

                // Schedule automatic respawn after 60 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && !hasActiveSummon(playerId, playerBoar) && !playerBoar.containsKey(playerId)) {
                        summonBoars(player);
                    }
                }, 20 * 60); // 60 seconds

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Boar summon died for " + player.getName() +
                            " (respawn cooldown: 60s)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Boar summon died (60s respawn)");
                }
            }
            else if (isBearSummon(playerId, deadEntity)) {
                bearRespawnCooldowns.put(playerId, System.currentTimeMillis());
                playerBear.remove(playerId);
                untrackSummonSkill(playerId, BEAR_SUMMON_ID);

                // Notify player that bear died
                ActionBarUtils.sendActionBar(player,
                        ChatColor.RED + "Your bear has died! (60s respawn)");

                // Schedule automatic respawn after 60 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && !hasActiveSummon(playerId, playerBear) && !playerBear.containsKey(playerId)) {
                        summonBears(player);
                    }
                }, 20 * 60); // 60 seconds

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Bear summon died for " + player.getName() +
                            " (respawn cooldown: 60s)");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bear summon died (60s respawn)");
                }

                // If Bear Guardian was active, remove it
                if (bearGuardianActive.getOrDefault(playerId, false)) {
                    bearGuardianActive.put(playerId, false);
                    removeBearGuardianBuff(player);
                }
            }
        }
    }

    /**
     * Summons a wolf for the player
     * @param player The player
     */
    public void summonWolves(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has the skill
        if (!isPurchased(playerId, WOLF_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You haven't learned to summon wolves yet!");
            return;
        }

        // Check summon type limit
        if (!canAddSummonType(playerId, WOLF_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You can only have 2 types of summons at once!");
            return;
        }

        // Check if wolf is already summoned
        if (hasActiveSummon(playerId, playerWolf)) {
            player.sendMessage(ChatColor.YELLOW + "You already have a wolf summoned!");
            return;
        }

        // Check cooldown
        if (isOnCooldown(playerId, wolfRespawnCooldowns, WOLF_SUMMON_COOLDOWN)) {
            long timeLeft = (wolfRespawnCooldowns.get(playerId) + WOLF_SUMMON_COOLDOWN - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + "Wolf summon on cooldown (" + timeLeft + "s)");
            return;
        }

        // Determine how many wolves to summon
        int wolfCount = 1;
        if (isPurchased(playerId, 100025)) {
            wolfCount = 2; // Additional wolf with skill 25
        }

        // Array to track all wolves spawned
        List<Wolf> spawnedWolves = new ArrayList<>();

        // Summon wolves
        try {
            for (int i = 0; i < wolfCount; i++) {
                Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
                wolf.setTamed(true);
                wolf.setOwner(player);
                spawnedWolves.add(wolf);

                // Apply wolf-specific bonuses
                setWolfStats(player, wolf);

                // Set custom name with player name and HP
                updateSummonNameTag(wolf, player, "Wolf");
            }

            // Store all wolves - with Wolf Pack we need to handle multiple wolves
            if (!spawnedWolves.isEmpty()) {
                // Store reference to the primary wolf
                playerWolf.put(playerId, spawnedWolves.get(0).getUniqueId());

                // Store additional wolves if any
                if (spawnedWolves.size() > 1) {
                    additionalWolves.put(playerId, spawnedWolves.stream()
                        .skip(1)
                        .map(Entity::getUniqueId)
                        .collect(Collectors.toList()));
                }
            }

            // Show notification
            ActionBarUtils.sendActionBar(player,
                    ChatColor.GREEN + "Summoned " + wolfCount + " wolf" + (wolfCount > 1 ? "ves" : ""));

            // Track this summon type
            trackSummonSkill(playerId, WOLF_SUMMON_ID);

        } catch (Exception e) {
            plugin.getLogger().severe("Error spawning wolf: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error summoning wolf: " + e.getMessage());
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info(player.getName() + " summoned " + wolfCount + " wolves");
            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Summoned " + wolfCount + " wolves");
        }
    }

    /**
     * Summons a boar for the player
     * @param player The player
     */
    public void summonBoars(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has the skill
        if (!isPurchased(playerId, BOAR_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You haven't learned to summon boars yet!");
            return;
        }

        // Check summon type limit
        if (!canAddSummonType(playerId, BOAR_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You can only have 2 types of summons at once!");
            return;
        }

        // Check if boar is already summoned
        if (hasActiveSummon(playerId, playerBoar)) {
            player.sendMessage(ChatColor.YELLOW + "You already have a boar summoned!");
            return;
        }

        // Check cooldown
        if (isOnCooldown(playerId, boarRespawnCooldowns, BOAR_SUMMON_COOLDOWN)) {
            long timeLeft = (boarRespawnCooldowns.get(playerId) + BOAR_SUMMON_COOLDOWN - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + "Boar summon on cooldown (" + timeLeft + "s)");
            return;
        }

        // Summon a wolf that will be disguised as a boar
        Wolf boar = player.getWorld().spawn(player.getLocation(), Wolf.class);
        boar.setTamed(true);
        boar.setOwner(player);

        // Apply boar-specific bonuses
        setBoarStats(player, boar);

        // Set custom name with player name and HP
        updateSummonNameTag(boar, player, "Boar");

        // Store boar in map
        playerBoar.put(playerId, boar.getUniqueId());

        // Try to disguise the boar as a pig using LibsDisguises if available
        try {
            // Check if LibsDisguises plugin is available
            if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                // Use reflection to avoid direct dependency
                Class<?> disguiseAPIClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
                Class<?> pigDisguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.MobDisguise");
                Class<?> disguiseTypeClass = Class.forName("me.libraryaddict.disguise.disguisetypes.DisguiseType");

                // Get the PIG enum value
                Object pigType = disguiseTypeClass.getField("PIG").get(null);

                // Create a new PigDisguise
                Object pigDisguise = pigDisguiseClass.getConstructor(disguiseTypeClass).newInstance(pigType);

                // Disguise the entity
                disguiseAPIClass.getMethod("disguiseEntity", org.bukkit.entity.Entity.class, Class.forName("me.libraryaddict.disguise.disguisetypes.Disguise"))
                    .invoke(null, boar, pigDisguise);
            }
        } catch (Exception e) {
            // LibsDisguises not available or error occurred
        }

        // Show notification for important ability
        ActionBarUtils.sendActionBar(player,
                ChatColor.GREEN + "Summoned a boar");

        // Track this summon type
        trackSummonSkill(playerId, BOAR_SUMMON_ID);
    }

    /**
     * Summons a bear for the player
     * @param player The player
     */
    public void summonBears(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if player has the skill
        if (!isPurchased(playerId, BEAR_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You haven't learned to summon bears yet!");
            return;
        }

        // Check summon type limit
        if (!canAddSummonType(playerId, BEAR_SUMMON_ID)) {
            player.sendMessage(ChatColor.RED + "You can only have 2 types of summons at once!");
            return;
        }

        // Check if bear is already summoned
        if (hasActiveSummon(playerId, playerBear)) {
            player.sendMessage(ChatColor.YELLOW + "You already have a bear summoned!");
            return;
        }

        // Check cooldown
        if (isOnCooldown(playerId, bearRespawnCooldowns, BEAR_SUMMON_COOLDOWN)) {
            long timeLeft = (bearRespawnCooldowns.get(playerId) + BEAR_SUMMON_COOLDOWN - System.currentTimeMillis()) / 1000;
            player.sendMessage(ChatColor.RED + "Bear summon on cooldown (" + timeLeft + "s)");
            return;
        }

        // Summon a wolf that will be disguised as a bear
        Wolf bear = player.getWorld().spawn(player.getLocation(), Wolf.class);
        bear.setTamed(true);
        bear.setOwner(player);

        // Apply bear-specific bonuses
        setBearStats(player, bear);

        // Set custom name with player name and HP
        updateSummonNameTag(bear, player, "Bear");

        // Store bear in map
        playerBear.put(playerId, bear.getUniqueId());

        // Try to disguise the bear as a polar bear using LibsDisguises if available
        try {
            // Check if LibsDisguises plugin is available
            if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                // Use reflection to avoid direct dependency
                Class<?> disguiseAPIClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");
                Class<?> bearDisguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.MobDisguise");
                Class<?> disguiseTypeClass = Class.forName("me.libraryaddict.disguise.disguisetypes.DisguiseType");

                // Get the POLAR_BEAR enum value
                Object polarBearType = disguiseTypeClass.getField("POLAR_BEAR").get(null);

                // Create a new PolarBearDisguise
                Object polarBearDisguise = bearDisguiseClass.getConstructor(disguiseTypeClass).newInstance(polarBearType);

                // Disguise the entity
                disguiseAPIClass.getMethod("disguiseEntity", org.bukkit.entity.Entity.class, Class.forName("me.libraryaddict.disguise.disguisetypes.Disguise"))
                    .invoke(null, bear, polarBearDisguise);
            }
        } catch (Exception e) {
            // LibsDisguises not available or error occurred
        }

        // Show notification for important ability
        ActionBarUtils.sendActionBar(player,
                ChatColor.GREEN + "Summoned a bear");

        // Track this summon type
        trackSummonSkill(playerId, BEAR_SUMMON_ID);
    }

    /**
     * Apply periodic effects for Beastmaster summons
     */
    public void applyPeriodicEffects() {
        // Update name tags and handle bear regeneration
        for (UUID playerId : new HashSet<>(playerBear.keySet())) {
            Player owner = Bukkit.getPlayer(playerId);
            if (owner == null || !owner.isOnline()) continue;

            // Update bear name tag
            UUID bearId = playerBear.get(playerId);
            Entity entity = Bukkit.getEntity(bearId);

            if (entity != null && entity instanceof LivingEntity) {
                LivingEntity bear = (LivingEntity) entity;

                // Update name tag
                updateSummonNameTag(bear, owner, "Bear");

                // Handle bear regeneration (ID 20)
                if (isPurchased(playerId, 100020)) {
                    double maxHealth = bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double healAmount = maxHealth * 0.10; // 10% of max health
                    double newHealth = Math.min(bear.getHealth() + healAmount, maxHealth);
                    bear.setHealth(newHealth);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Bear Regeneration: " + owner.getName() +
                                "'s bear healed for " + healAmount + " HP");
                        owner.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Bear healed for " +
                                String.format("%.1f", healAmount) + " HP");
                    }
                }
            }
        }

        // Update wolf name tags
        for (UUID playerId : new HashSet<>(playerWolf.keySet())) {
            Player owner = Bukkit.getPlayer(playerId);
            if (owner == null || !owner.isOnline()) continue;

            UUID wolfId = playerWolf.get(playerId);
            Entity entity = Bukkit.getEntity(wolfId);

            if (entity != null && entity instanceof LivingEntity) {
                updateSummonNameTag((LivingEntity) entity, owner, "Wolf");
            }
        }

        // Update boar name tags
        for (UUID playerId : new HashSet<>(playerBoar.keySet())) {
            Player owner = Bukkit.getPlayer(playerId);
            if (owner == null || !owner.isOnline()) continue;

            UUID boarId = playerBoar.get(playerId);
            Entity entity = Bukkit.getEntity(boarId);

            if (entity != null && entity instanceof LivingEntity) {
                updateSummonNameTag((LivingEntity) entity, owner, "Boar");
            }
        }
    }

    /**
     * Set appropriate stats for a wolf
     */
    private void setWolfStats(Player player, Wolf wolf) {
        UUID playerId = player.getUniqueId();

        // Base wolf - 50 HP (fixed value, not based on player)
        double baseHealth = 50.0;  // Fixed base value
        double finalHealth = baseHealth;

        // Calculate damage multipliers - wolves have 50 dmg as per requirements
        double damageMultiplier = 0.5; // 50% damage (50 dmg)

        // Additional global damage bonuses (ID 9, ID 14)
        if (isPurchased(playerId, 100009)) {
            damageMultiplier += 0.05; // +5% damage
        }
        if (isPurchased(playerId, 100014)) {
            damageMultiplier += 0.10; // +10% damage
        }

        // Wolf Vitality skill (ID 12) - +100 flat HP
        if (isPurchased(playerId, 100012)) {
            finalHealth += 100;
        }

        // Wolf Health skill (ID 22) - +10% HP
        if (isPurchased(playerId, 100022)) {
            finalHealth *= 1.10; // +10% health
        }

        // Global health bonus (ID 21)
        if (isPurchased(playerId, 100021)) {
            finalHealth *= 1.30; // +30% health
        }

        // Set the wolf's health
        wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        wolf.setHealth(finalHealth);

        // Store damage multiplier for combat
        summonDamageMultiplier.put(wolf.getUniqueId(), damageMultiplier);

        // Movement speed bonuses (ID 4, ID 18)
        double moveSpeedBonus = 0.0;
        if (isPurchased(playerId, 100004)) {
            moveSpeedBonus += 0.05; // +5% movement speed
        }
        if (isPurchased(playerId, 100018)) {
            moveSpeedBonus += 0.10; // +10% movement speed for all summons
        }
        if (moveSpeedBonus > 0) {
            // Get the original vanilla wolf movement speed (0.3)
            double vanillaWolfSpeed = 0.3;

            // Set the speed directly using the vanilla value as base
            wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                vanillaWolfSpeed * (1 + moveSpeedBonus));

            summonMovementSpeedMultiplier.put(wolf.getUniqueId(), moveSpeedBonus);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Wolf speed set to " + 
                    (vanillaWolfSpeed * (1 + moveSpeedBonus)) + 
                    " (vanilla: " + vanillaWolfSpeed + ", bonus: " + moveSpeedBonus + ")");
            }
        }

        // Attack speed bonuses (ID 7)
        double attackSpeedBonus = 0.0;
        if (isPurchased(playerId, 100007)) {
            attackSpeedBonus += 0.05; // +5% attack speed
        }
        if (attackSpeedBonus > 0) {
            summonAttackSpeedMultiplier.put(wolf.getUniqueId(), attackSpeedBonus);
        }

        // Critical chance (ID 11)
        if (isPurchased(playerId, 100011)) {
            summonCritChance.put(wolf.getUniqueId(), 0.10); // 10% crit chance
        }

        // Global defense bonus (ID 24)
        if (isPurchased(playerId, 100024)) {
            summonDefenseMultiplier.put(wolf.getUniqueId(), 0.25); // +25% defense
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Set wolf stats for " + player.getName() + ": health=" + finalHealth +
                    ", damage multiplier=" + damageMultiplier +
                    ", move speed bonus=" + moveSpeedBonus +
                    ", attack speed bonus=" + attackSpeedBonus);
        }
    }

    /**
     * Set appropriate stats for a boar (actually a wolf with boar stats)
     */
    private void setBoarStats(Player player, Wolf boar) {
        UUID playerId = player.getUniqueId();

        // Base boar - 20 HP (fixed value, not based on player)
        double baseHealth = 20.0;  // Fixed base value
        double finalHealth = baseHealth;

        // Calculate damage bonuses - boars have 80 dmg as per requirements
        double damageMultiplier = 0.8; // 80% damage (80 dmg)

        // Additional global damage bonuses (ID 9, ID 14)
        if (isPurchased(playerId, 100009)) {
            damageMultiplier += 0.05; // +5% damage
        }
        if (isPurchased(playerId, 100014)) {
            damageMultiplier += 0.10; // +10% damage
        }

        // Boar damage skill (ID 5) - +15% damage
        if (isPurchased(playerId, 100005)) {
            damageMultiplier += 0.15;
        }

        // Boar rage skill (ID 26) - +15% damage
        if (isPurchased(playerId, 100026)) {
            damageMultiplier += 0.15;
        }

        // Global health bonus (ID 21)
        if (isPurchased(playerId, 100021)) {
            finalHealth *= 1.30; // +30% health
        }

        // Set the boar's health
        boar.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        boar.setHealth(finalHealth);

        // Store damage multiplier for combat
        summonDamageMultiplier.put(boar.getUniqueId(), damageMultiplier);

        // Movement speed bonuses (ID 18, ID 23)
        double moveSpeedBonus = 0.0;
        if (isPurchased(playerId, 100018)) {
            moveSpeedBonus += 0.10; // +10% movement speed for all summons
        }
        if (isPurchased(playerId, 100023)) {
            moveSpeedBonus += 0.20; // +20% movement speed for boars
        }
        if (moveSpeedBonus > 0) {
            // Get the original vanilla wolf movement speed (0.3)
            double vanillaWolfSpeed = 0.3;

            // Set the speed directly using the vanilla value as base
            boar.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                vanillaWolfSpeed * (1 + moveSpeedBonus));

            summonMovementSpeedMultiplier.put(boar.getUniqueId(), moveSpeedBonus);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Boar speed set to " + 
                    (vanillaWolfSpeed * (1 + moveSpeedBonus)) + 
                    " (vanilla: " + vanillaWolfSpeed + ", bonus: " + moveSpeedBonus + ")");
            }
        }

        // Attack speed bonuses (ID 8, ID 26)
        double attackSpeedBonus = 0.0;
        if (isPurchased(playerId, 100008)) {
            attackSpeedBonus += 0.10; // +10% attack speed
        }
        if (isPurchased(playerId, 100026)) {
            attackSpeedBonus += 0.15; // +15% attack speed
        }
        if (attackSpeedBonus > 0) {
            summonAttackSpeedMultiplier.put(boar.getUniqueId(), attackSpeedBonus);
        }

        // Critical chance (ID 13)
        if (isPurchased(playerId, 100013)) {
            summonCritChance.put(boar.getUniqueId(), 0.15); // 15% crit chance
        }

        // Global defense bonus (ID 24)
        if (isPurchased(playerId, 100024)) {
            summonDefenseMultiplier.put(boar.getUniqueId(), 0.25); // +25% defense
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Set boar stats for " + player.getName() + ": health=" + finalHealth +
                    ", damage multiplier=" + damageMultiplier +
                    ", move speed bonus=" + moveSpeedBonus +
                    ", attack speed bonus=" + attackSpeedBonus);
        }
    }

    /**
     * Set appropriate stats for a bear (actually a wolf with bear stats)
     */
    private void setBearStats(Player player, Wolf bear) {
        UUID playerId = player.getUniqueId();

        // Base bear - 80 HP (fixed value, not based on player)
        double baseHealth = 80.0;  // Fixed base value
        double finalHealth = baseHealth;

        // Calculate damage bonuses - bears have 20 dmg as per requirements
        double damageMultiplier = 0.2; // 20% damage (20 dmg)

        // Additional global damage bonuses (ID 9, ID 14)
        if (isPurchased(playerId, 100009)) {
            damageMultiplier += 0.05; // +5% damage
        }
        if (isPurchased(playerId, 100014)) {
            damageMultiplier += 0.10; // +10% damage
        }

        // Bear Health skill (ID 6) - +10% health
        if (isPurchased(playerId, 100006)) {
            finalHealth *= 1.10;
        }

        // Bear Vitality skill (ID 16) - +200 flat HP
        if (isPurchased(playerId, 100016)) {
            finalHealth += 200;
        }

        // Global health bonus (ID 21)
        if (isPurchased(playerId, 100021)) {
            finalHealth *= 1.30; // +30% health
        }

        // Set the bear's health
        bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        bear.setHealth(finalHealth);

        // Store damage multiplier for combat
        summonDamageMultiplier.put(bear.getUniqueId(), damageMultiplier);

        // Apply defense bonus (ID 10, ID 24)
        double defenseBonus = 0.0;
        if (isPurchased(playerId, 100010)) {
            defenseBonus += 0.50; // +50% defense for bears
        }
        if (isPurchased(playerId, 100024)) {
            defenseBonus += 0.25; // +25% defense for all summons
        }
        if (defenseBonus > 0) {
            summonDefenseMultiplier.put(bear.getUniqueId(), defenseBonus);
        }

        // Movement speed bonuses (ID 18)
        double moveSpeedBonus = 0.0;
        if (isPurchased(playerId, 100018)) {
            moveSpeedBonus += 0.10; // +10% movement speed for all summons
        }
        if (moveSpeedBonus > 0) {
            // Get the original vanilla wolf movement speed (0.3)
            double vanillaWolfSpeed = 0.3;

            // Set the speed directly using the vanilla value as base
            bear.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                vanillaWolfSpeed * (1 + moveSpeedBonus));

            summonMovementSpeedMultiplier.put(bear.getUniqueId(), moveSpeedBonus);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Bear speed set to " + 
                    (vanillaWolfSpeed * (1 + moveSpeedBonus)) + 
                    " (vanilla: " + vanillaWolfSpeed + ", bonus: " + moveSpeedBonus + ")");
            }
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Set bear stats for " + player.getName() + ": health=" + finalHealth +
                    ", damage multiplier=" + damageMultiplier +
                    ", defense bonus=" + defenseBonus +
                    ", move speed bonus=" + moveSpeedBonus);
        }
    }

    /**
     * Apply Bear Guardian buff to player and all summons
     */
    private void applyBearGuardianBuff(Player player) {
        UUID playerId = player.getUniqueId();

        // Get all summons
        Set<UUID> allSummons = new HashSet<>();
        if (playerWolf.containsKey(playerId)) allSummons.add(playerWolf.get(playerId));
        if (playerBoar.containsKey(playerId)) allSummons.add(playerBoar.get(playerId));
        if (playerBear.containsKey(playerId)) allSummons.add(playerBear.get(playerId));

        // Apply 10% defense boost to all summons
        for (UUID summonId : allSummons) {
            double currentDefense = summonDefenseMultiplier.getOrDefault(summonId, 0.0);
            summonDefenseMultiplier.put(summonId, currentDefense + 0.10);
        }

        // Apply defense boost to player via player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDefenseBonus(10); // +10% defense

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied Bear Guardian buff to " + player.getName() +
                    " and " + allSummons.size() + " summons (+10% defense)");
        }
    }

    /**
     * Remove Bear Guardian buff from player and summons
     */
    private void removeBearGuardianBuff(Player player) {
        UUID playerId = player.getUniqueId();

        // Get all summons
        Set<UUID> allSummons = new HashSet<>();
        if (playerWolf.containsKey(playerId)) allSummons.add(playerWolf.get(playerId));
        if (playerBoar.containsKey(playerId)) allSummons.add(playerBoar.get(playerId));
        if (playerBear.containsKey(playerId)) allSummons.add(playerBear.get(playerId));

        // Remove 10% defense from all summons
        for (UUID summonId : allSummons) {
            double currentDefense = summonDefenseMultiplier.getOrDefault(summonId, 0.0);
            summonDefenseMultiplier.put(summonId, Math.max(0, currentDefense - 0.10));
        }

        // Remove defense boost from player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        stats.addDefenseBonus(-10); // Remove 10% defense

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Removed Bear Guardian buff from " + player.getName() +
                    " and " + allSummons.size() + " summons (-10% defense)");
        }
    }

    /**
     * Apply Boar Frenzy buff to a specific boar
     */
    private void applyBoarFrenzy(Player player, Entity boar) {
        UUID playerId = player.getUniqueId();
        UUID boarId = boar.getUniqueId();

        // Set expiration time (3 seconds from now)
        Map<UUID, Long> frenzyMap = boarFrenzyExpiration.computeIfAbsent(playerId, k -> new HashMap<>());
        frenzyMap.put(boarId, System.currentTimeMillis() + 3000);

        // Apply attack speed bonus
        double currentSpeed = summonAttackSpeedMultiplier.getOrDefault(boarId, 0.0);
        summonAttackSpeedMultiplier.put(boarId, currentSpeed + 0.07); // +7% attack speed

        // Schedule task to remove bonus after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if the frenzy is still active (could have been renewed)
            Long expiryTime = frenzyMap.get(boarId);
            if (expiryTime != null && System.currentTimeMillis() >= expiryTime) {
                // Remove the attack speed bonus
                double speed = summonAttackSpeedMultiplier.getOrDefault(boarId, 0.0);
                summonAttackSpeedMultiplier.put(boarId, Math.max(0, speed - 0.07));
                frenzyMap.remove(boarId);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Boar Frenzy expired for " + player.getName() + "'s boar");
                }
            }
        }, 60L); // 3 seconds = 60 ticks

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Applied Boar Frenzy to " + player.getName() + "'s boar (+7% attack speed for 3s)");
        }
    }

    /**
     * Apply defense bonuses to a summon taking damage
     */
    private void applyDefenseToSummon(Entity entity, EntityDamageEvent event) {
        UUID summonId = entity.getUniqueId();

        // Check if summon has defense bonus
        double defenseBonus = summonDefenseMultiplier.getOrDefault(summonId, 0.0);

        if (defenseBonus > 0) {
            // Apply defense reduction
            double reduction = Math.min(0.80, defenseBonus); // Cap at 80% reduction
            double damage = event.getDamage();
            double reducedDamage = damage * (1 - reduction);
            event.setDamage(reducedDamage);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied defense bonus to summon: " +
                        (defenseBonus * 100) + "% reduction (" + damage + " → " + reducedDamage + ")");
            }
        }
    }

    /**
     * Apply damage bonuses to a summon's attack
     */
    private void applyDamageToSummonAttack(Player player, Entity summon, EntityDamageByEntityEvent event) {
        UUID summonId = summon.getUniqueId();
        UUID playerId = player.getUniqueId();

        // Use fixed base damage values based on summon type
        double baseDamage = 0.0;

        // Determine base damage by summon type
        if (isWolfSummon(playerId, summon) || 
            (additionalWolves.containsKey(playerId) && 
             additionalWolves.get(playerId).contains(summonId))) {
            baseDamage = 50.0; // Wolf base damage
        } else if (isBoarSummon(playerId, summon)) {
            baseDamage = 80.0; // Boar base damage
        } else if (isBearSummon(playerId, summon)) {
            baseDamage = 20.0; // Bear base damage
        }

        // Apply damage multipliers directly
        double multiplier = summonDamageMultiplier.getOrDefault(summonId, 1.0);
        double damage = baseDamage * multiplier;

        // Check for critical hit
        double critChance = summonCritChance.getOrDefault(summonId, 0.0);
        boolean isCrit = false;

        if (critChance > 0 && random.nextDouble() < critChance) {
            damage *= 2.0; // Critical hits deal double damage
            isCrit = true;

            // Show notification for critical hits
            ActionBarUtils.sendActionBar(player,
                    ChatColor.RED + "Summon Critical Hit!");
        }

        // IMPORTANT: Set the base damage directly instead of modifying the existing damage
        // This is the key fix - we're completely overriding the damage value
        event.setDamage(damage);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Summon attack damage: " + damage + 
                    " (base damage: " + baseDamage + 
                    ", multiplier: " + multiplier + 
                    (isCrit ? ", CRITICAL HIT!" : "") + ")");
            player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Summon attack: " + 
                    baseDamage + " × " + multiplier + " = " + damage + " damage" + 
                    (isCrit ? " (CRIT!)" : ""));
        }
    }

    /**
     * Heal all summons for a specific amount
     */
    private void healAllSummons(Player player, double amount) {
        UUID playerId = player.getUniqueId();

        // Get all summons
        Set<UUID> allSummons = new HashSet<>();
        if (playerWolf.containsKey(playerId)) allSummons.add(playerWolf.get(playerId));
        if (playerBoar.containsKey(playerId)) allSummons.add(playerBoar.get(playerId));
        if (playerBear.containsKey(playerId)) allSummons.add(playerBear.get(playerId));

        // Heal each summon
        for (UUID summonId : allSummons) {
            Entity entity = Bukkit.getEntity(summonId);
            if (entity != null && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = Math.min(livingEntity.getHealth() + amount, maxHealth);
                livingEntity.setHealth(newHealth);
            }
        }
    }

    /**
     * Check if player has purchased a specific skill
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    /**
     * Checks if adding a new summon type would exceed the limit
     * @param playerId Player UUID
     * @param skillId The summon skill being used
     * @return true if the player can summon, false if it would exceed the limit
     */
    private boolean canAddSummonType(UUID playerId, int skillId) {
        // Initialize set if needed
        Set<Integer> active = activeSummonSkills.computeIfAbsent(playerId, k -> new HashSet<>());

        // If player already has this summon type, then it's always allowed
        if (active.contains(skillId)) {
            return true;
        }

        // Otherwise, check if adding would exceed the limit
        if (active.size() >= 2) {
            return false;
        }

        return true;
    }

    /**
     * Tracks a new active summon skill for a player
     */
    private void trackSummonSkill(UUID playerId, int skillId) {
        Set<Integer> active = activeSummonSkills.computeIfAbsent(playerId, k -> new HashSet<>());
        active.add(skillId);
    }

    /**
     * Removes tracking for a summon skill
     */
    private void untrackSummonSkill(UUID playerId, int skillId) {
        if (activeSummonSkills.containsKey(playerId)) {
            Set<Integer> active = activeSummonSkills.get(playerId);
            active.remove(skillId);
        }
    }

    /**
     * Check if on cooldown
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownDuration) {
        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }

        long lastUse = cooldownMap.get(playerId);
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastUse;
        long timeLeft = cooldownDuration - timeElapsed;
        boolean onCooldown = timeElapsed < cooldownDuration;


        return onCooldown;
    }

    /**
     * Check if a player has an active summon of a specific type
     */
    private boolean hasActiveSummon(UUID playerId, Map<UUID, UUID> summonMap) {
        if (!summonMap.containsKey(playerId)) {
            return false;
        }

        UUID summonId = summonMap.get(playerId);
        Entity entity = Bukkit.getEntity(summonId);

        // If entity is null, it's no longer active (changed from assuming it is)
        if (entity == null) {
            // Changed logic: if we can't find the entity, it's NOT active
            if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Entity check - Entity with ID " + 
                        summonId + " not found. Considering it inactive.");
            }
            return false;
        }

        // Additional validation - make sure it's still alive and valid
        boolean valid = entity.isValid() && !entity.isDead();

        if (debuggingFlag == 1 && !valid) {
            plugin.getLogger().info("[BEASTMASTER DEBUG] Entity found but not valid for " + 
                    playerId + ". Dead: " + entity.isDead() + ", Valid: " + entity.isValid());
        }

        return valid;
    }

    /**
     * Check if an entity is one of the player's summons
     */
    private boolean isSummonedEntity(UUID playerId, Entity entity) {
        if (entity == null) return false;

        UUID entityId = entity.getUniqueId();

        // Check if it's a wolf summon
        if (isWolfSummon(playerId, entity)) return true;

        // Check if it's one of the additional wolves
        if (additionalWolves.containsKey(playerId)) {
            List<UUID> wolves = additionalWolves.get(playerId);
            if (wolves.contains(entityId)) return true;
        }

        // Check if it's a boar summon
        if (isBoarSummon(playerId, entity)) return true;

        // Check if it's a bear summon
        if (isBearSummon(playerId, entity)) return true;

        return false;
    }

    /**
     * Check if an entity is the player's wolf summon (primary or additional)
     */
    private boolean isWolfSummon(UUID playerId, Entity entity) {
        if (entity == null) return false;

        // Check if it's the primary wolf
        if (playerWolf.containsKey(playerId) && 
            playerWolf.get(playerId).equals(entity.getUniqueId())) {
            return true;
        }

        // Check if it's one of the additional wolves
        if (additionalWolves.containsKey(playerId)) {
            List<UUID> wolves = additionalWolves.get(playerId);
            if (wolves.contains(entity.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an entity is the player's boar summon
     */
    private boolean isBoarSummon(UUID playerId, Entity entity) {
        if (entity == null) return false;
        return playerBoar.containsKey(playerId) &&
                playerBoar.get(playerId).equals(entity.getUniqueId());
    }

    /**
     * Check if an entity is the player's bear summon
     */
    private boolean isBearSummon(UUID playerId, Entity entity) {
        if (entity == null) return false;
        return playerBear.containsKey(playerId) &&
                playerBear.get(playerId).equals(entity.getUniqueId());
    }

    /**
     * Removes a specific summon type
     */
    public void removeSummonType(Player player, int skillId) {
        UUID playerId = player.getUniqueId();

        if (skillId == WOLF_SUMMON_ID && playerWolf.containsKey(playerId)) {
            // Remove primary wolf
            UUID wolfId = playerWolf.get(playerId);
            Entity wolf = Bukkit.getEntity(wolfId);
            if (wolf != null) {
                wolf.remove();
            }
            playerWolf.remove(playerId);
            
            // IMPORTANT FIX: Also remove additional wolves from Wolf Pack skill (ID 25)
            if (additionalWolves.containsKey(playerId)) {
                List<UUID> wolves = additionalWolves.get(playerId);
                for (UUID additionalWolfId : new ArrayList<>(wolves)) {
                    Entity additionalWolf = Bukkit.getEntity(additionalWolfId);
                    if (additionalWolf != null) {
                        additionalWolf.remove();
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Removed additional wolf for " + player.getName() + " during class reset");
                        }
                    }
                }
                // Clear the additional wolves list
                additionalWolves.remove(playerId);
            }
            
            untrackSummonSkill(playerId, WOLF_SUMMON_ID);

        }
        else if (skillId == BOAR_SUMMON_ID && playerBoar.containsKey(playerId)) {
            UUID boarId = playerBoar.get(playerId);
            Entity boar = Bukkit.getEntity(boarId);
            if (boar != null) {
                boar.remove();
            }
            playerBoar.remove(playerId);
            untrackSummonSkill(playerId, BOAR_SUMMON_ID);

        }
        else if (skillId == BEAR_SUMMON_ID && playerBear.containsKey(playerId)) {
            UUID bearId = playerBear.get(playerId);
            Entity bear = Bukkit.getEntity(bearId);
            if (bear != null) {
                bear.remove();
            }
            playerBear.remove(playerId);
            untrackSummonSkill(playerId, BEAR_SUMMON_ID);

        }
    }

    /**
     * Removes all summons for a player
     */
    public void removeAllSummons(Player player) {
        UUID playerId = player.getUniqueId();

        removeSummonType(player, WOLF_SUMMON_ID);
        removeSummonType(player, BOAR_SUMMON_ID);
        removeSummonType(player, BEAR_SUMMON_ID);

        // Clear tracking completely
        activeSummonSkills.remove(playerId);
    }

    /**
     * Check if an entity is a summon and get its owner
     * @param entity The entity to check
     * @return The player who owns the summon, or null if the entity is not a summon
     */
    public Player getSummonOwner(Entity entity) {
        if (entity == null) return null;

        UUID entityId = entity.getUniqueId();

        // Check all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // Check if this entity is a summon of this player
            if (isSummonedEntity(playerId, entity)) {
                return player;
            }
        }

        return null;
    }

    /**
     * Checks if the player has summon skills and summons the creatures if not present
     * @param player The player to check
     */
    public void checkAndSummonCreatures(Player player) {
        UUID playerId = player.getUniqueId();

        // Add debug logging to track all calls to this method
        if (debuggingFlag == 1) {
            plugin.getLogger().info("[BEASTMASTER DEBUG] checkAndSummonCreatures called for " + 
                player.getName() + " - Wolf active: " + hasActiveSummon(playerId, playerWolf) +
                ", Boar active: " + hasActiveSummon(playerId, playerBoar) + 
                ", Bear active: " + hasActiveSummon(playerId, playerBear));
        }

        // Check for wolf summon skill
        if (isPurchased(playerId, WOLF_SUMMON_ID) && !hasActiveSummon(playerId, playerWolf) && 
            !isOnCooldown(playerId, wolfRespawnCooldowns, WOLF_SUMMON_COOLDOWN)) {

            // Verify again to avoid duplication
            if (!hasActiveSummonInWorld(player.getWorld(), playerId, "Wolf")) {
                summonWolves(player);
            } else if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Found active wolf in world for " + 
                    player.getName() + ", skipping summon");
            }
        }

        // Check for boar summon skill
        if (isPurchased(playerId, BOAR_SUMMON_ID) && !hasActiveSummon(playerId, playerBoar) && 
            !isOnCooldown(playerId, boarRespawnCooldowns, BOAR_SUMMON_COOLDOWN)) {

            // Verify again to avoid duplication
            if (!hasActiveSummonInWorld(player.getWorld(), playerId, "Boar")) {
                summonBoars(player);
            } else if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Found active boar in world for " + 
                    player.getName() + ", skipping summon");
            }
        }

        // Check for bear summon skill
        if (isPurchased(playerId, BEAR_SUMMON_ID) && !hasActiveSummon(playerId, playerBear) && 
            !isOnCooldown(playerId, bearRespawnCooldowns, BEAR_SUMMON_COOLDOWN)) {

            // Verify again to avoid duplication
            if (!hasActiveSummonInWorld(player.getWorld(), playerId, "Bear")) {
                summonBears(player);
            } else if (debuggingFlag == 1) {
                plugin.getLogger().info("[BEASTMASTER DEBUG] Found active bear in world for " + 
                    player.getName() + ", skipping summon");
            }
        }
    }

    /**
     * Add this new method to perform an additional check in the current world
     */
    private boolean hasActiveSummonInWorld(org.bukkit.World world, UUID playerId, String type) {
        // Search for entities that might be summons but aren't tracked properly
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Wolf) {
                Wolf wolf = (Wolf) entity;

                // Check if it's tamed and owned by this player
                if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                    Player owner = (Player) wolf.getOwner();
                    if (owner.getUniqueId().equals(playerId)) {
                        // Check if it has a custom name that matches our summon pattern
                        String name = wolf.getCustomName();
                        if (name != null && name.contains(owner.getName()) && name.contains(type)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Refreshes existing summons with updated stats based on purchased skills
     * @param player The player whose summons to refresh
     */
    public void refreshSummons(Player player) {
        UUID playerId = player.getUniqueId();

        // Refresh wolf if active
        if (hasActiveSummon(playerId, playerWolf)) {
            UUID wolfId = playerWolf.get(playerId);
            Entity entity = Bukkit.getEntity(wolfId);
            if (entity != null && entity instanceof Wolf) {
                Wolf wolf = (Wolf) entity;
                setWolfStats(player, wolf);
                updateSummonNameTag(wolf, player, "Wolf");
            }

            // Refresh additional wolves if Wolf Pack is active
            if (additionalWolves.containsKey(playerId)) {
                for (UUID additionalWolfId : additionalWolves.get(playerId)) {
                    Entity additionalEntity = Bukkit.getEntity(additionalWolfId);
                    if (additionalEntity != null && additionalEntity instanceof Wolf) {
                        Wolf additionalWolf = (Wolf) additionalEntity;
                        setWolfStats(player, additionalWolf);
                        updateSummonNameTag(additionalWolf, player, "Wolf");
                    }
                }
            }

            // If Wolf Pack was just purchased and we only have one wolf, summon another
            if (isPurchased(playerId, 100025) && 
                (!additionalWolves.containsKey(playerId) || 
                (additionalWolves.containsKey(playerId) && additionalWolves.get(playerId).isEmpty()))) {
                // Summon an additional wolf
                Wolf newWolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
                newWolf.setTamed(true);
                newWolf.setOwner(player);
                setWolfStats(player, newWolf);
                updateSummonNameTag(newWolf, player, "Wolf");

                // Store the additional wolf
                List<UUID> wolves = additionalWolves.computeIfAbsent(playerId, k -> new ArrayList<>());
                wolves.add(newWolf.getUniqueId());

                // Notify player
                player.sendMessage(ChatColor.GREEN + "Your Wolf Pack skill summoned an additional wolf!");
            }
        }

        // Refresh boar if active
        if (hasActiveSummon(playerId, playerBoar)) {
            UUID boarId = playerBoar.get(playerId);
            Entity entity = Bukkit.getEntity(boarId);
            if (entity != null && entity instanceof Wolf) {
                Wolf boar = (Wolf) entity;
                setBoarStats(player, boar);
                updateSummonNameTag(boar, player, "Boar");
            }
        }

        // Refresh bear if active
        if (hasActiveSummon(playerId, playerBear)) {
            UUID bearId = playerBear.get(playerId);
            Entity entity = Bukkit.getEntity(bearId);
            if (entity != null && entity instanceof Wolf) {
                Wolf bear = (Wolf) entity;
                setBearStats(player, bear);
                updateSummonNameTag(bear, player, "Bear");
            }
        }
    }

    /**
     * Clean up all summons and tasks for a player
     */
    public void clearPlayerData(UUID playerId) {
        // Remove all summons
        if (playerWolf.containsKey(playerId)) {
            Entity entity = Bukkit.getEntity(playerWolf.get(playerId));
            if (entity != null) entity.remove();
            playerWolf.remove(playerId);
        }

        // Remove additional wolves (from Wolf Pack skill)
        if (additionalWolves.containsKey(playerId)) {
            List<UUID> wolves = additionalWolves.get(playerId);
            for (UUID wolfId : wolves) {
                Entity entity = Bukkit.getEntity(wolfId);
                if (entity != null) {
                    entity.remove();
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Cleared additional wolf for player ID: " + playerId);
                    }
                }
            }
            additionalWolves.remove(playerId);
        }

        if (playerBoar.containsKey(playerId)) {
            Entity entity = Bukkit.getEntity(playerBoar.get(playerId));
            if (entity != null) entity.remove();
            playerBoar.remove(playerId);
        }

        if (playerBear.containsKey(playerId)) {
            Entity entity = Bukkit.getEntity(playerBear.get(playerId));
            if (entity != null) entity.remove();
            playerBear.remove(playerId);
        }

        // Clear tracking for summon skills
        activeSummonSkills.remove(playerId);

        // Clear cooldowns
        wolfRespawnCooldowns.remove(playerId);
        boarRespawnCooldowns.remove(playerId);
        bearRespawnCooldowns.remove(playerId);

        // Clear active effects
        bearGuardianActive.remove(playerId);
        boarFrenzyExpiration.remove(playerId);

        // Clear summon stats caches for all entities belonging to this player
        // This ensures no orphaned references remain
        Set<UUID> toRemove = new HashSet<>();
        for (UUID entityId : summonDamageMultiplier.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity == null || isSummonedEntity(playerId, entity)) {
                toRemove.add(entityId);
            }
        }
        for (UUID entityId : toRemove) {
            summonDamageMultiplier.remove(entityId);
            summonHealthMultiplier.remove(entityId);
            summonDefenseMultiplier.remove(entityId);
            summonAttackSpeedMultiplier.remove(entityId);
            summonMovementSpeedMultiplier.remove(entityId);
            summonCritChance.remove(entityId);
        }

        // Cancel notification tasks
        BukkitTask task = cooldownNotificationTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all Beastmaster data for player ID: " + playerId + 
                    " including " + toRemove.size() + " cached summon stats");
        }
    }

    /**
     * Updates the name tag of a summon to show player name, type, and HP
     * @param entity The summon entity
     * @param player The player who owns the summon
     * @param type The type of summon (Wolf, Boar, Bear)
     */
    private void updateSummonNameTag(LivingEntity entity, Player player, String type) {
        double health = Math.round(entity.getHealth());
        double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        // Set custom name with player name, type, and HP (numeric format)
        entity.setCustomName(ChatColor.GREEN + player.getName() + "'s " + type + 
                             ChatColor.RED + " ❤ " + (int)health + "/" + (int)maxHealth);
        entity.setCustomNameVisible(true);

    }

    /**
     * Updates the name tags of all summons for a player
     * @param player The player whose summons to update
     */
    private void updateAllSummonNameTags(Player player) {
        UUID playerId = player.getUniqueId();

        // Get all summons
        Set<UUID> allSummons = new HashSet<>();
        if (playerWolf.containsKey(playerId)) allSummons.add(playerWolf.get(playerId));
        if (playerBoar.containsKey(playerId)) allSummons.add(playerBoar.get(playerId));
        if (playerBear.containsKey(playerId)) allSummons.add(playerBear.get(playerId));

        // Update each summon's name tag
        for (UUID summonId : allSummons) {
            Entity entity = Bukkit.getEntity(summonId);
            if (entity != null && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Determine the type of summon
                String type = "Summon";
                if (isWolfSummon(playerId, entity)) {
                    type = "Wolf";
                } else if (isBoarSummon(playerId, entity)) {
                    type = "Boar";
                } else if (isBearSummon(playerId, entity)) {
                    type = "Bear";
                }

                // Update the name tag
                updateSummonNameTag(livingEntity, player, type);
            }
        }

    }
    /**
     * Helper method for debug info
     */
    private String getEntityName(Entity entity) {
        if (entity instanceof Player) {
            return "Player:" + ((Player) entity).getName();
        } else {
            String name = entity.getCustomName();
            if (name != null) {
                return entity.getType() + ":" + ChatColor.stripColor(name);
            } else {
                return entity.getType().toString();
            }
        }
    }

    /**
     * Global event handler to catch all cases of players attacking their own summons
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttackSummon(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Entity target = event.getEntity();

        // Check all online players to see if this entity belongs to anyone
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isSummonedEntity(player.getUniqueId(), target) && 
                attacker.getUniqueId().equals(player.getUniqueId())) {

                // Cancel the event to prevent damage
                event.setCancelled(true);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Prevented " + attacker.getName() + 
                        " from damaging their own summon via global handler");
                    attacker.sendMessage(ChatColor.DARK_GRAY + 
                        "[DEBUG] You cannot damage your own summons (global protection)");
                }
                return;
            }
        }
    }

    /**
     * Handle summon attacks with correct damage values
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSummonAttack(EntityDamageByEntityEvent event) {
        // Check if attacker is a wolf or any entity that could be a summon
        if (event.getDamager() instanceof Wolf) {
            Wolf wolf = (Wolf) event.getDamager();

            // Only process tamed wolves (summons are tamed)
            if (!wolf.isTamed()) return;

            // Get the owner if it's a player
            if (wolf.getOwner() instanceof Player) {
                Player owner = (Player) wolf.getOwner();
                UUID ownerId = owner.getUniqueId();

                // Check if this is our summon
                if (isWolfSummon(ownerId, wolf) || 
                    (additionalWolves.containsKey(ownerId) && 
                     additionalWolves.get(ownerId).contains(wolf.getUniqueId())) ||
                    isBoarSummon(ownerId, wolf) ||
                    isBearSummon(ownerId, wolf)) {

                    // Get the base damage based on summon type
                    double baseDamage = 0.0;
                    if (isWolfSummon(ownerId, wolf) || 
                        (additionalWolves.containsKey(ownerId) && 
                         additionalWolves.get(ownerId).contains(wolf.getUniqueId()))) {
                        baseDamage = 50.0;
                    } else if (isBoarSummon(ownerId, wolf)) {
                        baseDamage = 80.0;
                    } else if (isBearSummon(ownerId, wolf)) {
                        baseDamage = 20.0;
                    }

                    // Apply damage multiplier
                    double multiplier = summonDamageMultiplier.getOrDefault(wolf.getUniqueId(), 1.0);
                    double finalDamage = baseDamage * multiplier;

                    // Check for critical hit
                    double critChance = summonCritChance.getOrDefault(wolf.getUniqueId(), 0.0);
                    boolean isCrit = false;
                    if (critChance > 0 && random.nextDouble() < critChance) {
                        finalDamage *= 2.0;
                        isCrit = true;

                        // Show notification
                        ActionBarUtils.sendActionBar(owner, 
                            ChatColor.RED + "Summon Critical Hit!");
                    }

                    // Set the damage directly - this is the key fix
                    event.setDamage(finalDamage);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("[SUMMON DAMAGE] " + owner.getName() + 
                                "'s summon dealt " + finalDamage + " damage" +
                                " (base: " + baseDamage + ", multiplier: " + multiplier + 
                                (isCrit ? ", CRITICAL HIT!" : "") + ")");
                        owner.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Summon dealt " + 
                                finalDamage + " damage");
                    }

                    // Apply wolf lifesteal if applicable (Skill ID 100017)
                    if (isWolfSummon(ownerId, wolf) && isPurchased(ownerId, 100017)) {
                        double healAmount = finalDamage * 0.05; // 5% of damage dealt
                        double maxHealth = owner.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        double newHealth = Math.min(owner.getHealth() + healAmount, maxHealth);
                        owner.setHealth(newHealth);

                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Wolf Healing: " + owner.getName() + 
                                    " healed for " + healAmount + " HP");
                            owner.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Wolf Healing: +" + 
                                    String.format("%.1f", healAmount) + " HP");
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle player teleportation and remove all summons
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Skip for short-distance teleports (like small movements)
        if (event.getFrom().distance(event.getTo()) < 10) {
            return;
        }

        // Schedule task to run after teleport completes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Removing summons for " + player.getName() + " after teleport");
                player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] Removing all summons after teleport");
            }

            // Remove all summons
            removeAllSummons(player);

            // Force cooldowns to be off after a delay to allow re-summoning
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    wolfRespawnCooldowns.remove(playerId);
                    boarRespawnCooldowns.remove(playerId);
                    bearRespawnCooldowns.remove(playerId);

                    // Now it's safe to re-summon
                    checkAndSummonCreatures(player);
                }
            }, 20L); // 1 second delay

        }, 5L); // 5 ticks (0.25 second) delay to ensure teleport completes
    }
}
