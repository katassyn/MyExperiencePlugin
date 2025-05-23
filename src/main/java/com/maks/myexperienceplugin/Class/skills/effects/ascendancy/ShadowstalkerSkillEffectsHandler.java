package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowstalkerSkillEffectsHandler extends BaseSkillEffectsHandler implements Listener {
    // ID offset for Shadowstalker skills
    private static final int ID_OFFSET = 110000;

    // Random for critical hit and poison chance calculations
    private final Random random = new Random();

    // Track poisoned entities and their poisoners
    private final Map<UUID, Map<UUID, Long>> poisonedEntities = new ConcurrentHashMap<>();

    // Track bleeding entities and their attackers
    private final Map<UUID, Map<UUID, Long>> bleedingEntities = new ConcurrentHashMap<>();

    // Track last attack time for each player against each entity
    private final Map<UUID, Map<UUID, Long>> lastAttackTime = new ConcurrentHashMap<>();

    // Track attack count for precision strike (every third attack is a crit)
    private final Map<UUID, Map<UUID, Integer>> attackCounter = new ConcurrentHashMap<>();

    // Track sneaking start time for patient hunter
    private final Map<UUID, Long> sneakingStartTime = new ConcurrentHashMap<>();

    // Track critical defense stacks
    private final Map<UUID, Integer> critDefenseStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> critDefenseExpiry = new ConcurrentHashMap<>();

    // Track ambush cooldown
    private final Map<UUID, Long> lastAttackAnyTarget = new ConcurrentHashMap<>();

    // Track kill rush speed boost
    private final Map<UUID, Long> killRushExpiry = new ConcurrentHashMap<>();

    // Track ambush speed boost
    private final Map<UUID, Long> ambushSpeedExpiry = new ConcurrentHashMap<>();

    public ShadowstalkerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
        // Register this class as a listener for events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Start periodic task to check and remove expired effects
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupExpiredEffects, 20L, 20L);
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        int originalId = skillId - ID_OFFSET; // Remove offset to get original skill ID

        switch (originalId) {
            case 1: // +5% movement speed in shadows and at night (1/2)
                // This is handled dynamically in the periodic task
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 1: Will add 5% movement speed in shadows and at night");
                }
                break;
            case 2: // +2% critical hit chance (1/3)
                // We'll handle this in the damage calculation since PlayerSkillStats doesn't have a critical chance property
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 2: Will add " + (2 * purchaseCount) + "% critical chance during combat");
                }
                break;
            case 3: // +15% damage on first attack against target (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 3: Will add 15% damage on first attack against target");
                }
                break;
            case 4: // +4% evade chance while sneaking
                // This is handled dynamically based on player state
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 4: Will add 4% evade chance while sneaking");
                }
                break;
            case 5: // Critical hits apply 3% of your damage as bleeding for 5 seconds (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 5: Will apply bleeding on critical hits");
                }
                break;
            case 6: // +3% evade chance in darkness (1/2)
                // This is handled dynamically based on light level
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 6: Will add 3% evade chance in darkness");
                }
                break;
            case 7: // -10% enemy detection range while sneaking
                // This is handled by modifying entity targeting
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 7: Will reduce enemy detection range while sneaking");
                }
                break;
            case 8: // +10% damage when attacking from behind (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 8: Will add 10% damage when attacking from behind");
                }
                break;
            case 9: // +15% chance for attacks to apply poison for 4 seconds (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 9: Will add chance to apply poison");
                }
                break;
            case 10: // After killing an enemy, gain +10% movement speed for 5 seconds
                // This is handled in the entity death event
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 10: Will add movement speed after kills");
                }
                break;
            case 11: // +20% critical damage (1/2)
                // We'll handle this in the damage calculation since PlayerSkillStats doesn't have a critical damage multiplier property
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 11: Will add " + (20 * purchaseCount) + "% critical damage during combat");
                }
                break;
            case 12: // When below 30% health, gain +15% evade chance
                // This is handled dynamically based on player health
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 12: Will add evade chance when low health");
                }
                break;
            case 13: // +3% damage per second spent sneaking before attacking (max +15%)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 13: Will add damage based on sneaking time");
                }
                break;
            case 14: // +12% damage against enemies affected by poison or bleeding (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 14: Will add damage against poisoned/bleeding enemies");
                }
                break;
            case 15: // +20% poison damage and duration (1/2)
                // This is handled when applying poison effects
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 15: Will enhance poison effects");
                }
                break;
            case 16: // +25% movement speed while sneaking (1/2)
                // This is handled dynamically based on player state
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 16: Will add movement speed while sneaking");
                }
                break;
            case 17: // Critical hits have 10% chance to deal +50% additional damage (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 17: Will add chance for devastating crits");
                }
                break;
            case 18: // Each critical hit reduces damage taken by 3% for 4 seconds (stacks up to 5 times)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 18: Will add defense on critical hits");
                }
                break;
            case 19: // After 3 seconds of not attacking, your next attack deals +35% damage
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 19: Will add damage after not attacking");
                }
                break;
            case 20: // Attacks against full-health targets have +15% critical chance (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 20: Will add crit chance against full health targets");
                }
                break;
            case 21: // Critical hits against poisoned targets have 25% chance to amplify poison effect, dealing double damage (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 21: Will add chance to amplify poison");
                }
                break;
            case 22: // +10% damage reduction in darkness (1/2)
                // This is handled dynamically based on light level
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 22: Will add damage reduction in darkness");
                }
                break;
            case 23: // Every third attack on the same target is automatically a critical hit
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 23: Will make every third attack a crit");
                }
                break;
            case 24: // +1 maximum wind stack and +5% evade chance at max stacks (1/2)
                // This is handled by modifying the player's wind stacks
                stats.setMaxWindStacks(stats.getMaxWindStacks() + purchaseCount);
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 24: Added " + purchaseCount + " max wind stacks");
                }
                break;
            case 25: // After attacking from a sneaking position, gain +40% attack speed for 3 seconds
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 25: Will add attack speed after ambush");
                }
                break;
            case 26: // After applying poison, gain +5% damage against that target for the duration (1/2)
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 26: Will add damage against poisoned targets");
                }
                break;
            case 27: // While at full health, your attacks ignore 25% of enemy armor
                // This is handled dynamically during combat
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 27: Will ignore armor when at full health");
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Skill 12: When below 30% health, gain +15% evade chance
        if (isPurchased(playerId, ID_OFFSET + 12)) {
            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;
            if (healthPercent < 30) {
                // Add evade chance
                stats.addEvadeChance(15);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added 15% evade chance from Desperate Evasion");
                }
            }
        }

        // Skill 18: Critical Defense - damage reduction based on stacks
        if (isPurchased(playerId, ID_OFFSET + 18) && critDefenseStacks.containsKey(playerId)) {
            int stacks = critDefenseStacks.get(playerId);
            if (stacks > 0) {
                double reduction = stacks * 0.03; // 3% per stack
                stats.addDefenseBonus(reduction * 100); // Convert to percentage

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added " + (reduction * 100) + "% defense from Critical Defense");
                }
            }
        }

        // Skill 22: +10% damage reduction in darkness
        if (isPurchased(playerId, ID_OFFSET + 22) && isInDarkness(player)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 22);
            double reduction = 0.1 * purchaseCount;
            stats.addDefenseBonus(reduction * 100); // Convert to percentage

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (reduction * 100) + "% defense from Shadow Protection");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getEntity();

        // Handle player taking damage
        if (entity instanceof Player) {
            Player player = (Player) entity;
            UUID playerId = player.getUniqueId();
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

            if (!"Shadowstalker".equals(ascendancy)) return;

            // Skill 12: When below 30% health, gain +15% evade chance
            if (isPurchased(playerId, ID_OFFSET + 12)) {
                double healthPercent = player.getHealth() / player.getMaxHealth() * 100;
                if (healthPercent < 30) {
                    // Check for evasion
                    if (random.nextDouble() * 100 < 15) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.GREEN + "You evaded the attack due to Desperate Evasion!");
                        return;
                    }
                }
            }

            // Skill 18: Critical Defense - damage reduction based on stacks
            if (isPurchased(playerId, ID_OFFSET + 18) && critDefenseStacks.containsKey(playerId)) {
                int stacks = critDefenseStacks.get(playerId);
                if (stacks > 0) {
                    double reduction = stacks * 0.03; // 3% per stack
                    double newDamage = event.getDamage() * (1 - reduction);
                    event.setDamage(newDamage);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Applied " + (reduction * 100) + "% damage reduction from Critical Defense");
                    }
                }
            }

            // Skill 22: +10% damage reduction in darkness
            if (isPurchased(playerId, ID_OFFSET + 22)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 22);
                if (isInDarkness(player)) {
                    double reduction = 0.1 * purchaseCount;
                    double newDamage = event.getDamage() * (1 - reduction);
                    event.setDamage(newDamage);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Applied " + (reduction * 100) + "% damage reduction from Shadow Protection");
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity target = (LivingEntity) event.getEntity();
        UUID targetId = target.getUniqueId();

        // Track attack for various skills
        updateAttackTracking(playerId, targetId);

        // Calculate base damage
        double baseDamage = event.getDamage();
        double damageBonus = 0;
        double damageMultiplier = 1.0;

        // Skill 2: +2% critical hit chance (1/3)
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 2);
            // We'll add this to the player's base critical chance in the event handler
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (2 * purchaseCount) + "% critical chance from Critical Precision");
            }
        }

        // Skill 3: +15% damage on first attack against target
        if (isPurchased(playerId, ID_OFFSET + 3)) {
            Map<UUID, Long> attacks = lastAttackTime.get(playerId);
            if (attacks != null) {
                Long lastAttack = attacks.get(targetId);
                if (lastAttack == null || System.currentTimeMillis() - lastAttack > 10000) { // No attack in last 10 seconds
                    int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 3);
                    damageMultiplier *= (1 + 0.15 * purchaseCount);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Added " + (15 * purchaseCount) + "% damage from First Strike");
                    }
                }
            }
        }

        // Skill 8: +10% damage when attacking from behind
        if (isPurchased(playerId, ID_OFFSET + 8) && isAttackingFromBehind(player, target)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 8);
            damageMultiplier *= (1 + 0.1 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (10 * purchaseCount) + "% damage from Backstab");
            }
        }

        // Skill 13: +3% damage per second spent sneaking before attacking (max +15%)
        if (isPurchased(playerId, ID_OFFSET + 13) && sneakingStartTime.containsKey(playerId)) {
            long sneakTime = System.currentTimeMillis() - sneakingStartTime.get(playerId);
            if (sneakTime > 0) {
                double seconds = sneakTime / 1000.0;
                double bonus = Math.min(seconds * 0.03, 0.15); // 3% per second, max 15%
                damageMultiplier *= (1 + bonus);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added " + (bonus * 100) + "% damage from Patient Hunter");
                }
            }
            // Reset sneaking time after attack
            sneakingStartTime.remove(playerId);
        }

        // Skill 14: +12% damage against enemies affected by poison or bleeding
        if (isPurchased(playerId, ID_OFFSET + 14) && 
            (isEntityPoisoned(targetId, playerId) || isEntityBleeding(targetId, playerId))) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 14);
            damageMultiplier *= (1 + 0.12 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (12 * purchaseCount) + "% damage from Toxic Exploitation");
            }
        }

        // Skill 19: After 3 seconds of not attacking, your next attack deals +35% damage
        if (isPurchased(playerId, ID_OFFSET + 19) && lastAttackAnyTarget.containsKey(playerId)) {
            long timeSinceLastAttack = System.currentTimeMillis() - lastAttackAnyTarget.get(playerId);
            if (timeSinceLastAttack > 3000) { // 3 seconds
                damageMultiplier *= 1.35;

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added 35% damage from Ambush");
                }
            }
        }

        // Skill 26: After applying poison, gain +5% damage against that target for the duration
        if (isPurchased(playerId, ID_OFFSET + 26) && isEntityPoisoned(targetId, playerId)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 26);
            damageMultiplier *= (1 + 0.05 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (5 * purchaseCount) + "% damage from Toxic Focus");
            }
        }

        // Skill 27: While at full health, your attacks ignore 25% of enemy armor
        if (isPurchased(playerId, ID_OFFSET + 27) && player.getHealth() >= player.getMaxHealth()) {
            // Simulate armor penetration by increasing damage
            damageMultiplier *= 1.25;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Applied armor penetration from Armor Penetration");
            }
        }

        // Apply damage bonuses to stats
        stats.addBonusDamage(damageBonus);
        stats.addDamageMultiplier(damageMultiplier - 1.0); // Convert to bonus

        // Skill 9: Chance to apply poison
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 9);
            if (random.nextDouble() * 100 < 15 * purchaseCount) {
                // We'll apply poison in the event handler
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Will apply poison from Poisoned Blade");
                }
            }
        }

        // Skill 25: After attacking from a sneaking position, gain +40% attack speed for 3 seconds
        if (isPurchased(playerId, ID_OFFSET + 25) && player.isSneaking()) {
            // We'll apply attack speed in the event handler
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Will apply attack speed boost from Ambush Speed");
            }
        }

        // Update last attack time for any target
        lastAttackAnyTarget.put(playerId, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        LivingEntity target = (LivingEntity) event.getEntity();
        UUID targetId = target.getUniqueId();
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

        if (!"Shadowstalker".equals(ascendancy)) return;

        // Get player stats
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        // Track attack for various skills
        updateAttackTracking(playerId, targetId);

        // Calculate base damage
        double baseDamage = event.getDamage();
        double finalDamage = baseDamage;
        boolean isCritical = false;

        // Check for critical hit
        double critChance = 5.0; // Base critical chance

        // Skill 20: Attacks against full-health targets have +15% critical chance
        if (isPurchased(playerId, ID_OFFSET + 20) && target.getHealth() >= target.getMaxHealth()) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 20);
            critChance += 15 * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (15 * purchaseCount) + "% crit chance from Opening Strike");
            }
        }

        // Skill 23: Every third attack on the same target is automatically a critical hit
        if (isPurchased(playerId, ID_OFFSET + 23)) {
            Map<UUID, Integer> counters = attackCounter.computeIfAbsent(playerId, k -> new HashMap<>());
            int count = counters.getOrDefault(targetId, 0);

            if (count >= 2) { // Third attack (0-indexed)
                isCritical = true;
                counters.put(targetId, 0); // Reset counter

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Guaranteed critical hit from Precision Strike");
                }
            }
        }

        // Normal critical hit check if not already critical
        if (!isCritical && random.nextDouble() * 100 < critChance) {
            isCritical = true;
        }

        // Apply critical hit damage
        if (isCritical) {
            // Base critical multiplier is 2.0 (200% damage)
            double critMultiplier = 2.0;

            // Skill 11: +20% critical damage (1/2)
            if (isPurchased(playerId, ID_OFFSET + 11)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 11);
                critMultiplier += 0.2 * purchaseCount; // +20% per purchase
            }

            finalDamage *= critMultiplier;

            // Skill 17: Critical hits have 10% chance to deal +50% additional damage
            if (isPurchased(playerId, ID_OFFSET + 17)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 17);
                if (random.nextDouble() * 100 < 10 * purchaseCount) {
                    finalDamage *= 1.5;
                    player.sendMessage(ChatColor.RED + "Devastating Critical Hit!");

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Applied devastating critical from Devastating Critical");
                    }
                }
            }

            // Skill 5: Critical hits apply bleeding
            if (isPurchased(playerId, ID_OFFSET + 5)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 5);
                applyBleeding(player, target, baseDamage * 0.03 * purchaseCount, 5);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Applied bleeding from Critical Bleeding");
                }
            }

            // Skill 18: Each critical hit reduces damage taken
            if (isPurchased(playerId, ID_OFFSET + 18)) {
                int currentStacks = critDefenseStacks.getOrDefault(playerId, 0);
                if (currentStacks < 5) { // Max 5 stacks
                    critDefenseStacks.put(playerId, currentStacks + 1);
                }
                critDefenseExpiry.put(playerId, System.currentTimeMillis() + 4000); // 4 seconds

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added Critical Defense stack, now at " + (currentStacks + 1));
                }
            }

            // Skill 21: Critical hits against poisoned targets have chance to amplify poison
            if (isPurchased(playerId, ID_OFFSET + 21) && isEntityPoisoned(targetId, playerId)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 21);
                if (random.nextDouble() * 100 < 25 * purchaseCount) {
                    amplifyPoison(player, target);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Amplified poison from Toxic Amplification");
                    }
                }
            }
        }

        // Skill 3: +15% damage on first attack against target
        if (isPurchased(playerId, ID_OFFSET + 3)) {
            Map<UUID, Long> attacks = lastAttackTime.get(playerId);
            if (attacks != null) {
                Long lastAttack = attacks.get(targetId);
                if (lastAttack == null || System.currentTimeMillis() - lastAttack > 10000) { // No attack in last 10 seconds
                    int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 3);
                    finalDamage *= (1 + 0.15 * purchaseCount);

                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: Added " + (15 * purchaseCount) + "% damage from First Strike");
                    }
                }
            }
        }

        // Skill 8: +10% damage when attacking from behind
        if (isPurchased(playerId, ID_OFFSET + 8) && isAttackingFromBehind(player, target)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 8);
            finalDamage *= (1 + 0.1 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (10 * purchaseCount) + "% damage from Backstab");
            }
        }

        // Skill 13: +3% damage per second spent sneaking before attacking (max +15%)
        if (isPurchased(playerId, ID_OFFSET + 13) && sneakingStartTime.containsKey(playerId)) {
            long sneakTime = System.currentTimeMillis() - sneakingStartTime.get(playerId);
            if (sneakTime > 0) {
                double seconds = sneakTime / 1000.0;
                double bonus = Math.min(seconds * 0.03, 0.15); // 3% per second, max 15%
                finalDamage *= (1 + bonus);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added " + (bonus * 100) + "% damage from Patient Hunter");
                }
            }
            // Reset sneaking time after attack
            sneakingStartTime.remove(playerId);
        }

        // Skill 14: +12% damage against enemies affected by poison or bleeding
        if (isPurchased(playerId, ID_OFFSET + 14) && 
            (isEntityPoisoned(targetId, playerId) || isEntityBleeding(targetId, playerId))) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 14);
            finalDamage *= (1 + 0.12 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (12 * purchaseCount) + "% damage from Toxic Exploitation");
            }
        }

        // Skill 19: After 3 seconds of not attacking, your next attack deals +35% damage
        if (isPurchased(playerId, ID_OFFSET + 19) && lastAttackAnyTarget.containsKey(playerId)) {
            long timeSinceLastAttack = System.currentTimeMillis() - lastAttackAnyTarget.get(playerId);
            if (timeSinceLastAttack > 3000) { // 3 seconds
                finalDamage *= 1.35;

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added 35% damage from Ambush");
                }
            }
        }

        // Skill 25: After attacking from a sneaking position, gain +40% attack speed for 3 seconds
        if (isPurchased(playerId, ID_OFFSET + 25) && player.isSneaking()) {
            ambushSpeedExpiry.put(playerId, System.currentTimeMillis() + 3000); // 3 seconds
            player.sendMessage(ChatColor.GREEN + "Ambush Speed activated!");

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Applied attack speed boost from Ambush Speed");
            }
        }

        // Skill 26: After applying poison, gain +5% damage against that target for the duration
        if (isPurchased(playerId, ID_OFFSET + 26) && isEntityPoisoned(targetId, playerId)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 26);
            finalDamage *= (1 + 0.05 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (5 * purchaseCount) + "% damage from Toxic Focus");
            }
        }

        // Skill 27: While at full health, your attacks ignore 25% of enemy armor
        if (isPurchased(playerId, ID_OFFSET + 27) && player.getHealth() >= player.getMaxHealth()) {
            // Simulate armor penetration by increasing damage
            // This is a simplification since we can't directly modify armor values
            finalDamage *= 1.25;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Applied armor penetration from Armor Penetration");
            }
        }

        // Apply final damage
        event.setDamage(finalDamage);

        // Skill 9: Chance to apply poison
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 9);
            if (random.nextDouble() * 100 < 15 * purchaseCount) {
                applyPoison(player, target);

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Applied poison from Poisoned Blade");
                }
            }
        }

        // Update last attack time for any target
        lastAttackAnyTarget.put(playerId, System.currentTimeMillis());
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Skill 10: After killing an enemy, gain +10% movement speed for 5 seconds
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            killRushExpiry.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds

            // Apply movement speed bonus
            stats.addMovementSpeedBonus(10);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added 10% movement speed from Kill Rush");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        UUID playerId = killer.getUniqueId();
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

        if (!"Shadowstalker".equals(ascendancy)) return;

        // Skill 10: After killing an enemy, gain +10% movement speed for 5 seconds
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            killRushExpiry.put(playerId, System.currentTimeMillis() + 5000); // 5 seconds
            killer.sendMessage(ChatColor.GREEN + "Kill Rush activated!");

            // Apply speed effect
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, false, false, true));

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Applied speed boost from Kill Rush");
            }
        }

        // Clean up tracking for the dead entity
        UUID entityId = entity.getUniqueId();
        cleanupEntityTracking(entityId);
    }

    /**
     * Public method to apply periodic effects
     * Called by AscendancySkillEffectIntegrator
     */
    public void applyPeriodicEffects() {
        cleanupExpiredEffects();
    }

    /**
     * Periodic task to apply effects and clean up expired effects
     */
    private void cleanupExpiredEffects() {
        long currentTime = System.currentTimeMillis();

        // Clean up expired poison effects
        for (Map.Entry<UUID, Map<UUID, Long>> entry : poisonedEntities.entrySet()) {
            UUID entityId = entry.getKey();
            Map<UUID, Long> poisoners = entry.getValue();

            poisoners.entrySet().removeIf(poisoner -> currentTime > poisoner.getValue());

            if (poisoners.isEmpty()) {
                poisonedEntities.remove(entityId);
            }
        }

        // Clean up expired bleeding effects
        for (Map.Entry<UUID, Map<UUID, Long>> entry : bleedingEntities.entrySet()) {
            UUID entityId = entry.getKey();
            Map<UUID, Long> bleeders = entry.getValue();

            bleeders.entrySet().removeIf(bleeder -> currentTime > bleeder.getValue());

            if (bleeders.isEmpty()) {
                bleedingEntities.remove(entityId);
            }
        }

        // Clean up expired critical defense stacks
        critDefenseExpiry.entrySet().removeIf(entry -> {
            if (currentTime > entry.getValue()) {
                critDefenseStacks.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Apply dynamic effects for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            String ascendancy = plugin.getClassManager().getPlayerAscendancy(playerId);

            if (!"Shadowstalker".equals(ascendancy)) continue;

            // Update sneaking time tracking
            if (player.isSneaking()) {
                if (!sneakingStartTime.containsKey(playerId)) {
                    sneakingStartTime.put(playerId, currentTime);
                }
            } else {
                sneakingStartTime.remove(playerId);
            }

            // Apply movement speed bonuses
            applyMovementSpeedBonuses(player);

            // Apply evade chance bonuses
            applyEvadeChanceBonuses(player);
        }
    }

    /**
     * Apply movement speed bonuses based on player state and skills
     */
    private void applyMovementSpeedBonuses(Player player) {
        UUID playerId = player.getUniqueId();
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        double speedBonus = 0;

        // Skill 1: +5% movement speed in shadows and at night
        if (isPurchased(playerId, ID_OFFSET + 1) && (isInDarkness(player) || isNightTime(player.getWorld()))) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 1);
            speedBonus += 0.05 * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (5 * purchaseCount) + "% movement speed from Shadow Speed");
            }
        }

        // Skill 16: +25% movement speed while sneaking
        if (isPurchased(playerId, ID_OFFSET + 16) && player.isSneaking()) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 16);
            speedBonus += 0.25 * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (25 * purchaseCount) + "% movement speed from Shadow Step");
            }
        }

        // Skill 10: Kill Rush speed boost
        if (killRushExpiry.containsKey(playerId) && System.currentTimeMillis() < killRushExpiry.get(playerId)) {
            speedBonus += 0.1;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added 10% movement speed from Kill Rush");
            }
        } else {
            killRushExpiry.remove(playerId);
        }

        // Apply the speed bonus
        if (speedBonus > 0) {
            // Convert from multiplier (0.1) to percentage (10)
            stats.addMovementSpeedBonus(speedBonus * 100);

            // Visual feedback
            ActionBarUtils.sendActionBar(player, ChatColor.GREEN + "Speed Bonus: +" + (int)(speedBonus * 100) + "%");
        }
    }

    /**
     * Apply evade chance bonuses based on player state and skills
     */
    private void applyEvadeChanceBonuses(Player player) {
        UUID playerId = player.getUniqueId();
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
        double evadeBonus = 0;

        // Skill 4: +4% evade chance while sneaking
        if (isPurchased(playerId, ID_OFFSET + 4) && player.isSneaking()) {
            evadeBonus += 4;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added 4% evade chance from Sneaking Evasion");
            }
        }

        // Skill 6: +3% evade chance in darkness
        if (isPurchased(playerId, ID_OFFSET + 6) && isInDarkness(player)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 6);
            evadeBonus += 3 * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (3 * purchaseCount) + "% evade chance from Shadow Evasion");
            }
        }

        // Skill 12: +15% evade chance when below 30% health
        if (isPurchased(playerId, ID_OFFSET + 12)) {
            double healthPercent = player.getHealth() / player.getMaxHealth() * 100;
            if (healthPercent < 30) {
                evadeBonus += 15;

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Added 15% evade chance from Desperate Evasion");
                }
            }
        }

        // Skill 24: +5% evade chance at max wind stacks
        if (isPurchased(playerId, ID_OFFSET + 24) && stats.getWindStacks() >= stats.getMaxWindStacks()) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 24);
            evadeBonus += 5 * purchaseCount;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Added " + (5 * purchaseCount) + "% evade chance from Wind Mastery");
            }
        }

        // Apply the evade bonus
        if (evadeBonus > 0) {
            stats.addEvadeChance(evadeBonus);
        }
    }

    /**
     * Apply poison to a target
     */
    private void applyPoison(Player attacker, LivingEntity target) {
        UUID attackerId = attacker.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Base duration: 4 seconds
        int duration = 4;

        // Skill 15: +20% poison duration
        if (isPurchased(attackerId, ID_OFFSET + 15)) {
            int purchaseCount = getSkillPurchaseCount(attackerId, ID_OFFSET + 15);
            duration += duration * 0.2 * purchaseCount;
        }

        // Track the poisoned entity
        Map<UUID, Long> poisoners = poisonedEntities.computeIfAbsent(targetId, k -> new ConcurrentHashMap<>());
        poisoners.put(attackerId, System.currentTimeMillis() + (duration * 1000));

        // Apply visual poison effect
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, 0, false, true, true));

        // Notify attacker
        attacker.sendMessage(ChatColor.GREEN + "You poisoned the target!");
    }

    /**
     * Amplify poison effect on a target
     */
    private void amplifyPoison(Player attacker, LivingEntity target) {
        UUID targetId = target.getUniqueId();

        // Check if target is poisoned
        if (!isEntityPoisoned(targetId, attacker.getUniqueId())) return;

        // Apply stronger poison effect
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1, false, true, true));

        // Notify attacker
        attacker.sendMessage(ChatColor.GREEN + "You amplified the poison effect!");
    }

    /**
     * Apply bleeding to a target
     */
    private void applyBleeding(Player attacker, LivingEntity target, double damagePerSecond, int duration) {
        UUID attackerId = attacker.getUniqueId();
        UUID targetId = target.getUniqueId();

        // Track the bleeding entity
        Map<UUID, Long> bleeders = bleedingEntities.computeIfAbsent(targetId, k -> new ConcurrentHashMap<>());
        bleeders.put(attackerId, System.currentTimeMillis() + (duration * 1000));

        // Apply visual effect
        target.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, duration * 20, 0, false, false, true));

        // Schedule bleeding damage
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (target.isDead() || !target.isValid()) {
                return;
            }

            // Check if still bleeding
            if (!isEntityBleeding(targetId, attackerId)) {
                return;
            }

            // Apply damage
            target.damage(damagePerSecond, attacker);
        }, 20L, 20L); // Every second

        // Schedule task cancellation
        Bukkit.getScheduler().runTaskLater(plugin, task::cancel, duration * 20L);

        // Notify attacker
        attacker.sendMessage(ChatColor.RED + "Your target is bleeding!");
    }

    /**
     * Update attack tracking for various skills
     */
    private void updateAttackTracking(UUID playerId, UUID targetId) {
        // Track last attack time for first strike
        Map<UUID, Long> attacks = lastAttackTime.computeIfAbsent(playerId, k -> new HashMap<>());
        attacks.put(targetId, System.currentTimeMillis());

        // Track attack count for precision strike
        if (isPurchased(playerId, ID_OFFSET + 23)) {
            Map<UUID, Integer> counters = attackCounter.computeIfAbsent(playerId, k -> new HashMap<>());
            int count = counters.getOrDefault(targetId, 0);
            counters.put(targetId, count + 1);
        }
    }

    /**
     * Clean up tracking for an entity
     */
    private void cleanupEntityTracking(UUID entityId) {
        poisonedEntities.remove(entityId);
        bleedingEntities.remove(entityId);

        // Clean up player tracking of this entity
        for (Map<UUID, Long> attacks : lastAttackTime.values()) {
            attacks.remove(entityId);
        }

        for (Map<UUID, Integer> counters : attackCounter.values()) {
            counters.remove(entityId);
        }
    }

    /**
     * Check if an entity is poisoned by a specific player
     */
    private boolean isEntityPoisoned(UUID entityId, UUID playerId) {
        Map<UUID, Long> poisoners = poisonedEntities.get(entityId);
        if (poisoners == null) return false;

        Long expiry = poisoners.get(playerId);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    /**
     * Check if an entity is bleeding from a specific player's attack
     */
    private boolean isEntityBleeding(UUID entityId, UUID playerId) {
        Map<UUID, Long> bleeders = bleedingEntities.get(entityId);
        if (bleeders == null) return false;

        Long expiry = bleeders.get(playerId);
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    /**
     * Check if a player is attacking from behind a target
     */
    private boolean isAttackingFromBehind(Player attacker, LivingEntity target) {
        // Get the direction the target is facing
        float targetYaw = target.getLocation().getYaw();

        // Get the direction from target to attacker
        Location targetLoc = target.getLocation();
        Location attackerLoc = attacker.getLocation();
        double dx = attackerLoc.getX() - targetLoc.getX();
        double dz = attackerLoc.getZ() - targetLoc.getZ();
        float attackerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        // Normalize yaw values
        while (targetYaw < 0) targetYaw += 360;
        while (attackerYaw < 0) attackerYaw += 360;

        // Calculate the angle difference
        float angleDiff = Math.abs(targetYaw - attackerYaw);
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        // If the angle difference is less than 60 degrees, attacker is behind target
        return angleDiff < 60;
    }

    /**
     * Check if a player is in darkness (light level < 8)
     */
    private boolean isInDarkness(Player player) {
        return player.getLocation().getBlock().getLightLevel() < 8;
    }

    /**
     * Check if it's night time in the player's world
     */
    private boolean isNightTime(World world) {
        long time = world.getTime();
        return time > 13000 && time < 23000;
    }

    /**
     * Check if a player has purchased a specific skill
     */
    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    /**
     * Get the purchase count for a specific skill
     */
    private int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }
}
