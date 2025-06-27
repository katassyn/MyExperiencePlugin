package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.ClassNameNormalizer;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.DragonKnightSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.RangerSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.SpellWeaverSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.events.SkillPurchasedEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkillEffectsHandler implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final Random random = new Random();
    // Debugging flag - set to 0 after testing
    private final int debuggingFlag = 1;

    // Cached player stats
    private final Map<UUID, PlayerSkillStats> playerStatsCache = new ConcurrentHashMap<>();

    // Class-specific handlers
    private final Map<String, BaseSkillEffectsHandler> classHandlers = new HashMap<>();
    private final RangerSkillEffectsHandler rangerHandler;
    private final DragonKnightSkillEffectsHandler dragonKnightHandler;

    // Constants for attribute modifier names
    private static final String ATTR_MAX_HEALTH = "skill.maxhealth";
    private static final String ATTR_MOVEMENT_SPEED = "skill.movementspeed";
    private static final String ATTR_ATTACK_DAMAGE = "skill.attackdamage";
    private static final String ATTR_ARMOR = "skill.armor";
    private static final String ATTR_LUCK = "skill.luck";

    // Flag to prevent stacking multiple damage bonus messages
    private final Map<UUID, Long> lastDamageMessageTime = new HashMap<>();
    private static final long DAMAGE_MESSAGE_COOLDOWN = 1000; // 1 second cooldown

    // Add a field to track if the listener is currently handling an event
    private boolean isHandlingSkillEvent = false;

    private final SpellWeaverSkillEffectsHandler spellWeaverHandler;

    public SkillEffectsHandler(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;

        // Initialize class-specific handlers
        this.rangerHandler = new RangerSkillEffectsHandler(plugin);
        this.dragonKnightHandler = new DragonKnightSkillEffectsHandler(plugin);
        this.spellWeaverHandler = new SpellWeaverSkillEffectsHandler(plugin);

        // Register handlers in the map
        classHandlers.put("Ranger", rangerHandler);
        classHandlers.put("DragonKnight", dragonKnightHandler);
        classHandlers.put("SpellWeaver", spellWeaverHandler);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SkillEffectsHandler initialized with handlers for: " +
                    String.join(", ", classHandlers.keySet()));
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());

        // Completely recalculate all stats on join
        recalculateAllStats(player);

        // Initialize cooldowns for spell-based classes
        if ("SpellWeaver".equals(playerClass)) {
            spellWeaverHandler.initializePlayerSpellCooldowns(player);
        }

        if (debuggingFlag == 1) {
            // Debug log output
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(playerId);

        // Clear player data from cache
        playerStatsCache.remove(playerId);

        // Clear class-specific handler data
        if ("DragonKnight".equals(playerClass)) {
            dragonKnightHandler.clearPlayerData(playerId);
        } else if ("SpellWeaver".equals(playerClass)) {
            spellWeaverHandler.clearPlayerData(playerId);
        }

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared all skill effect data for " + player.getName() + " on logout");
        }
    }

    /**
     * Listen for skill purchase events to update stats
     */
    @EventHandler
    public void onSkillPurchased(SkillPurchasedEvent event) {
        if (isHandlingSkillEvent) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("Recursive skill event detected! Ignoring to prevent loop.");
            }
            return;
        }

        isHandlingSkillEvent = true;

        try {
            Player player = event.getPlayer();
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SkillPurchasedEvent received for player " + player.getName() +
                        ", skillId: " + event.getSkillId());
            }

            // Completely recalculate stats when a skill is purchased
            recalculateAllStats(player);
        } finally {
            isHandlingSkillEvent = false;
        }
    }

    /**
     * Complete stats recalculation, cleaning everything first
     */
    private void recalculateAllStats(Player player) {
        // First, clear all cached data and modifiers
        clearAllSkillModifiers(player);

        // Then calculate fresh stats
        calculatePlayerStats(player);

        // Then apply them
        applyPlayerStats(player);
    }

    /**
     * Clears all skill-related attribute modifiers from the player
     */
    private void clearAllSkillModifiers(Player player) {
        clearAttributeModifiers(player, Attribute.GENERIC_MAX_HEALTH, ATTR_MAX_HEALTH);
        clearAttributeModifiers(player, Attribute.GENERIC_MOVEMENT_SPEED, ATTR_MOVEMENT_SPEED);
        clearAttributeModifiers(player, Attribute.GENERIC_ATTACK_DAMAGE, ATTR_ATTACK_DAMAGE);
        clearAttributeModifiers(player, Attribute.GENERIC_ARMOR, ATTR_ARMOR);
        clearAttributeModifiers(player, Attribute.GENERIC_LUCK, ATTR_LUCK);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Cleared skill modifiers for " + player.getName());
        }
    }

    /**
     * Clears modifiers with the given name prefix from the specified attribute
     */
    private void clearAttributeModifiers(Player player, Attribute attribute, String namePrefix) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr != null) {
            // Create a copy of modifiers to avoid ConcurrentModificationException
            List<AttributeModifier> toRemove = new ArrayList<>();
            attr.getModifiers().forEach(mod -> {
                if (mod.getName().startsWith(namePrefix)) {
                    toRemove.add(mod);
                }
            });

            // Now remove them
            toRemove.forEach(attr::removeModifier);
        }
    }

    /**
     * Applies the calculated stats to the player via attribute modifiers
     */
    private void applyPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkillStats stats = playerStatsCache.get(uuid);
        if (stats == null) return;

        // Apply max health bonus
        if (stats.getMaxHealthBonus() > 0) {
            AttributeModifier healthMod = new AttributeModifier(
                    ATTR_MAX_HEALTH,
                    stats.getMaxHealthBonus(),
                    AttributeModifier.Operation.ADD_NUMBER
            );

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied +" + stats.getMaxHealthBonus() + " max health to " + player.getName());
            }
        }

        // Apply movement speed bonus (as percentage)
        if (stats.getMovementSpeedBonus() != 0) {
            double speedBonus = stats.getMovementSpeedBonus() / 100.0;
            AttributeModifier speedMod = new AttributeModifier(
                    ATTR_MOVEMENT_SPEED,
                    speedBonus,
                    AttributeModifier.Operation.ADD_SCALAR
            );

            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(speedMod);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Applied " + stats.getMovementSpeedBonus() + "% movement speed to " + player.getName());
            }
        }

        // Apply other attributes as needed...
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        PlayerSkillStats stats = getPlayerStats(player);
        String playerClass = plugin.getClassManager().getPlayerClass(playerId);

        // Get the appropriate handler for this player's class
        BaseSkillEffectsHandler handler = classHandlers.get(playerClass);
        if (handler != null) {
            handler.handleEntityDamage(event, player, stats);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply common damage effects

        // Apply bonus damage
        if (stats.getBonusDamage() > 0) {
            event.setDamage(event.getDamage() + stats.getBonusDamage());

            // Only show the message if cooldown has passed
            long currentTime = System.currentTimeMillis();
            if (!lastDamageMessageTime.containsKey(playerId) ||
                    currentTime - lastDamageMessageTime.get(playerId) > DAMAGE_MESSAGE_COOLDOWN) {
                if (debuggingFlag == 1) {
                    player.sendMessage(ChatColor.DARK_GRAY + "Skill bonus damage: +" + stats.getBonusDamage());
                }
                lastDamageMessageTime.put(playerId, currentTime);
            }
        }

        // Check for critical hit
        boolean isCritical = false;
        if (plugin.getCriticalStrikeSystem() != null && event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
            isCritical = plugin.getCriticalStrikeSystem().rollForCritical(player);

            if (isCritical) {
                // Get critical damage multiplier
                double critMultiplier = plugin.getCriticalStrikeSystem().getCriticalDamageMultiplier(player);

                // Apply critical damage
                double originalDamage = event.getDamage();
                event.setDamage(originalDamage * critMultiplier);

                // Show critical hit effects
                com.maks.myexperienceplugin.Class.skills.effects.BerserkerVisualEffects.playCriticalHitEffect(
                    player, (org.bukkit.entity.LivingEntity) event.getEntity());

                // Notify player
                ActionBarUtils.sendActionBar(player, 
                    ChatColor.RED + "CRITICAL HIT! " + 
                    ChatColor.GOLD + String.format("%.1fx", critMultiplier) + " " + 
                    ChatColor.YELLOW + "damage");

                if (debuggingFlag == 1) {
                    player.sendMessage(ChatColor.DARK_GRAY + "Critical hit! x" + 
                        String.format("%.2f", critMultiplier) + " damage (" + 
                        String.format("%.1f", originalDamage) + " → " + 
                        String.format("%.1f", event.getDamage()) + ")");
                }

                // Update last message time
                lastDamageMessageTime.put(playerId, System.currentTimeMillis());
            }
        }

        // Apply damage multiplier (if not already a critical hit)
        if (!isCritical && stats.getDamageMultiplier() != 1.0) {
            double newDamage = event.getDamage() * stats.getDamageMultiplier();
            event.setDamage(newDamage);
            if (debuggingFlag == 1 &&
                    (!lastDamageMessageTime.containsKey(playerId) ||
                            System.currentTimeMillis() - lastDamageMessageTime.get(playerId) > DAMAGE_MESSAGE_COOLDOWN)) {
                player.sendMessage(ChatColor.DARK_GRAY + "Damage multiplier: x" + String.format("%.2f", stats.getDamageMultiplier()));
                lastDamageMessageTime.put(playerId, System.currentTimeMillis());
            }
        }

        // Now delegate to class-specific handler
        String playerClass = plugin.getClassManager().getPlayerClass(playerId);
        BaseSkillEffectsHandler handler = classHandlers.get(playerClass);
        if (handler != null) {
            handler.handleEntityDamageByEntity(event, player, stats);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            UUID playerId = player.getUniqueId();
            PlayerSkillStats stats = getPlayerStats(player);

            // Delegate to class-specific handler
            String playerClass = plugin.getClassManager().getPlayerClass(playerId);
            BaseSkillEffectsHandler handler = classHandlers.get(playerClass);
            if (handler != null) {
                handler.handleEntityDeath(event, player, stats);
            }
        }
    }

    // UPDATED: Complete rewrite of calculatePlayerStats with delegation to class-specific handlers
    // and proper accumulation of bonuses
    public void calculatePlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        // Initialize a completely new stats object
        PlayerSkillStats stats = new PlayerSkillStats();

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            playerStatsCache.put(uuid, stats);
            if (debuggingFlag == 1) {
                plugin.getLogger().info("Player " + player.getName() + " has no class, using default stats");
            }
            return;
        }

        // Get the class-specific handler
        BaseSkillEffectsHandler classHandler = classHandlers.get(playerClass);
        if (classHandler == null) {
            if (debuggingFlag == 1) {
                plugin.getLogger().warning("No handler found for class: " + playerClass);
            }
            playerStatsCache.put(uuid, stats);
            return;
        }

        // Get all purchased skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);
        Map<Integer, Integer> purchaseCounts = skillTreeManager.getPurchaseCountMap(uuid);

        // For tracking total bonus damage for debugging
        double totalBonusDamage = 0;

        // Process all skills through the class handler
        for (int skillId : purchasedSkills) {
            // Only process basic class skills (IDs 1-99)
            if (skillId >= 1 && skillId <= 99) {
                int count = purchaseCounts.getOrDefault(skillId, 1);

                // Record current damage before applying new effects
                double previousDamage = stats.getBonusDamage();

                // Apply effect for this skill
                classHandler.applySkillEffects(stats, skillId, count);

                // Track damage increase for debugging
                double damageIncrease = stats.getBonusDamage() - previousDamage;
                if (damageIncrease > 0) {
                    totalBonusDamage += damageIncrease;
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("Skill " + skillId + " added " + damageIncrease +
                                " damage for player " + player.getName() +
                                " (purchase count: " + count + ")");
                    }
                }
            }
        }

        // Process ascendancy skills if applicable
        if (!ascendancy.isEmpty()) {
            // Only use handlers registered directly in classHandlers map
            BaseSkillEffectsHandler ascendancyHandler = classHandlers.get(ascendancy);

            if (ascendancyHandler != null) {
                for (int skillId : purchasedSkills) {
                    // Only process ascendancy skills (IDs 100000+)
                    if (skillId >= 100000) {
                        int count = purchaseCounts.getOrDefault(skillId, 1);

                        // Record current damage before applying new effects
                        double previousDamage = stats.getBonusDamage();

                        // Apply effect for this skill
                        ascendancyHandler.applySkillEffects(stats, skillId, count);

                        // Track damage increase for debugging
                        double damageIncrease = stats.getBonusDamage() - previousDamage;
                        if (damageIncrease > 0) {
                            totalBonusDamage += damageIncrease;
                            if (debuggingFlag == 1) {
                                plugin.getLogger().info("Ascendancy skill " + skillId + " added " + damageIncrease +
                                        " damage for player " + player.getName() +
                                        " (purchase count: " + count + ")");
                            }
                        }
                    }
                }
            } else if (debuggingFlag == 1) {
                plugin.getLogger().warning("No handler found for ascendancy: " + ascendancy +
                        ". Make sure to register handlers with registerClassHandler() method.");
            }
        }

        // Initialize critical strike stats from the CriticalStrikeSystem
        if (plugin.getCriticalStrikeSystem() != null) {
            // Set critical chance from PlayerSkillStats to CriticalStrikeSystem
            if (stats.getCriticalChance() > 0) {
                plugin.getCriticalStrikeSystem().setBaseCritChance(player, stats.getCriticalChance());
            }

            // Set critical damage bonus from PlayerSkillStats to CriticalStrikeSystem
            if (stats.getCriticalDamageBonus() > 0) {
                // Default multiplier is 2.0, add the bonus
                plugin.getCriticalStrikeSystem().setCritDamageMultiplier(
                    player, 2.0 + stats.getCriticalDamageBonus());
            }

            if (debuggingFlag == 1) {
                plugin.getLogger().info("Initialized critical strike stats for " + player.getName() + ": " +
                        "Chance=" + stats.getCriticalChance() + "%, " +
                        "Multiplier=" + (2.0 + stats.getCriticalDamageBonus()) + "x");
            }
        }

        // Store the final stats
        playerStatsCache.put(uuid, stats);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Calculated stats for player " + player.getName() + " (" + playerClass + "): " +
                    "HP+" + stats.getMaxHealthBonus() + ", " +
                    "DMG+" + stats.getBonusDamage() + " (total from all skills: " + totalBonusDamage + "), " +
                    "MULT×" + stats.getDamageMultiplier() + 
                    (stats.getCriticalChance() > 0 ? ", CRIT=" + stats.getCriticalChance() + "%" : ""));
        }
    }

    public PlayerSkillStats getPlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerStatsCache.containsKey(uuid)) {
            calculatePlayerStats(player);
        }
        return playerStatsCache.get(uuid);
    }

    // Method to recalculate stats when skills change
    public void refreshPlayerStats(Player player) {
        recalculateAllStats(player);
    }

    // Inner class to store player skill-based stats
    public static class PlayerSkillStats {
        private double bonusDamage = 0;
        private double damageMultiplier = 1.0;
        private double evadeChance = 0;
        private double shieldBlockChance = 0;
        private double defenseBonus = 0;
        private double maxHealthBonus = 0;
        private double movementSpeedBonus = 0;
        private double luckBonus = 0;
        private double goldPerKill = 0;
        private boolean hasRegenerationEffect = false;
        private boolean hasTripleStrike = false;
        // For Ranger - Wind Stacks
        private int windStacks = 0;
        private int maxWindStacks = 3; // Default 3, increased to 5 by Wind Mastery skill
        private long windStacksExpiryTime = 0; // Time when stacks expire
        private double spellDamageBonus = 0;
        private double spellDamageMultiplier = 1.0;
        private double spellCriticalChance = 0;
        private boolean hasFireResistance = false;
        // Critical strike system
        private double criticalChance = 0;
        private double criticalDamageBonus = 0; // Bonus to base 2x dmg
        // Attack speed bonus
        private double attackSpeed = 0;
        public double getSpellDamageBonus() {
            return spellDamageBonus;
        }

        public void setSpellDamageBonus(double amount) {
            this.spellDamageBonus = amount;
        }

        public void addSpellDamageBonus(double amount) {
            this.spellDamageBonus += amount;
        }

        public double getSpellDamageMultiplier() {
            return spellDamageMultiplier;
        }

        public void setSpellDamageMultiplier(double amount) {
            this.spellDamageMultiplier = amount;
        }

        public void multiplySpellDamageMultiplier(double factor) {
            this.spellDamageMultiplier *= factor;
        }

        public double getSpellCriticalChance() {
            return spellCriticalChance;
        }

        public void setSpellCriticalChance(double amount) {
            this.spellCriticalChance = amount;
        }

        public void addSpellCriticalChance(double amount) {
            this.spellCriticalChance += amount;
        }

        public boolean hasFireResistance() {
            return hasFireResistance;
        }

        public void setHasFireResistance(boolean hasFireResistance) {
            this.hasFireResistance = hasFireResistance;
        }

        public double getCriticalChance() {
            return criticalChance;
        }

        public void setCriticalChance(double chance) {
            this.criticalChance = chance;
        }

        public void addCriticalChance(double amount) {
            this.criticalChance += amount;
        }

        public double getCriticalDamageBonus() {
            return criticalDamageBonus;
        }

        public void setCriticalDamageBonus(double bonus) {
            this.criticalDamageBonus = bonus;
        }

        public void addCriticalDamageBonus(double amount) {
            this.criticalDamageBonus += amount;
        }

        public double getAttackSpeed() {
            return attackSpeed;
        }

        public void setAttackSpeed(double attackSpeed) {
            this.attackSpeed = attackSpeed;
        }

        public void addAttackSpeed(double amount) {
            this.attackSpeed += amount;
        }
        public int getWindStacks() {
            return windStacks;
        }

        public void setWindStacks(int windStacks) {
            this.windStacks = Math.min(windStacks, maxWindStacks);
        }

        public void addWindStack() {
            this.windStacks = Math.min(windStacks + 1, maxWindStacks);
            this.windStacksExpiryTime = System.currentTimeMillis() + 30000; // 30 seconds duration
        }

        public void loseWindStack() {
            this.windStacks = Math.max(0, windStacks - 1);
        }

        public int getMaxWindStacks() {
            return maxWindStacks;
        }

        public void setMaxWindStacks(int maxWindStacks) {
            this.maxWindStacks = maxWindStacks;
        }

        public boolean hasWindStacksExpired() {
            return System.currentTimeMillis() > windStacksExpiryTime;
        }
        // Getters
        public double getBonusDamage() {
            return bonusDamage;
        }

        public double getDamageMultiplier() {
            return damageMultiplier;
        }

        public double getEvadeChance() {
            return evadeChance;
        }

        public double getShieldBlockChance() {
            return shieldBlockChance;
        }

        public double getDefenseBonus() {
            return defenseBonus;
        }

        public double getMaxHealthBonus() {
            return maxHealthBonus;
        }

        public double getMovementSpeedBonus() {
            return movementSpeedBonus;
        }

        public double getLuckBonus() {
            return luckBonus;
        }

        public double getGoldPerKill() {
            return goldPerKill;
        }

        // Setters - use these instead of adders for most properties
        public void setBonusDamage(double amount) {
            this.bonusDamage = amount;
        }

        public void setDamageMultiplier(double amount) {
            this.damageMultiplier = amount;
        }

        public void multiplyDamageMultiplier(double factor) {
            this.damageMultiplier *= factor;
        }

        public void setEvadeChance(double amount) {
            this.evadeChance = amount;
        }

        public void setShieldBlockChance(double amount) {
            this.shieldBlockChance = amount;
        }

        public void setDefenseBonus(double amount) {
            this.defenseBonus = amount;
        }

        public void setMaxHealthBonus(double amount) {
            this.maxHealthBonus = amount;
        }

        public void setMovementSpeedBonus(double amount) {
            this.movementSpeedBonus = amount;
        }

        public void setLuckBonus(double amount) {
            this.luckBonus = amount;
        }

        public void setGoldPerKill(double amount) {
            this.goldPerKill = amount;
        }

        // Adders - use these for incremental changes
        public void addBonusDamage(double amount) {
            this.bonusDamage += amount;
        }

        public void addDamageMultiplier(double amount) {
            this.damageMultiplier += amount;
        }

        public void addEvadeChance(double amount) {
            this.evadeChance += amount;
        }

        public void addShieldBlockChance(double amount) {
            this.shieldBlockChance += amount;
        }

        public void addDefenseBonus(double amount) {
            this.defenseBonus += amount;
        }

        public void addMaxHealth(double amount) {
            this.maxHealthBonus += amount;
        }

        public void addMovementSpeedBonus(double amount) {
            this.movementSpeedBonus += amount;
        }

        public void addLuckBonus(double amount) {
            this.luckBonus += amount;
        }

        public void addGoldPerKill(double amount) {
            this.goldPerKill += amount;
        }
        public boolean hasRegenerationEffect() {
            return hasRegenerationEffect;
        }

        public void setHasRegenerationEffect(boolean hasRegenerationEffect) {
            this.hasRegenerationEffect = hasRegenerationEffect;
        }

        public boolean hasTripleStrike() {
            return hasTripleStrike;
        }

        public void setHasTripleStrike(boolean hasTripleStrike) {
            this.hasTripleStrike = hasTripleStrike;
        }
    }

    public void applyRegenerationEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerSkillStats stats = getPlayerStats(player);

            if (stats.hasRegenerationEffect()) {
                // Apply Regeneration I effect (Duration: 3 seconds, amplifier 0 = level 1)
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, true, false));

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("Applied Regeneration I to " + player.getName() + " from Nature's Recovery skill");
                }
            }
        }
    }

    /**
     * Initialize periodic tasks for skill effects
     */
    public void initializePeriodicTasks() {
        // Apply regeneration effect every 3 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, this::applyRegenerationEffects, 20L, 60L);

        // Apply SpellWeaver fire resistance every 5 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getClassManager().getPlayerClass(player.getUniqueId()).equals("SpellWeaver")) {
                    spellWeaverHandler.applyFireResistanceEffect(player);
                }
            }
        }, 20L, 100L);

        // Apply other periodic effects every second
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applyActiveSkillEffects(player);
            }
        }, 20L, 20L);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Initialized periodic skill effect tasks");
        }
    }

    // Add this method to SkillEffectsHandler class

    /**
     * Register a class handler that will be used for skill effects
     * @param className The class name (capitalized correctly)
     * @param handler The handler instance
     */
    public void registerClassHandler(String className, BaseSkillEffectsHandler handler) {
        if (className == null || className.isEmpty() || handler == null) {
            return;
        }

        // Normalize class name
        String normalizedClassName = ClassNameNormalizer.normalize(className);

        classHandlers.put(normalizedClassName, handler);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Registered skill effects handler for class: " + normalizedClassName);
        }
    }

    /**
     * Get the class-specific handler for a player
     * @param player The player
     * @return The class handler or null if not found
     */
    public BaseSkillEffectsHandler getHandlerForPlayer(Player player) {
        if (player == null) {
            return null;
        }

        UUID playerId = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(playerId);

        // Normalize class name
        String normalizedClassName = ClassNameNormalizer.normalize(playerClass);

        return classHandlers.get(normalizedClassName);
    }

    /**
     * Apply active skill effects to a player
     * This is called periodically and after player actions
     * @param player The player to apply effects to
     */
    public void applyActiveSkillEffects(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply regeneration effect if needed
        if (stats.hasRegenerationEffect()) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    60, // 3 seconds
                    0,  // Level 1
                    false, // No particles
                    false, // No icon
                    true   // Show particles
            ));
        }

        // Let class-specific handler apply its effects
        BaseSkillEffectsHandler handler = getHandlerForPlayer(player);
        if (handler != null) {
            // For handlers with periodic checks
            if (handler instanceof RangerSkillEffectsHandler) {
                ((RangerSkillEffectsHandler) handler).checkWindStacksEffects(player, stats);
            }

            // Add more class-specific periodic effects as needed
        }
    }
}
