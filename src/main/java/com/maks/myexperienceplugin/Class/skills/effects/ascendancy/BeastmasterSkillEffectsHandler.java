package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private static final long BOAR_SUMMON_COOLDOWN = 60000; // 60 seconds (unified cooldown)
    private static final long BEAR_SUMMON_COOLDOWN = 60000; // 60 seconds (unified cooldown)
    private static final long SUMMON_CHECK_DELAY = 60L; // 3 seconds delay for summon checks
    private static final long RATE_LIMIT_DELAY = 100L; // 0.1 second rate limiting for summon checks
    private static final long AUTO_RESUMMON_DELAY = 3000L; // 3 seconds delay for auto-resummon
    private static final int debuggingFlag = 0; // Set to 1 for debug mode

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

    // Anti-duplication tracking
    private final Map<UUID, Boolean> summonInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSummonCheck = new ConcurrentHashMap<>();

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
        
        // Note: Registration with the SkillEffectsHandler manager is handled by the plugin's main class
        // This is typically done in the plugin's initialization code, not here
        
        // Start periodic task for summon maintenance
        startMaintenanceTask();
    }
    
    /**
     * Starts a periodic task for summon maintenance (health regeneration, etc.)
     */
    private void startMaintenanceTask() {
        // Run task every 10 seconds (200 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Iterate through all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                
                // Check if player has any summons
                boolean hasSummons = playerWolf.containsKey(playerId) || 
                                    playerBoar.containsKey(playerId) || 
                                    playerBear.containsKey(playerId);
                
                if (hasSummons) {
                    // Verify summon counts and clean up any duplicates
                    verifySummonCounts(player);
                    
                    // Update summon name tags to show current health
                    updateAllSummonNameTags(player);
                    
                    // Regenerate bear health if applicable
                    if (playerBear.containsKey(playerId)) {
                        UUID bearId = playerBear.get(playerId);
                        Entity bearEntity = Bukkit.getEntity(bearId);
                        
                        if (bearEntity != null && bearEntity instanceof LivingEntity) {
                            LivingEntity bear = (LivingEntity) bearEntity;
                            double maxHealth = bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                            double currentHealth = bear.getHealth();
                            
                            // Bears regenerate 5% of max health every 10 seconds
                            if (currentHealth < maxHealth) {
                                double newHealth = Math.min(currentHealth + (maxHealth * 0.05), maxHealth);
                                bear.setHealth(newHealth);
                                
                                // Update name tag to show new health
                                updateSummonNameTag(bear, player, "Bear");
                                
                                if (debuggingFlag == 1) {
                                    plugin.getLogger().info("Regenerated bear health for " + player.getName() + 
                                        ": " + currentHealth + " -> " + newHealth);
                                }
                            }
                        }
                    }
                }
            }
        }, 200L, 200L); // Initial delay: 10s, Period: 10s
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - 100000; // Remove offset to get original skill ID
        UUID playerId = player.getUniqueId();

        switch (originalId) {
            case 1: // Wolf summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 1: Wolf summon unlocked for " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 1: Wolf summon unlocked (50 dmg/50 hp)");
                }
                // DODAJ TĘ LINIĘ:
                Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndSummonCreatures(player), 20L);
                break;
            case 2: // Boar summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 2: Boar summon unlocked for " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 2: Boar summon unlocked (80 dmg/20 hp)");
                }
                // DODAJ TĘ LINIĘ:
                Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndSummonCreatures(player), 20L);
                break;
            case 3: // Bear summon
                // This is handled when player uses the summon command
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 3: Bear summon unlocked for " + player.getName());
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 3: Bear summon unlocked (20 dmg/80 hp)");
                }
                // DODAJ TĘ LINIĘ:
                Bukkit.getScheduler().runTaskLater(plugin, () -> checkAndSummonCreatures(player), 20L);
                break;
            case 4: // Wolves gain +5% ms
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 4: Will add 5% movement speed to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 4: Wolves will gain +5% movement speed");
                }
                break;
            case 5: // Boars gain +15% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 5: Will add 15% damage to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 5: Boars will gain +15% damage");
                }
                break;
            case 6: // Bears gain +10% hp
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 6: Will add 10% health to bears when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 6: Bears will gain +10% health");
                }
                break;
            case 7: // Wolves gain +5% as
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 7: Will add 5% attack speed to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 7: Wolves will gain +5% attack speed");
                }
                break;
            case 8: // Boars gain +10% as
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 8: Will add 10% attack speed to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 8: Boars will gain +10% attack speed");
                }
                break;
            case 9: // Summons gain +5% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 9: Will add 5% damage to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 9: All summons will gain +5% damage");
                }
                break;
            case 10: // Bears gain +50% def
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 10: Will add 50% defense to bears when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 10: Bears will gain +50% defense");
                }
                break;
            case 11: // Wolves gain 10% chance to crit
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 11: Will add 10% critical chance to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 11: Wolves will gain 10% crit chance");
                }
                break;
            case 12: // Wolves gain +100hp
                // Store as flat bonus, not multiplier
                // This will be handled separately in setWolfStats
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 12: Wolf HP bonus stored for application");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 12: Wolves gain +100 HP (flat bonus)");
                }
                break;
            case 13: // Boars gain 15% chance to crit
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 13: Will add 15% critical chance to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 13: Boars gain 15% crit chance");
                }
                break;
            case 14: // Summons gain +10% dmg
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 14: Will add 10% damage to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 14: All summons gain +10% damage");
                }
                break;
            case 15: // When Bears hp<50% u and ur summons gain +10% def
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 15: Will handle bear guardian effect dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 15: Bear guardian effect enabled");
                }
                break;
            case 16: // Bears gain +200hp
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 16: Will add 200 HP to bears when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 16: Bears gain +200 HP");
                }
                break;
            case 17: // Wolves heal's u for 5% of dmg dealt
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 17: Will handle wolf healing dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 17: Wolf healing enabled");
                }
                break;
            case 18: // Summons gain +10% ms
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 18: Will add 10% movement speed to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 18: All summons gain +10% movement speed");
                }
                break;
            case 19: // Boars after killing enemy gains +7% as for 3s
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 19: Will handle boar frenzy dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 19: Boar frenzy enabled");
                }
                break;
            case 20: // Bears heal for 10% hp each 10s
                // This is handled via periodic task
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 20: Will handle bear regeneration via periodic task");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 20: Bear regeneration enabled");
                }
                break;
            case 21: // Summons gain +30% hp
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 21: Will add 30% health to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 21: All summons gain +30% health");
                }
                break;
            case 22: // Wolves gain +10% hp
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 22: Will add 10% health to wolves when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 22: Wolves gain +10% health");
                }
                break;
            case 23: // Boars gain 20% ms
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 23: Will add 20% movement speed to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 23: Boars gain +20% movement speed");
                }
                break;
            case 24: // Summons gain +25% def
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 24: Will add 25% defense to all summons");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 24: All summons gain +25% defense");
                }
                break;
            case 25: // U summon 1 more wolf
                // Handled by the summon wolf method
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 25: Will summon an additional wolf");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 25: Additional wolf enabled");
                }
                break;
            case 26: // Boars gain +15% dmg and +15% as
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 26: Will add 15% damage and 15% attack speed to boars when summoned");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 26: Boars gain +15% damage and +15% attack speed");
                }
                break;
            case 27: // Heals ur summons for 5% of yours dmg dealt
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("BEASTMASTER SKILL 27: Will handle summon healing dynamically");
                    player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] BEASTMASTER SKILL 27: Summon healing enabled");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Unknown Beastmaster skill ID: " + skillId);
                }
                break;
        }
        
        // Refresh existing summons if any
        refreshSummons(player);
    }

    /**
     * Finds and removes duplicate summons that aren't being tracked properly
     * @param player The player to check for duplicate summons
     */
    public void cleanupDuplicateSummons(Player player) {
        UUID playerId = player.getUniqueId();
        World world = player.getWorld();
        
        // Get all wolves in the player's world
        List<Entity> wolves = world.getEntities().stream()
            .filter(e -> e instanceof Wolf)
            .filter(e -> ((Wolf) e).isTamed())
            .filter(e -> ((Wolf) e).getOwner() != null && ((Wolf) e).getOwner().equals(player))
            .collect(Collectors.toList());
        
        // Get all pigs in the player's world (potential boar summons)
        List<Entity> pigs = world.getEntities().stream()
            .filter(e -> e instanceof Pig)
            .filter(e -> e.getCustomName() != null && e.getCustomName().contains(player.getName()))
            .collect(Collectors.toList());
        
        // Get all polar bears in the player's world (potential bear summons)
        List<Entity> polarBears = world.getEntities().stream()
            .filter(e -> e instanceof PolarBear)
            .filter(e -> e.getCustomName() != null && e.getCustomName().contains(player.getName()))
            .collect(Collectors.toList());
        
        // Combine all potential summons
        List<Entity> allPotentialSummons = new ArrayList<>();
        allPotentialSummons.addAll(wolves);
        allPotentialSummons.addAll(pigs);
        allPotentialSummons.addAll(polarBears);
        
        // Get tracked summons
        Set<UUID> trackedSummons = new HashSet<>();
        if (playerWolf.containsKey(playerId)) trackedSummons.add(playerWolf.get(playerId));
        if (playerBoar.containsKey(playerId)) trackedSummons.add(playerBoar.get(playerId));
        if (playerBear.containsKey(playerId)) trackedSummons.add(playerBear.get(playerId));
        
        // Add additional wolves
        if (additionalWolves.containsKey(playerId)) {
            trackedSummons.addAll(additionalWolves.get(playerId));
        }
        
        // Remove untracked summons
        int removedCount = 0;
        for (Entity entity : allPotentialSummons) {
            if (!trackedSummons.contains(entity.getUniqueId())) {
                entity.remove();
                removedCount++;
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Removed untracked summon for " + player.getName() + 
                        ": " + entity.getType() + " at " + entity.getLocation());
                }
            }
        }
        
        if (removedCount > 0 && debuggingFlag == 1) {
            plugin.getLogger().info("Cleaned up " + removedCount + " untracked summons for " + player.getName());
            player.sendMessage(ChatColor.YELLOW + "Cleaned up " + removedCount + " duplicate summons.");
        }
    }

    /**
     * Handle player logout and clean up all summons
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Player " + player.getName() + " quit, cleaning up summons");
        }
        
        // Clean up all summons and data
        clearPlayerData(playerId);
    }

    /**
     * Summons a wolf for the player
     * @param player The player
     */
    public void summonWolves(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if summon is already in progress to prevent duplicates
        if (summonInProgress.getOrDefault(playerId, false)) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Summon already in progress for " + player.getName());
            }
            return;
        }

        // Rate limiting check
        long currentTime = System.currentTimeMillis();
        if (lastSummonCheck.containsKey(playerId)) {
            long lastCheck = lastSummonCheck.get(playerId);
            if (currentTime - lastCheck < RATE_LIMIT_DELAY) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Rate limiting summon check for " + player.getName());
                }
                return;
            }
        }
        lastSummonCheck.put(playerId, currentTime);

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

        // Set summon in progress flag
        summonInProgress.put(playerId, true);

        // Clean up any duplicate summons first
        cleanupDuplicateSummons(player);

        // Determine how many wolves to summon based on existing wolves
        int currentWolfCount = 0;
        if (playerWolf.containsKey(playerId) && hasActiveSummon(playerId, playerWolf)) {
            currentWolfCount++;
        }
        if (additionalWolves.containsKey(playerId)) {
            for (UUID wolfId : additionalWolves.get(playerId)) {
                Entity wolf = Bukkit.getEntity(wolfId);
                if (wolf != null && !wolf.isDead()) {
                    currentWolfCount++;
                }
            }
        }

        int maxWolves = isPurchased(playerId, 100025) ? 2 : 1;
        int wolfCount = maxWolves - currentWolfCount;

        if (wolfCount <= 0) {
            player.sendMessage(ChatColor.YELLOW + "You already have all your wolves summoned!");
            return;
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
        } finally {
            // Clear summon in progress flag
            summonInProgress.put(playerId, false);
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

        // Check if summon is already in progress to prevent duplicates
        if (summonInProgress.getOrDefault(playerId, false)) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Summon already in progress for " + player.getName());
            }
            return;
        }

        // Rate limiting check
        long currentTime = System.currentTimeMillis();
        if (lastSummonCheck.containsKey(playerId)) {
            long lastCheck = lastSummonCheck.get(playerId);
            if (currentTime - lastCheck < RATE_LIMIT_DELAY) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Rate limiting summon check for " + player.getName());
                }
                return;
            }
        }
        lastSummonCheck.put(playerId, currentTime);

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

        // Set summon in progress flag
        summonInProgress.put(playerId, true);

        // Clean up any duplicate summons first
        cleanupDuplicateSummons(player);

        try {
            Entity boarEntity;
            
            // Try to use LibsDisguises if available
            if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
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
                
                boarEntity = boar;
                
                // Use reflection to avoid direct dependency
                try {
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
                        
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Successfully applied pig disguise to boar for " + player.getName());
                    }
                } catch (Exception e) {
                    // Error with LibsDisguises
                    if (debuggingFlag == 1) {
                        plugin.getLogger().warning("Error applying pig disguise: " + e.getMessage());
                    }
                }
            } else {
                // LibsDisguises not available, use native Pig
                Pig pig = player.getWorld().spawn(player.getLocation(), Pig.class);
                
                // Set custom name with player name and HP
                updateSummonNameTag(pig, player, "Boar");
                
                // Store boar in map
                playerBoar.put(playerId, pig.getUniqueId());
                
                boarEntity = pig;
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("LibsDisguises not available, using native Pig for " + player.getName());
                }
            }

            // Show notification for important ability
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Summoned a boar");

            // Track this summon type
            trackSummonSkill(playerId, BOAR_SUMMON_ID);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error spawning boar: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error summoning boar: " + e.getMessage());
        } finally {
            // Clear summon in progress flag
            summonInProgress.put(playerId, false);
        }
    }

    /**
     * Summons a bear for the player
     * @param player The player
     */
    public void summonBears(Player player) {
        UUID playerId = player.getUniqueId();

        // Check if summon is already in progress to prevent duplicates
        if (summonInProgress.getOrDefault(playerId, false)) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Summon already in progress for " + player.getName());
            }
            return;
        }

        // Rate limiting check
        long currentTime = System.currentTimeMillis();
        if (lastSummonCheck.containsKey(playerId)) {
            long lastCheck = lastSummonCheck.get(playerId);
            if (currentTime - lastCheck < RATE_LIMIT_DELAY) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Rate limiting summon check for " + player.getName());
                }
                return;
            }
        }
        lastSummonCheck.put(playerId, currentTime);

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

        // Set summon in progress flag
        summonInProgress.put(playerId, true);

        // Clean up any duplicate summons first
        cleanupDuplicateSummons(player);

        try {
            Entity bearEntity;
            
            // Try to use LibsDisguises if available
            if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
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
                
                bearEntity = bear;
                
                // Use reflection to avoid direct dependency
                try {
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
                        
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Successfully applied polar bear disguise to bear for " + player.getName());
                    }
                } catch (Exception e) {
                    // Error with LibsDisguises
                    if (debuggingFlag == 1) {
                        plugin.getLogger().warning("Error applying polar bear disguise: " + e.getMessage());
                    }
                }
            } else {
                // LibsDisguises not available, use native PolarBear
                PolarBear polarBear = player.getWorld().spawn(player.getLocation(), PolarBear.class);
                
                // Set custom name with player name and HP
                updateSummonNameTag(polarBear, player, "Bear");
                
                // Store bear in map
                playerBear.put(playerId, polarBear.getUniqueId());
                
                bearEntity = polarBear;
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("LibsDisguises not available, using native PolarBear for " + player.getName());
                }
            }

            // Show notification for important ability
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Summoned a bear");

            // Track this summon type
            trackSummonSkill(playerId, BEAR_SUMMON_ID);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error spawning bear: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error summoning bear: " + e.getMessage());
        } finally {
            // Clear summon in progress flag
            summonInProgress.put(playerId, false);
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
     * Verifies and fixes summon counts for a player
     * @param player The player to check
     */
    public void verifySummonCounts(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Count active summon types
        int summonTypeCount = 0;
        if (playerWolf.containsKey(playerId) && hasActiveSummon(playerId, playerWolf)) summonTypeCount++;
        if (playerBoar.containsKey(playerId) && hasActiveSummon(playerId, playerBoar)) summonTypeCount++;
        if (playerBear.containsKey(playerId) && hasActiveSummon(playerId, playerBear)) summonTypeCount++;
        
        // Check if player has more than 2 types of summons
        if (summonTypeCount > 2) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Player " + player.getName() + " has " + summonTypeCount + 
                    " summon types, which exceeds the limit of 2. Removing excess summons.");
            }
            
            // Remove excess summons (prioritize keeping wolf and boar)
            if (playerBear.containsKey(playerId) && summonTypeCount > 2) {
                removeSummonType(player, BEAR_SUMMON_ID);
                summonTypeCount--;
            }
            if (playerBoar.containsKey(playerId) && summonTypeCount > 2) {
                removeSummonType(player, BOAR_SUMMON_ID);
                summonTypeCount--;
            }
            if (playerWolf.containsKey(playerId) && summonTypeCount > 2) {
                removeSummonType(player, WOLF_SUMMON_ID);
            }
            
            player.sendMessage(ChatColor.RED + "Some of your summons have been removed because you exceeded the limit.");
        }
        
        // Update all summon name tags to ensure they're correct
        updateAllSummonNameTags(player);
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

                // Update name tag
                updateSummonNameTag(livingEntity, player, type);
            }
        }

        // Also update additional wolves
        if (additionalWolves.containsKey(playerId)) {
            for (UUID wolfId : additionalWolves.get(playerId)) {
                Entity entity = Bukkit.getEntity(wolfId);
                if (entity != null && entity instanceof LivingEntity) {
                    updateSummonNameTag((LivingEntity) entity, player, "Wolf");
                }
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
     * Removes all summons for a player
     * Called when player logs out or changes worlds
     * @param player The player whose summons to remove
     */
    public void removeAllSummons(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        clearPlayerData(playerId);
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Removed all summons for " + player.getName());
        }
    }
    
    /**
     * Checks and summons creatures for a player based on their purchased skills
     * Called when player logs in or periodically
     * @param player The player to check and summon creatures for
     */
    public void checkAndSummonCreatures(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("checkAndSummonCreatures: Player null/offline/dead for " + 
                    (player != null ? player.getName() : "null"));
            }
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("checkAndSummonCreatures() called for " + player.getName());
        }
        
        // Add cooldown check to prevent spam
        long currentTime = System.currentTimeMillis();
        if (lastSummonCheck.containsKey(playerId)) {
            long lastCheck = lastSummonCheck.get(playerId);
            if (currentTime - lastCheck < 2000) { // ZMIENIONE z 5000 na 2000
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Rate limiting summon check for " + player.getName());
                }
                return;
            }
        }
        lastSummonCheck.put(playerId, currentTime);
        
        // Clean up any duplicate summons first
        cleanupDuplicateSummons(player);
        
        // Check which summons the player should have based on purchased skills
        boolean shouldHaveWolf = isPurchased(playerId, WOLF_SUMMON_ID) && 
                                !hasActiveSummon(playerId, playerWolf) && 
                                !isOnCooldown(playerId, wolfRespawnCooldowns, WOLF_SUMMON_COOLDOWN);
        
        boolean shouldHaveBoar = isPurchased(playerId, BOAR_SUMMON_ID) && 
                                !hasActiveSummon(playerId, playerBoar) && 
                                !isOnCooldown(playerId, boarRespawnCooldowns, BOAR_SUMMON_COOLDOWN);
        
        boolean shouldHaveBear = isPurchased(playerId, BEAR_SUMMON_ID) && 
                                !hasActiveSummon(playerId, playerBear) && 
                                !isOnCooldown(playerId, bearRespawnCooldowns, BEAR_SUMMON_COOLDOWN);
        
        // Check summon type limit (max 2 types)
        int currentTypes = 0;
        if (hasActiveSummon(playerId, playerWolf)) currentTypes++;
        if (hasActiveSummon(playerId, playerBoar)) currentTypes++;
        if (hasActiveSummon(playerId, playerBear)) currentTypes++;
        
        // Summon missing creatures (respecting the 2-type limit)
        if (shouldHaveWolf && currentTypes < 2) {
            summonWolves(player);
            currentTypes++;
        }
        
        if (shouldHaveBoar && currentTypes < 2) {
            summonBoars(player);
            currentTypes++;
        }
        
        if (shouldHaveBear && currentTypes < 2) {
            summonBears(player);
        }
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Checked and summoned creatures for " + player.getName());
        }
    }
    
    /**
     * Checks bear health and applies guardian buff if applicable
     * @param player The player to check
     */
    private void checkBearGuardian(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!isPurchased(playerId, 100015) || !playerBear.containsKey(playerId)) {
            bearGuardianActive.put(playerId, false);
            return;
        }
        
        Entity bearEntity = Bukkit.getEntity(playerBear.get(playerId));
        if (bearEntity != null && bearEntity instanceof LivingEntity) {
            LivingEntity bear = (LivingEntity) bearEntity;
            double healthPercent = (bear.getHealth() / bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) * 100;
            
            boolean wasActive = bearGuardianActive.getOrDefault(playerId, false);
            boolean isActive = healthPercent < 50;
            
            bearGuardianActive.put(playerId, isActive);
            
            // Notify player when guardian activates/deactivates
            if (isActive && !wasActive) {
                player.sendMessage(ChatColor.GOLD + "Bear Guardian activated! +10% defense for you and summons!");
            } else if (!isActive && wasActive) {
                player.sendMessage(ChatColor.YELLOW + "Bear Guardian deactivated.");
            }
        }
    }

    /**
     * Applies periodic effects for all Beastmaster players
     * Called by the periodic task in AscendancySkillEffectIntegrator
     */
    public void applyPeriodicEffects() {
        // Iterate through all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Check if player has any summons
            boolean hasSummons = playerWolf.containsKey(playerId) || 
                                playerBoar.containsKey(playerId) || 
                                playerBear.containsKey(playerId);
            
            if (hasSummons) {
                // Verify summon counts and clean up any duplicates
                verifySummonCounts(player);
                
                // Update summon name tags to show current health
                updateAllSummonNameTags(player);
                
                // Regenerate bear health if applicable
                if (playerBear.containsKey(playerId)) {
                    UUID bearId = playerBear.get(playerId);
                    Entity bearEntity = Bukkit.getEntity(bearId);
                    
                    if (bearEntity != null && bearEntity instanceof LivingEntity) {
                        LivingEntity bear = (LivingEntity) bearEntity;
                        double maxHealth = bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                        double currentHealth = bear.getHealth();
                        
                        // Bears regenerate 5% of max health every 10 seconds
                        if (currentHealth < maxHealth) {
                            double newHealth = Math.min(currentHealth + (maxHealth * 0.05), maxHealth);
                            bear.setHealth(newHealth);
                            
                            // Update name tag to show new health
                            updateSummonNameTag(bear, player, "Bear");
                            
                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("Regenerated bear health for " + player.getName() + 
                                    ": " + currentHealth + " -> " + newHealth);
                            }
                        }
                        
                        // Check Bear Guardian (Skill 15)
                        checkBearGuardian(player);
                    }
                }
            }
        }
    }
    
    /**
     * Gets the owner of a summon entity
     * @param entity The entity to check
     * @return The player who owns the summon, or null if the entity is not a summon
     */
    public Player getSummonOwner(Entity entity) {
        if (entity == null) return null;
        
        // Check all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            
            // Check if the entity is a wolf summon
            if (isWolfSummon(playerId, entity)) {
                return player;
            }
            
            // Check if the entity is a boar summon
            if (isBoarSummon(playerId, entity)) {
                return player;
            }
            
            // Check if the entity is a bear summon
            if (isBearSummon(playerId, entity)) {
                return player;
            }
        }
        
        // If the entity is a wolf, check if it's tamed and has an owner
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf) entity;
            if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                return (Player) wolf.getOwner();
            }
        }
        
        return null;
    }
    
    /**
     * Refreshes all summons for a player
     * Called when a player purchases a new skill that might affect summon stats
     * @param player The player whose summons to refresh
     */
    public void refreshSummons(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Update wolf stats
        if (playerWolf.containsKey(playerId)) {
            UUID wolfId = playerWolf.get(playerId);
            Entity wolfEntity = Bukkit.getEntity(wolfId);
            
            if (wolfEntity != null && wolfEntity instanceof Wolf) {
                Wolf wolf = (Wolf) wolfEntity;
                setWolfStats(player, wolf);
                updateSummonNameTag(wolf, player, "Wolf");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Refreshed wolf stats for " + player.getName());
                }
            }
        }
        
        // Update additional wolves
        if (additionalWolves.containsKey(playerId)) {
            for (UUID wolfId : additionalWolves.get(playerId)) {
                Entity wolfEntity = Bukkit.getEntity(wolfId);
                
                if (wolfEntity != null && wolfEntity instanceof Wolf) {
                    Wolf wolf = (Wolf) wolfEntity;
                    setWolfStats(player, wolf);
                    updateSummonNameTag(wolf, player, "Wolf");
                }
            }
            
            if (debuggingFlag == 1 && !additionalWolves.get(playerId).isEmpty()) {
                plugin.getLogger().info("Refreshed additional wolves for " + player.getName());
            }
        }
        
        // Update boar stats
        if (playerBoar.containsKey(playerId)) {
            UUID boarId = playerBoar.get(playerId);
            Entity boarEntity = Bukkit.getEntity(boarId);
            
            if (boarEntity != null && boarEntity instanceof LivingEntity) {
                LivingEntity boar = (LivingEntity) boarEntity;
                setBoarStats(player, boar);
                updateSummonNameTag(boar, player, "Boar");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Refreshed boar stats for " + player.getName());
                }
            }
        }
        
        // Update bear stats
        if (playerBear.containsKey(playerId)) {
            UUID bearId = playerBear.get(playerId);
            Entity bearEntity = Bukkit.getEntity(bearId);
            
            if (bearEntity != null && bearEntity instanceof LivingEntity) {
                LivingEntity bear = (LivingEntity) bearEntity;
                setBearStats(player, bear);
                updateSummonNameTag(bear, player, "Bear");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Refreshed bear stats for " + player.getName());
                }
            }
        }
    }
    
    /**
     * Clears all summon data for a player
     * @param playerId The player's UUID
     */
    public void clearPlayerData(UUID playerId) {
        // Remove all summons
        if (playerWolf.containsKey(playerId)) {
            UUID wolfId = playerWolf.get(playerId);
            Entity wolf = Bukkit.getEntity(wolfId);
            if (wolf != null) wolf.remove();
            playerWolf.remove(playerId);
        }
        
        if (playerBoar.containsKey(playerId)) {
            UUID boarId = playerBoar.get(playerId);
            Entity boar = Bukkit.getEntity(boarId);
            if (boar != null) boar.remove();
            playerBoar.remove(playerId);
        }
        
        if (playerBear.containsKey(playerId)) {
            UUID bearId = playerBear.get(playerId);
            Entity bear = Bukkit.getEntity(bearId);
            if (bear != null) bear.remove();
            playerBear.remove(playerId);
        }
        
        // Remove additional wolves
        if (additionalWolves.containsKey(playerId)) {
            for (UUID wolfId : additionalWolves.get(playerId)) {
                Entity wolf = Bukkit.getEntity(wolfId);
                if (wolf != null) wolf.remove();
            }
            additionalWolves.remove(playerId);
        }
        
        // Clear all tracking data
        activeSummonSkills.remove(playerId);
        summonDamageMultiplier.remove(playerId);
        summonHealthMultiplier.remove(playerId);
        summonDefenseMultiplier.remove(playerId);
        summonAttackSpeedMultiplier.remove(playerId);
        summonMovementSpeedMultiplier.remove(playerId);
        summonCritChance.remove(playerId);
        
        // Clear cooldowns
        wolfRespawnCooldowns.remove(playerId);
        boarRespawnCooldowns.remove(playerId);
        bearRespawnCooldowns.remove(playerId);
        
        // Cancel any cooldown notification tasks
        if (cooldownNotificationTasks.containsKey(playerId)) {
            cooldownNotificationTasks.get(playerId).cancel();
            cooldownNotificationTasks.remove(playerId);
        }
        
        // Clear bear guardian status
        bearGuardianActive.remove(playerId);
        
        // Clear boar frenzy stacks
        boarFrenzyExpiration.remove(playerId);
        
        // Clear anti-duplication tracking
        summonInProgress.remove(playerId);
        lastSummonCheck.remove(playerId);
    }
    
    /**
     * Checks if a player has purchased a skill
     * @param playerId The player's UUID
     * @param skillId The skill ID to check
     * @return Whether the player has purchased the skill
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        // Get the player's purchased skills from the plugin
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return false;
        
        // Check if the player has purchased the skill
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }
    
    /**
     * Checks if a player can add another summon type
     * @param playerId The player's UUID
     * @param summonId The summon ID to check
     * @return Whether the player can add another summon type
     */
    private boolean canAddSummonType(UUID playerId, int summonId) {
        // If the player already has this summon type, they can "add" it (replace the existing one)
        if (activeSummonSkills.containsKey(playerId) && activeSummonSkills.get(playerId).contains(summonId)) {
            return true;
        }
        
        // Check if the player has less than 2 summon types
        if (!activeSummonSkills.containsKey(playerId) || activeSummonSkills.get(playerId).size() < 2) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if a player has an active summon of a specific type
     * @param playerId The player's UUID
     * @param summonMap The map of summons to check
     * @return Whether the player has an active summon of the specified type
     */
    private boolean hasActiveSummon(UUID playerId, Map<UUID, UUID> summonMap) {
        if (!summonMap.containsKey(playerId)) return false;
        
        UUID summonId = summonMap.get(playerId);
        Entity summon = Bukkit.getEntity(summonId);
        
        return summon != null && !summon.isDead();
    }
    
    /**
     * Checks if a summon type is on cooldown
     * @param playerId The player's UUID
     * @param cooldownMap The map of cooldowns to check
     * @param cooldownDuration The duration of the cooldown
     * @return Whether the summon type is on cooldown
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownDuration) {
        if (!cooldownMap.containsKey(playerId)) return false;
        
        long lastUse = cooldownMap.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        return currentTime - lastUse < cooldownDuration;
    }
    
    /**
     * Sets the stats for a wolf summon
     * @param player The player who owns the wolf
     * @param wolf The wolf entity
     */
    private void setWolfStats(Player player, Wolf wolf) {
        // Set base stats
        wolf.setAdult();
        wolf.setAgeLock(true);
        wolf.setCollarColor(org.bukkit.DyeColor.RED);
        
        // Set attributes
        double baseHealth = 50.0;  // Changed from 20.0 to match requirements (balanced companion)
        double baseDamage = 4.0;
        
        UUID playerId = player.getUniqueId();
        
        // Calculate health with proper bonuses
        double finalHealth = baseHealth;
        
        // Apply percentage multipliers first
        double healthPercentMultiplier = 1.0;
        if (isPurchased(playerId, 100021)) healthPercentMultiplier += 0.30; // Skill 21: +30% hp
        if (isPurchased(playerId, 100022)) healthPercentMultiplier += 0.10; // Skill 22: +10% hp
        
        finalHealth *= healthPercentMultiplier;
        
        // Then add flat bonuses
        if (isPurchased(playerId, 100012)) finalHealth += 100; // Skill 12: +100 HP flat
        
        // Apply damage multiplier
        double damageMultiplier = 1.0;
        if (isPurchased(playerId, 100009)) damageMultiplier += 0.05; // Skill 9: +5% dmg
        if (isPurchased(playerId, 100014)) damageMultiplier += 0.10; // Skill 14: +10% dmg
        
        double finalDamage = baseDamage * damageMultiplier;
        
        // Set final values
        wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        wolf.setHealth(finalHealth);
        
        if (wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(finalDamage);
        }
        
        // Set movement speed
        double baseSpeed = 0.3;
        double speedMultiplier = summonMovementSpeedMultiplier.getOrDefault(playerId, 1.0);
        if (wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
        }
    }
    
    /**
     * Sets the stats for a boar summon
     * @param player The player who owns the boar
     * @param boar The boar entity (actually a wolf or pig)
     */
    private void setBoarStats(Player player, LivingEntity boar) {
        // Set base stats
        if (boar instanceof Wolf) {
            ((Wolf) boar).setAdult();
            ((Wolf) boar).setAgeLock(true);
            ((Wolf) boar).setCollarColor(org.bukkit.DyeColor.BROWN);
        }
        
        double baseHealth = 20.0;
        double baseDamage = 6.0;
        
        UUID playerId = player.getUniqueId();
        
        // Calculate health with proper bonuses
        double finalHealth = baseHealth;
        
        // Apply percentage multipliers first
        double healthPercentMultiplier = 1.0;
        if (isPurchased(playerId, 100021)) healthPercentMultiplier += 0.30; // Skill 21: +30% hp
        
        finalHealth *= healthPercentMultiplier;
        
        // Apply damage multiplier
        double damageMultiplier = 1.0;
        if (isPurchased(playerId, 100005)) damageMultiplier += 0.15; // Skill 5: +15% dmg
        if (isPurchased(playerId, 100009)) damageMultiplier += 0.05; // Skill 9: +5% dmg
        if (isPurchased(playerId, 100014)) damageMultiplier += 0.10; // Skill 14: +10% dmg
        if (isPurchased(playerId, 100026)) damageMultiplier += 0.15; // Skill 26: +15% dmg
        
        double finalDamage = baseDamage * damageMultiplier;
        
        // Set final values
        boar.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        boar.setHealth(finalHealth);
        
        if (boar.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            boar.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(finalDamage);
        }
        
        // Set movement speed
        double baseSpeed = 0.25;
        double speedMultiplier = 1.0;
        if (isPurchased(playerId, 100018)) speedMultiplier += 0.10; // Skill 18: +10% ms
        if (isPurchased(playerId, 100023)) speedMultiplier += 0.20; // Skill 23: +20% ms
        
        if (boar.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            boar.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
        }
    }
    
    /**
     * Sets the stats for a bear summon
     * @param player The player who owns the bear
     * @param bear The bear entity (actually a wolf or polar bear)
     */
    private void setBearStats(Player player, LivingEntity bear) {
        // Set base stats
        if (bear instanceof Wolf) {
            ((Wolf) bear).setAdult();
            ((Wolf) bear).setAgeLock(true);
            ((Wolf) bear).setCollarColor(org.bukkit.DyeColor.WHITE);
        }
        
        double baseHealth = 80.0;
        double baseDamage = 3.0;
        
        UUID playerId = player.getUniqueId();
        
        // Calculate health with proper bonuses
        double finalHealth = baseHealth;
        
        // Apply percentage multipliers first
        double healthPercentMultiplier = 1.0;
        if (isPurchased(playerId, 100006)) healthPercentMultiplier += 0.10; // Skill 6: +10% hp
        if (isPurchased(playerId, 100021)) healthPercentMultiplier += 0.30; // Skill 21: +30% hp
        
        finalHealth *= healthPercentMultiplier;
        
        // Then add flat bonuses
        if (isPurchased(playerId, 100016)) finalHealth += 200; // Skill 16: +200 HP flat
        
        // Apply damage multiplier
        double damageMultiplier = 1.0;
        if (isPurchased(playerId, 100009)) damageMultiplier += 0.05; // Skill 9: +5% dmg
        if (isPurchased(playerId, 100014)) damageMultiplier += 0.10; // Skill 14: +10% dmg
        
        double finalDamage = baseDamage * damageMultiplier;
        
        // Set final values
        bear.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        bear.setHealth(finalHealth);
        
        if (bear.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            bear.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(finalDamage);
        }
        
        // Set movement speed
        double baseSpeed = 0.2;
        double speedMultiplier = 1.0;
        if (isPurchased(playerId, 100018)) speedMultiplier += 0.10; // Skill 18: +10% ms
        
        if (bear.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            bear.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
        }
    }
    
    /**
     * Tracks a summon skill for a player
     * @param playerId The player's UUID
     * @param summonId The summon ID to track
     */
    private void trackSummonSkill(UUID playerId, int summonId) {
        // Initialize the set if it doesn't exist
        if (!activeSummonSkills.containsKey(playerId)) {
            activeSummonSkills.put(playerId, new HashSet<>());
        }
        
        // Add the summon ID to the set
        activeSummonSkills.get(playerId).add(summonId);
    }
    
    /**
     * Checks if an entity is a wolf summon for a player
     * @param playerId The player's UUID
     * @param entity The entity to check
     * @return Whether the entity is a wolf summon for the player
     */
    private boolean isWolfSummon(UUID playerId, Entity entity) {
        if (!playerWolf.containsKey(playerId)) return false;
        
        return entity.getUniqueId().equals(playerWolf.get(playerId)) || 
               (additionalWolves.containsKey(playerId) && additionalWolves.get(playerId).contains(entity.getUniqueId()));
    }
    
    /**
     * Checks if an entity is a boar summon for a player
     * @param playerId The player's UUID
     * @param entity The entity to check
     * @return Whether the entity is a boar summon for the player
     */
    private boolean isBoarSummon(UUID playerId, Entity entity) {
        if (!playerBoar.containsKey(playerId)) return false;
        
        return entity.getUniqueId().equals(playerBoar.get(playerId));
    }
    
    /**
     * Checks if an entity is a bear summon for a player
     * @param playerId The player's UUID
     * @param entity The entity to check
     * @return Whether the entity is a bear summon for the player
     */
    private boolean isBearSummon(UUID playerId, Entity entity) {
        if (!playerBear.containsKey(playerId)) return false;
        
        return entity.getUniqueId().equals(playerBear.get(playerId));
    }
    
    /**
     * Removes a summon type for a player
     * @param player The player
     * @param summonId The summon ID to remove
     */
    private void removeSummonType(Player player, int summonId) {
        UUID playerId = player.getUniqueId();
        
        switch (summonId) {
            case WOLF_SUMMON_ID:
                // Remove the wolf
                if (playerWolf.containsKey(playerId)) {
                    UUID wolfId = playerWolf.get(playerId);
                    Entity wolf = Bukkit.getEntity(wolfId);
                    if (wolf != null) wolf.remove();
                    playerWolf.remove(playerId);
                }
                
                // Remove additional wolves
                if (additionalWolves.containsKey(playerId)) {
                    for (UUID wolfId : additionalWolves.get(playerId)) {
                        Entity wolf = Bukkit.getEntity(wolfId);
                        if (wolf != null) wolf.remove();
                    }
                    additionalWolves.remove(playerId);
                }
                
                // Remove from active summon skills
                if (activeSummonSkills.containsKey(playerId)) {
                    activeSummonSkills.get(playerId).remove(WOLF_SUMMON_ID);
                }
                break;
                
            case BOAR_SUMMON_ID:
                // Remove the boar
                if (playerBoar.containsKey(playerId)) {
                    UUID boarId = playerBoar.get(playerId);
                    Entity boar = Bukkit.getEntity(boarId);
                    if (boar != null) boar.remove();
                    playerBoar.remove(playerId);
                }
                
                // Remove from active summon skills
                if (activeSummonSkills.containsKey(playerId)) {
                    activeSummonSkills.get(playerId).remove(BOAR_SUMMON_ID);
                }
                break;
                
            case BEAR_SUMMON_ID:
                // Remove the bear
                if (playerBear.containsKey(playerId)) {
                    UUID bearId = playerBear.get(playerId);
                    Entity bear = Bukkit.getEntity(bearId);
                    if (bear != null) bear.remove();
                    playerBear.remove(playerId);
                }
                
                // Remove from active summon skills
                if (activeSummonSkills.containsKey(playerId)) {
                    activeSummonSkills.get(playerId).remove(BEAR_SUMMON_ID);
                }
                break;
        }
    }
    
    /**
     * Handles entity damage events for the player's skills
     */
    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // This method is required by the BaseSkillEffectsHandler abstract class
        // For Beastmaster, we don't need to do anything special here as summon damage is handled separately
    }
    
    /**
     * Handles damage to summons and applies damage reduction
     */
    @EventHandler
    public void onSummonDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // Check if entity is a summon
        Player owner = getSummonOwner(entity);
        if (owner == null) return;
        
        UUID playerId = owner.getUniqueId();
        double damageReduction = 0.0;
        
        // Bear defense (Skill 10: +50% def)
        if (isBearSummon(playerId, entity) && isPurchased(playerId, 100010)) {
            damageReduction += 0.50;
        }
        
        // All summons defense (Skill 24: +25% def)
        if (isPurchased(playerId, 100024)) {
            damageReduction += 0.25;
        }
        
        // Bear Guardian active (Skill 15: +10% def when bear <50% hp)
        if (bearGuardianActive.getOrDefault(playerId, false)) {
            damageReduction += 0.10;
        }
        
        // Apply damage reduction (cap at 75% to prevent invincibility)
        damageReduction = Math.min(damageReduction, 0.75);
        if (damageReduction > 0) {
            double newDamage = event.getDamage() * (1.0 - damageReduction);
            event.setDamage(newDamage);
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Reduced damage to " + getEntityName(entity) + 
                    " by " + (damageReduction * 100) + "% (from " + 
                    event.getDamage() + " to " + newDamage + ")");
            }
        }
    }
    
    /**
     * Checks if Bear Guardian is active for a player
     * @param playerId The player's UUID
     * @return Whether Bear Guardian is active
     */
    public boolean isBearGuardianActive(UUID playerId) {
        return bearGuardianActive.getOrDefault(playerId, false);
    }
    
    /**
     * Helper method to heal a summon
     * @param summonId UUID of the summon to heal
     * @param healAmount Amount to heal
     * @param type Type of summon (for debug messages)
     */
    private void healSummon(UUID summonId, double healAmount, String type) {
        if (summonId == null) return;
        
        Entity entity = Bukkit.getEntity(summonId);
        if (entity != null && entity instanceof LivingEntity) {
            LivingEntity summon = (LivingEntity) entity;
            double currentHealth = summon.getHealth();
            double maxHealth = summon.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double newHealth = Math.min(currentHealth + healAmount, maxHealth);
            summon.setHealth(newHealth);
            
            if (debuggingFlag == 1 && healAmount > 0) {
                plugin.getLogger().info("Healed " + type + " for " + String.format("%.1f", healAmount) + " HP");
            }
        }
    }

    /**
     * Handles entity damage by entity events for the player's skills
     * This is where we handle summon damage bonuses and special effects
     */
    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        // Get the damager and the entity being damaged
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Check if the damager is one of the player's summons
        boolean isPlayerSummon = isWolfSummon(playerId, damager) || 
                                isBoarSummon(playerId, damager) || 
                                isBearSummon(playerId, damager);
        
        if (isPlayerSummon) {
            // Apply damage multiplier from skills
            double damageMultiplier = summonDamageMultiplier.getOrDefault(playerId, 1.0);
            double newDamage = event.getDamage() * damageMultiplier;
            
            // Apply critical hit chance based on summon type
            double critChance = 0.0;
            String critSource = "";
            
            if (isWolfSummon(playerId, damager) && isPurchased(playerId, 100011)) {
                critChance = 10.0; // 10% for wolves
                critSource = "Wolf";
            } else if (isBoarSummon(playerId, damager) && isPurchased(playerId, 100013)) {
                critChance = 15.0; // 15% for boars
                critSource = "Boar";
            }
            
            if (critChance > 0 && rollChance(critChance, player, critSource + " Critical Hit")) {
                newDamage *= 2.0; // CRIT = 200% DMG as per Beastmaster.md
                
                // Visual effect
                if (entity instanceof LivingEntity) {
                    entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5);
                }
                
                if (debuggingFlag == 1) {
                    player.sendMessage(ChatColor.RED + "Your " + critSource + " landed a critical hit!");
                }
            }
            
            // Set the new damage
            event.setDamage(newDamage);
            
            // Handle wolf healing (Skill 17)
            if (isWolfSummon(playerId, damager) && isPurchased(playerId, 100017)) {
                double healAmount = newDamage * 0.05; // 5% of damage dealt
                double currentHealth = player.getHealth();
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = Math.min(currentHealth + healAmount, maxHealth);
                player.setHealth(newHealth);
                
                if (debuggingFlag == 1 && healAmount > 0) {
                    player.sendMessage(ChatColor.GREEN + "Healed for " + String.format("%.1f", healAmount) + " HP from wolf damage!");
                }
            }

            // Handle player damage healing summons (Skill 27)
            if (damager.equals(player) && isPurchased(playerId, 100027)) {
                double healAmount = event.getDamage() * 0.05; // 5% of damage dealt
                
                // Heal all summons
                healSummon(playerWolf.get(playerId), healAmount, "Wolf");
                healSummon(playerBoar.get(playerId), healAmount, "Boar");
                healSummon(playerBear.get(playerId), healAmount, "Bear");
                
                // Heal additional wolves
                if (additionalWolves.containsKey(playerId)) {
                    for (UUID wolfId : additionalWolves.get(playerId)) {
                        healSummon(wolfId, healAmount, "Wolf");
                    }
                }
            }
            
            // Handle special effects for different summon types
            if (isWolfSummon(playerId, damager)) {
                // Wolf special effect: chance to apply bleeding
                // Implementation would go here
            } else if (isBoarSummon(playerId, damager)) {
                // Check if boar has frenzy active
                Map<UUID, Long> playerFrenzies = boarFrenzyExpiration.get(playerId);
                if (playerFrenzies != null && playerFrenzies.containsKey(damager.getUniqueId())) {
                    if (System.currentTimeMillis() < playerFrenzies.get(damager.getUniqueId())) {
                        // Frenzy is active - simulate 7% attack speed by small damage boost
                        newDamage *= 1.07;
                        
                        if (debuggingFlag == 1) {
                            plugin.getLogger().info("Boar frenzy active - damage increased by 7%");
                        }
                    } else {
                        // Frenzy expired, remove it
                        playerFrenzies.remove(damager.getUniqueId());
                    }
                }
            } else if (isBearSummon(playerId, damager)) {
                // Bear special effect: chance to apply guardian buff
                // Implementation would go here
            }
        }
        
        // Check if the entity being damaged is the player and they have a bear summon with guardian active
        if (entity.equals(player) && playerBear.containsKey(playerId) && bearGuardianActive.getOrDefault(playerId, false)) {
            // Reduce damage taken while bear guardian is active
            double damageReduction = 0.2; // 20% damage reduction
            double newDamage = event.getDamage() * (1.0 - damageReduction);
            event.setDamage(newDamage);
            
            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.GREEN + "Your bear guardian reduced damage by 20%!");
            }
        }
    }
    
    /**
     * Handles entity death events for the player's skills
     * This is where we handle summon death and respawn mechanics
     */
    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        Entity entity = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Check if a boar killed the entity (for Boar Frenzy)
        Entity killer = event.getEntity().getKiller();
        if (killer != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                UUID pId = p.getUniqueId();
                if (isBoarSummon(pId, killer) && isPurchased(pId, 100019)) {
                    // Apply frenzy buff
                    boarFrenzyExpiration.computeIfAbsent(pId, k -> new HashMap<>())
                        .put(killer.getUniqueId(), System.currentTimeMillis() + 3000); // 3 seconds
                    
                    if (debuggingFlag == 1) {
                        p.sendMessage(ChatColor.RED + "Your boar enters a frenzy! +7% attack speed for 3s!");
                    }
                }
            }
        }
        
        // Check if the entity that died is one of the player's summons
        if (isWolfSummon(playerId, entity)) {
            // Handle wolf death
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + "'s wolf died");
                player.sendMessage(ChatColor.RED + "Your wolf has died!");
            }
            
            // Remove the wolf from tracking
            if (playerWolf.containsKey(playerId) && playerWolf.get(playerId).equals(entity.getUniqueId())) {
                playerWolf.remove(playerId);
                
                // Start cooldown for respawn
                wolfRespawnCooldowns.put(playerId, System.currentTimeMillis());
                
                // Schedule automatic respawn after cooldown with better checks
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Player p = Bukkit.getPlayer(playerId);
                    if (p != null && p.isOnline() && !p.isDead()) {
                        // Check if player is in the same world or has changed worlds
                        if (entity.getWorld().equals(p.getWorld())) {
                            // Check if player doesn't already have this summon type
                            if (!hasActiveSummon(playerId, playerWolf)) {
                                summonWolves(p);
                                if (debuggingFlag == 1) {
                                    p.sendMessage(ChatColor.GREEN + "Your wolf has been automatically resummoned!");
                                }
                            }
                        } else {
                            // Player changed worlds, clear cooldown to allow immediate summon in new world
                            wolfRespawnCooldowns.remove(playerId);
                        }
                    }
                }, WOLF_SUMMON_COOLDOWN / 50); // Convert milliseconds to ticks
                
                // Store the task for cancellation if needed
                cooldownNotificationTasks.put(playerId, task);
            }
            // Handle additional wolves
            else if (additionalWolves.containsKey(playerId) && additionalWolves.get(playerId).contains(entity.getUniqueId())) {
                additionalWolves.get(playerId).remove(entity.getUniqueId());
                
                // If all additional wolves are dead, remove the list
                if (additionalWolves.get(playerId).isEmpty()) {
                    additionalWolves.remove(playerId);
                }
            }
        }
        else if (isBoarSummon(playerId, entity)) {
            // Handle boar death
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + "'s boar died");
                player.sendMessage(ChatColor.RED + "Your boar has died!");
            }
            
            // Remove the boar from tracking
            if (playerBoar.containsKey(playerId) && playerBoar.get(playerId).equals(entity.getUniqueId())) {
                playerBoar.remove(playerId);
                
                // Start cooldown for respawn
                boarRespawnCooldowns.put(playerId, System.currentTimeMillis());
                
                // Schedule automatic respawn after cooldown with better checks
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Player p = Bukkit.getPlayer(playerId);
                    if (p != null && p.isOnline() && !p.isDead()) {
                        // Check if player is in the same world or has changed worlds
                        if (entity.getWorld().equals(p.getWorld())) {
                            // Check if player doesn't already have this summon type
                            if (!hasActiveSummon(playerId, playerBoar)) {
                                summonBoars(p);
                                if (debuggingFlag == 1) {
                                    p.sendMessage(ChatColor.GREEN + "Your boar has been automatically resummoned!");
                                }
                            }
                        } else {
                            // Player changed worlds, clear cooldown to allow immediate summon in new world
                            boarRespawnCooldowns.remove(playerId);
                        }
                    }
                }, BOAR_SUMMON_COOLDOWN / 50); // Convert milliseconds to ticks
                
                // Store the task for cancellation if needed
                cooldownNotificationTasks.put(playerId, task);
            }
        }
        else if (isBearSummon(playerId, entity)) {
            // Handle bear death
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + "'s bear died");
                player.sendMessage(ChatColor.RED + "Your bear has died!");
            }
            
            // Remove the bear from tracking
            if (playerBear.containsKey(playerId) && playerBear.get(playerId).equals(entity.getUniqueId())) {
                playerBear.remove(playerId);
                
                // Deactivate bear guardian buff
                bearGuardianActive.put(playerId, false);
                
                // Start cooldown for respawn
                bearRespawnCooldowns.put(playerId, System.currentTimeMillis());
                
                // Schedule automatic respawn after cooldown with better checks
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Player p = Bukkit.getPlayer(playerId);
                    if (p != null && p.isOnline() && !p.isDead()) {
                        // Check if player is in the same world or has changed worlds
                        if (entity.getWorld().equals(p.getWorld())) {
                            // Check if player doesn't already have this summon type
                            if (!hasActiveSummon(playerId, playerBear)) {
                                summonBears(p);
                                if (debuggingFlag == 1) {
                                    p.sendMessage(ChatColor.GREEN + "Your bear has been automatically resummoned!");
                                }
                            }
                        } else {
                            // Player changed worlds, clear cooldown to allow immediate summon in new world
                            bearRespawnCooldowns.remove(playerId);
                        }
                    }
                }, BEAR_SUMMON_COOLDOWN / 50); // Convert milliseconds to ticks
                
                // Store the task for cancellation if needed
                cooldownNotificationTasks.put(playerId, task);
            }
        }
    }
    
    /**
     * Handle player death and clean up all summons
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Player " + player.getName() + " died, cleaning up summons");
        }
        
        // Clean up all summons
        clearPlayerData(playerId);
    }
    
    /**
     * Handle player respawn to clean up summons in old world
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Get player's ascendancy
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Beastmaster".equals(ascendancy)) {
            return;
        }
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Player " + player.getName() + " respawned, scheduling summon cleanup and respawn");
        }
        
        // Clean up all summons immediately
        clearPlayerData(playerId);
        
        // Schedule resummon after a longer delay to ensure player is fully respawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !player.isDead()) {
                checkAndSummonCreatures(player);
            }
        }, 60L); // 3 second delay
    }
    
    /**
     * Better handling for teleportation including warps
     */
    @EventHandler(priority = EventPriority.HIGHEST)  
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player is a Beastmaster
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
        if (!"Beastmaster".equals(ascendancy)) {
            return;
        }
        
        // Check if it's a cross-world teleport or significant distance
        boolean crossWorld = !event.getFrom().getWorld().equals(event.getTo().getWorld());
        boolean longDistance = false;
        
        // Only calculate distance if in same world to avoid errors
        if (!crossWorld) {
            try {
                longDistance = event.getFrom().distance(event.getTo()) > 100; // More than 100 blocks
            } catch (Exception e) {
                // Handle potential errors with distance calculation
                plugin.getLogger().warning("Error calculating teleport distance: " + e.getMessage());
            }
        }
        
        if (crossWorld || longDistance) {
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " teleported far/cross-world, cleaning up summons");
            }
            
            // Clean up all summons
            clearPlayerData(playerId);
            
            // Reset cooldowns for instant resummon in new location
            wolfRespawnCooldowns.remove(playerId);
            boarRespawnCooldowns.remove(playerId);
            bearRespawnCooldowns.remove(playerId);
            
            // Schedule resummon after teleport completes
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !player.isDead()) {
                    checkAndSummonCreatures(player);
                }
            }, 40L); // 2 second delay
        }
    }
    
    /**
     * Handle player changing worlds and move summons with them
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (debuggingFlag == 1) {
            plugin.getLogger().info("Player " + player.getName() + " changed worlds, cleaning up summons");
        }
        
        // Clean up all summons in the old world
        clearPlayerData(playerId);
        
        // Schedule resummon after a delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check which summons the player had active before world change
            if (activeSummonSkills.containsKey(playerId)) {
                Set<Integer> skills = new HashSet<>(activeSummonSkills.get(playerId));
                
                for (int skillId : skills) {
                    if (skillId == WOLF_SUMMON_ID) {
                        summonWolves(player);
                    } else if (skillId == BOAR_SUMMON_ID) {
                        summonBoars(player);
                    } else if (skillId == BEAR_SUMMON_ID) {
                        summonBears(player);
                    }
                }
            }
        }, AUTO_RESUMMON_DELAY / 50); // Convert milliseconds to ticks (20 ticks = 1 second)
    }
}