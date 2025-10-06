package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import com.maks.myexperienceplugin.utils.DebugUtils;
import com.maks.myexperienceplugin.utils.ChatNotificationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowstalkerSkillEffectsHandler extends BaseSkillEffectsHandler {

    private static final int ID_OFFSET = 500000;
    
    // Debug flag - set to 1 to enable debug messages
    private final int debuggingFlag = 0;

    // === TRACKING MAPS ===
    // Track pierwszego ataku na cel
    private final Map<UUID, Set<UUID>> firstAttackTracker = new ConcurrentHashMap<>();

    // Track czasu rozpoczęcia sneakingu
    private final Map<UUID, Long> sneakingStartTime = new ConcurrentHashMap<>();

    // Track ostatniego ataku (dla skill 19 - Ambush)
    private final Map<UUID, Long> lastAttackTime = new ConcurrentHashMap<>();

    // Track ilości ataków na cel (dla skill 23 - Precision Strike)
    private final Map<UUID, Map<UUID, Integer>> attackCountOnTarget = new ConcurrentHashMap<>();

    // Track stacków Critical Defense (skill 18)
    private final Map<UUID, Integer> criticalDefenseStacks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> criticalDefenseExpiry = new ConcurrentHashMap<>();

    // Track poison i bleeding efektów
    private final Map<UUID, Map<UUID, PoisonData>> poisonedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, BleedData>> bleedingEntities = new ConcurrentHashMap<>();

    // Track Assassin's Haste (skill 10)
    private final Map<UUID, Long> assassinHasteExpiry = new ConcurrentHashMap<>();

    // Track Ambush Speed (skill 25)
    private final Map<UUID, Long> ambushSpeedExpiry = new ConcurrentHashMap<>();

    // Klasy pomocnicze dla efektów czasowych
    private static class PoisonData {
        final long expiry;
        final double damagePerSecond;
        final UUID sourcePlayerId;
        boolean amplified = false;

        PoisonData(long expiry, double damagePerSecond, UUID sourcePlayerId) {
            this.expiry = expiry;
            this.damagePerSecond = damagePerSecond;
            this.sourcePlayerId = sourcePlayerId;
        }
    }

    private static class BleedData {
        final long expiry;
        final double damagePerSecond;
        final UUID sourcePlayerId;

        BleedData(long expiry, double damagePerSecond, UUID sourcePlayerId) {
            this.expiry = expiry;
            this.damagePerSecond = damagePerSecond;
            this.sourcePlayerId = sourcePlayerId;
        }
    }

    public ShadowstalkerSkillEffectsHandler(MyExperiencePlugin plugin) {
        super(plugin);
        startPeriodicTasks();
    }

    @Override
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount, Player player) {
        int originalId = skillId - ID_OFFSET;
        
        // Większość efektów Shadowstalkera jest dynamiczna
        // Statyczne efekty są już obsługiwane w ShadowstalkerSkillManager.applySkillStats()

        switch (originalId) {
            case 1: // +5% movement speed in shadows and at night
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 1: Will apply shadow movement speed dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL 1: +5% movement speed in shadows/night enabled");
                }
                break;
            case 2: // +2% critical hit chance
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 2: Will apply critical hit chance dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL 2: +2% critical hit chance enabled");
                }
                break;
            case 3: // +15% damage on first attack against target
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 3: Will apply first strike damage dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL 3: +15% damage on first attack enabled");
                }
                break;
            case 4: // +4% evade chance while sneaking
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 4: Will apply sneak evade chance dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL 4: +4% evade chance while sneaking enabled");
                }
                break;
            case 5: // Critical hits apply 3% of your damage as bleeding for 5 seconds
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER SKILL 5: Will apply critical bleeding dynamically");
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL 5: Critical hits apply bleeding enabled");
                }
                break;
            default:
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Applying skill effects for skill " + originalId + " with purchase count " + purchaseCount);
                    ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "[DEBUG] SHADOWSTALKER SKILL " + originalId + ": Dynamic effect enabled");
                }
                break;
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        double damage = event.getDamage();

        // Skill 4: +4% evade chance while sneaking
        if (isPurchased(playerId, ID_OFFSET + 4) && player.isSneaking()) {
            double evadeChance = stats.getEvadeChance() + 4;
            boolean success = rollChance(evadeChance, player, "Sneak Evade");
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info(String.format(
                    "[SHADOWSTALKER] %s - Sneak Evade: %.1f%% | Result: %s", 
                    player.getName(),
                    evadeChance,
                    success ? "SUCCESS ✓" : "FAILED ✗"
                ));
            }
            
            if (success) {
                event.setCancelled(true);
                ChatNotificationUtils.send(player, ChatColor.GRAY + "Evaded!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Evaded while sneaking!");
                }
                return;
            }
        }

        // Skill 6: +3% evade chance in darkness (1/2)
        if (isPurchased(playerId, ID_OFFSET + 6) && isInDarkness(player)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 6);
            double evadeChance = stats.getEvadeChance() + (3 * purchaseCount);
            boolean success = rollChance(evadeChance, player, "Shadow Evade");
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info(String.format(
                    "[SHADOWSTALKER] %s - Shadow Evade: %.1f%% | Result: %s", 
                    player.getName(),
                    evadeChance,
                    success ? "SUCCESS ✓" : "FAILED ✗"
                ));
            }
            
            if (success) {
                event.setCancelled(true);
                ChatNotificationUtils.send(player, ChatColor.DARK_GRAY + "Shadow evade!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Shadow evaded in darkness!");
                }
                return;
            }
        }

        // Skill 12: When below 30% health, gain +15% evade chance
        if (isPurchased(playerId, ID_OFFSET + 12) && player.getHealth() < (player.getMaxHealth() * 0.3)) {
            double evadeChance = stats.getEvadeChance() + 15;
            boolean success = rollChance(evadeChance, player, "Desperate Evade");
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info(String.format(
                    "[SHADOWSTALKER] %s - Desperate Evade: %.1f%% | Result: %s", 
                    player.getName(),
                    evadeChance,
                    success ? "SUCCESS ✓" : "FAILED ✗"
                ));
            }
            
            if (success) {
                event.setCancelled(true);
                ChatNotificationUtils.send(player, ChatColor.RED + "Desperate evade!");
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Desperate evaded at low health!");
                }
                return;
            }
        }

        // Skill 18: Critical Defense stacks reduction
        if (criticalDefenseStacks.containsKey(playerId)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < criticalDefenseExpiry.getOrDefault(playerId, 0L)) {
                int stacks = criticalDefenseStacks.get(playerId);
                double reduction = 0.03 * stacks; // 3% per stack
                event.setDamage(damage * (1 - reduction));

                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Reduced damage by " + 
                            (reduction * 100) + "% from Critical Defense stacks");
                }
            } else {
                criticalDefenseStacks.remove(playerId);
                criticalDefenseExpiry.remove(playerId);
            }
        }

        // Skill 22: +10% damage reduction in darkness (1/2)
        if (isPurchased(playerId, ID_OFFSET + 22) && isInDarkness(player)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 22);
            double reduction = 0.1 * purchaseCount;
            event.setDamage(damage * (1 - reduction));

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Shadow Protection reduced damage by " + 
                        (reduction * 100) + "%");
            }
        }
    }

    @Override
    public void handleEntityDamageByEntity(EntityDamageByEntityEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity target = (LivingEntity) event.getEntity();
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        double baseDamage = event.getDamage();
        double finalDamage = baseDamage;
        boolean isCritical = false;

        // Get the previous attack time for Ambush skill check (Skill 19)
        Long previousAttackTime = lastAttackTime.get(playerId);

        // === MOVEMENT SPEED EFFECTS ===
        applyMovementSpeedEffects(player, stats);

        // === CRITICAL HIT CALCULATION ===
        double critChance = stats.getCriticalChance();

        // Skill 2: +2% critical hit chance (1/3)
        if (isPurchased(playerId, ID_OFFSET + 2)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 2);
            critChance += 2 * purchaseCount;
        }

        // Skill 20: Attacks against full-health targets have +15% critical chance (1/2)
        if (isPurchased(playerId, ID_OFFSET + 20) && target.getHealth() >= target.getMaxHealth()) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 20);
            critChance += 15 * purchaseCount;
        }

        // Skill 23: Every third attack on the same target is automatically a critical hit
        Map<UUID, Integer> targetAttacks = attackCountOnTarget.computeIfAbsent(playerId, k -> new HashMap<>());
        int attackCount = targetAttacks.getOrDefault(targetId, 0) + 1;
        targetAttacks.put(targetId, attackCount);

        if (isPurchased(playerId, ID_OFFSET + 23) && attackCount % 3 == 0) {
            isCritical = true;
            ChatNotificationUtils.send(player, ChatColor.GOLD + "Precision Strike!");
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " triggered Precision Strike (skill 23) on " + target.getName());
            }
        } else {
            boolean success = rollChance(critChance, player, "Critical Hit");
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info(String.format(
                    "[SHADOWSTALKER] %s vs %s - Critical Hit: %.1f%% | Result: %s", 
                    player.getName(),
                    target.getName(),
                    critChance,
                    success ? "SUCCESS ✓" : "FAILED ✗"
                ));
            }
            
            if (success) {
                isCritical = true;
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("→ Critical hit landed!");
                }
            }
        }

        // === DAMAGE MODIFIERS ===

        // Skill 3: +15% damage on first attack against target (1/2)
        Set<UUID> attackedTargets = firstAttackTracker.computeIfAbsent(playerId, k -> new HashSet<>());
        if (isPurchased(playerId, ID_OFFSET + 3) && !attackedTargets.contains(targetId)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 3);
            finalDamage *= (1 + 0.15 * purchaseCount);
            attackedTargets.add(targetId);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: First Strike bonus applied");
            }
        }

        // Skill 8: +10% damage when attacking from behind (1/2)
        if (isPurchased(playerId, ID_OFFSET + 8) && isAttackingFromBehind(player, target)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 8);
            finalDamage *= (1 + 0.1 * purchaseCount);

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Backstab bonus applied");
            }
        }

        // Skill 13: +3% damage per second spent sneaking before attacking (max +15%)
        if (isPurchased(playerId, ID_OFFSET + 13) && sneakingStartTime.containsKey(playerId)) {
            long sneakTime = System.currentTimeMillis() - sneakingStartTime.get(playerId);
            double seconds = sneakTime / 1000.0;
            double bonus = Math.min(seconds * 0.03, 0.15);
            finalDamage *= (1 + bonus);
            sneakingStartTime.remove(playerId); // Reset po ataku

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Patient Hunter bonus: " + (bonus * 100) + "%");
            }
        }

        // Skill 14: +12% damage against enemies affected by poison or bleeding (1/2)
        if (isPurchased(playerId, ID_OFFSET + 14) && 
            (isEntityPoisoned(targetId, playerId) || isEntityBleeding(targetId, playerId))) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 14);
            finalDamage *= (1 + 0.12 * purchaseCount);
        }

        // Skill 19: After 3 seconds of not attacking, your next attack deals +35% damage
        if (isPurchased(playerId, ID_OFFSET + 19)) {
            if (previousAttackTime == null || (System.currentTimeMillis() - previousAttackTime) > 3000) {
                finalDamage *= 1.35;
                ChatNotificationUtils.send(player, ChatColor.DARK_GREEN + "Ambush!");
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " triggered Ambush (+35% damage) on " + target.getName());
                }
            }
        }
        
        // Update the last attack time after checking Ambush skill
        lastAttackTime.put(playerId, System.currentTimeMillis());

        // Skill 26: After applying poison, gain +5% damage against that target (1/2)
        if (isPurchased(playerId, ID_OFFSET + 26) && isEntityPoisoned(targetId, playerId)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 26);
            finalDamage *= (1 + 0.05 * purchaseCount);
        }

        // Skill 27: While at full health, attacks ignore 25% of enemy armor
        if (isPurchased(playerId, ID_OFFSET + 27) && player.getHealth() >= player.getMaxHealth()) {
            // Symuluj penetrację pancerza poprzez zwiększenie obrażeń
            finalDamage *= 1.25;

            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Perfect Strike armor penetration");
            }
        }

        // === CRITICAL HIT EFFECTS ===
        if (isCritical) {
            double critMultiplier = 2.0; // Bazowe 200% dmg

            // Skill 11: +20% critical damage (1/2)
            if (isPurchased(playerId, ID_OFFSET + 11)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 11);
                critMultiplier += 0.2 * purchaseCount;
            }

            finalDamage *= critMultiplier;

            // Skill 5: Critical hits apply bleeding (1/2)
            if (isPurchased(playerId, ID_OFFSET + 5)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 5);
                double bleedDamage = baseDamage * 0.03 * purchaseCount;
                applyBleeding(targetId, playerId, bleedDamage, 5000); // 5 sekund
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " applied bleeding to " + target.getName() + 
                            " from critical hit (" + String.format("%.1f", bleedDamage) + " dmg/s for 5s)");
                }
            }

            // Skill 17: Critical hits have 10% chance to deal +50% additional damage (1/2)
            if (isPurchased(playerId, ID_OFFSET + 17)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 17);
                double devastatingChance = 10 * purchaseCount;
                if (rollChance(devastatingChance, player, "Devastating Critical")) {
                    finalDamage *= 1.5;
                    ChatNotificationUtils.send(player, ChatColor.DARK_RED + "DEVASTATING CRITICAL!");
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " triggered Devastating Critical (" + String.format("%.1f", devastatingChance) + "% chance) on " + target.getName());
                    }
                } else {
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " Devastating Critical failed (rolled above " + String.format("%.1f", devastatingChance) + "% chance)");
                    }
                }
            }

            // Skill 18: Each critical hit reduces damage taken by 3% for 4 seconds (max 5 stacks)
            if (isPurchased(playerId, ID_OFFSET + 18)) {
                int currentStacks = criticalDefenseStacks.getOrDefault(playerId, 0);
                if (currentStacks < 5) {
                    criticalDefenseStacks.put(playerId, currentStacks + 1);
                }
                criticalDefenseExpiry.put(playerId, System.currentTimeMillis() + 4000);

                ActionBarUtils.sendActionBar(player, ChatColor.AQUA + "Critical Defense: " + 
                        criticalDefenseStacks.get(playerId) + "/5 stacks");
            }

            // Skill 21: Critical hits against poisoned targets have 25% chance to amplify poison (1/2)
            if (isPurchased(playerId, ID_OFFSET + 21) && isEntityPoisoned(targetId, playerId)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 21);
                double amplifyChance = 25 * purchaseCount;
                if (rollChance(amplifyChance, player, "Poison Amplification")) {
                    amplifyPoison(targetId, playerId);
                    ChatNotificationUtils.send(player, ChatColor.DARK_GREEN + "Poison Amplified!");
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " amplified poison on " + target.getName() + " (" + String.format("%.1f", amplifyChance) + "% chance)");
                    }
                } else {
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " poison amplification failed on " + target.getName() + " (rolled above " + String.format("%.1f", amplifyChance) + "% chance)");
                    }
                }
            }
        }

        // === POISON APPLICATION ===
        // Skill 9: +15% chance for attacks to apply poison (1/2)
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 9);
            double poisonChance = 15 * purchaseCount;
            if (rollChance(poisonChance, player, "Poison Application")) {
                // Calculate poison damage as a percentage of the player's attack damage
                double poisonDamage = baseDamage * 0.05; // 5% of base damage per second
                
                // Skill 15: +20% poison damage and duration (1/2)
                if (isPurchased(playerId, ID_OFFSET + 15)) {
                    int toxinPurchase = getSkillPurchaseCount(playerId, ID_OFFSET + 15);
                    poisonDamage *= (1 + 0.2 * toxinPurchase);
                }

                int duration = 4000; // 4 sekundy
                if (isPurchased(playerId, ID_OFFSET + 15)) {
                    int toxinPurchase = getSkillPurchaseCount(playerId, ID_OFFSET + 15);
                    duration *= (1 + 0.2 * toxinPurchase);
                }

                applyPoison(targetId, playerId, poisonDamage, duration);
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration / 50, 0));
                
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " applied poison to " + target.getName() + 
                            " (" + String.format("%.1f", poisonChance) + "% chance, " + String.format("%.1f", poisonDamage) + " dmg/s for " + (duration/1000) + "s)");
                }
            } else {
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " poison failed on " + target.getName() + 
                            " (rolled above " + String.format("%.1f", poisonChance) + "% chance)");
                }
            }
        }

        // === SPECIAL EFFECTS ===
        // Skill 25: After attacking from sneaking, gain +40% attack speed for 3 seconds
        if (isPurchased(playerId, ID_OFFSET + 25) && player.isSneaking()) {
            ambushSpeedExpiry.put(playerId, System.currentTimeMillis() + 3000);
            ChatNotificationUtils.send(player, ChatColor.YELLOW + "Ambush Speed activated!");
        }

        // Ustaw finalne obrażenia
        event.setDamage(finalDamage);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SHADOWSTALKER: Final damage: " + finalDamage + 
                    " (base: " + baseDamage + ", critical: " + isCritical + ")");
        }
    }

    @Override
    public void handleEntityDeath(EntityDeathEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();

        // Skill 10: After killing an enemy, gain +10% movement speed for 5 seconds
        if (isPurchased(playerId, ID_OFFSET + 10)) {
            assassinHasteExpiry.put(playerId, System.currentTimeMillis() + 5000);
            ChatNotificationUtils.send(player, ChatColor.GREEN + "Assassin's Haste activated!");
            
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " activated Assassin's Haste (+10% movement speed for 5s)");
            }

            // Natychmiastowa aplikacja efektu
            applyMovementSpeedEffects(player, stats);
        }

        // Czyść trackery dla zabitego celu
        UUID targetId = event.getEntity().getUniqueId();
        Map<UUID, Integer> attacks = attackCountOnTarget.get(playerId);
        if (attacks != null) {
            attacks.remove(targetId);
        }

        Set<UUID> firstAttacks = firstAttackTracker.get(playerId);
        if (firstAttacks != null) {
            firstAttacks.remove(targetId);
        }

        // Czyść efekty czasowe
        cleanupEffectsForEntity(targetId);
    }

    // === METODY POMOCNICZE ===

    private void applyMovementSpeedEffects(Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        float baseSpeed = 0.2f; // Bazowa prędkość w Minecraft
        float totalBonus = 1.0f + ((float)stats.getMovementSpeedBonus() / 100.0f);

        // Skill 1: +5% movement speed in shadows and at night (1/2)
        if (isPurchased(playerId, ID_OFFSET + 1) && (isInDarkness(player) || isNightTime(player))) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 1);
            totalBonus += 0.05f * purchaseCount;
        }

        // Skill 10: Assassin's Haste
        if (assassinHasteExpiry.containsKey(playerId) && 
            System.currentTimeMillis() < assassinHasteExpiry.get(playerId)) {
            totalBonus += 0.1f;
        }

        // Skill 16: +25% movement speed while sneaking (1/2)
        if (isPurchased(playerId, ID_OFFSET + 16) && player.isSneaking()) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 16);
            totalBonus += 0.25f * purchaseCount;
        }

        // Skill 25: Ambush Speed attack speed bonus
        if (ambushSpeedExpiry.containsKey(playerId) && 
            System.currentTimeMillis() < ambushSpeedExpiry.get(playerId)) {
            // Symuluj attack speed poprzez haste
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 60, 1));
        }

        player.setWalkSpeed(Math.min(baseSpeed * totalBonus, 1.0f)); // Max speed = 1.0
    }

    private boolean isInDarkness(Player player) {
        Location loc = player.getLocation();
        Block block = loc.getBlock();
        return block.getLightLevel() <= 7;
    }

    private boolean isNightTime(Player player) {
        long time = player.getWorld().getTime();
        return time >= 13000 && time <= 23000;
    }

    private boolean isAttackingFromBehind(Player attacker, LivingEntity target) {
        Location attackerLoc = attacker.getLocation();
        Location targetLoc = target.getLocation();

        org.bukkit.util.Vector targetDirection = targetLoc.getDirection();
        org.bukkit.util.Vector toAttacker = attackerLoc.toVector().subtract(targetLoc.toVector()).normalize();

        double angle = targetDirection.angle(toAttacker);
        
        // For debugging
        if (debuggingFlag == 1) {
            plugin.getLogger().info("SHADOWSTALKER: Angle between target direction and attacker: " + 
                    String.format("%.2f", angle) + " radians (" + String.format("%.2f", Math.toDegrees(angle)) + " degrees)");
            plugin.getLogger().info("SHADOWSTALKER: Attack is " + (angle > (2 * Math.PI / 3) ? "from behind" : "from front"));
        }
        
        // If angle is large (> 120 degrees), attacker is behind the target
        return angle > (2 * Math.PI / 3); // 120 degrees
    }

    private void applyPoison(UUID targetId, UUID playerId, double damagePerSecond, long duration) {
        Map<UUID, PoisonData> playerPoisons = poisonedEntities.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
    
        // Check if the target already has an amplified poison
        PoisonData existingPoison = playerPoisons.get(targetId);
        boolean shouldAmplify = existingPoison != null && existingPoison.amplified;
    
        // Create new poison data, amplify if needed
        PoisonData newPoison = new PoisonData(System.currentTimeMillis() + duration, damagePerSecond, playerId);
        if (shouldAmplify) {
            // Apply amplification (double damage)
            newPoison = new PoisonData(System.currentTimeMillis() + duration, damagePerSecond * 2, playerId);
            newPoison.amplified = true;
        
            if (debuggingFlag == 1) {
                plugin.getLogger().info("SHADOWSTALKER: Applied new poison with existing amplification (damage: " + 
                        String.format("%.1f", damagePerSecond) + " → " + String.format("%.1f", damagePerSecond * 2) + ")");
            }
        }
    
        playerPoisons.put(targetId, newPoison);
    }

    private void applyBleeding(UUID targetId, UUID playerId, double damagePerSecond, long duration) {
        Map<UUID, BleedData> playerBleeds = bleedingEntities.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerBleeds.put(targetId, new BleedData(System.currentTimeMillis() + duration, damagePerSecond, playerId));
    }

    private void amplifyPoison(UUID targetId, UUID playerId) {
        Map<UUID, PoisonData> playerPoisons = poisonedEntities.get(playerId);
        if (playerPoisons != null) {
            PoisonData poison = playerPoisons.get(targetId);
            if (poison != null && !poison.amplified) {
                // Create a new poison with doubled damage
                PoisonData amplified = new PoisonData(poison.expiry, poison.damagePerSecond * 2, playerId);
                amplified.amplified = true;
                playerPoisons.put(targetId, amplified);
            
                if (debuggingFlag == 1) {
                    plugin.getLogger().info("SHADOWSTALKER: Amplified poison damage: " + 
                            String.format("%.1f", poison.damagePerSecond) + " → " + 
                            String.format("%.1f", amplified.damagePerSecond) + " (doubled)");
                }
            }
        }
    }

    private boolean isEntityPoisoned(UUID targetId, UUID playerId) {
        Map<UUID, PoisonData> playerPoisons = poisonedEntities.get(playerId);
        if (playerPoisons == null) return false;

        PoisonData poison = playerPoisons.get(targetId);
        return poison != null && System.currentTimeMillis() < poison.expiry;
    }

    private boolean isEntityBleeding(UUID targetId, UUID playerId) {
        Map<UUID, BleedData> playerBleeds = bleedingEntities.get(playerId);
        if (playerBleeds == null) return false;

        BleedData bleed = playerBleeds.get(targetId);
        return bleed != null && System.currentTimeMillis() < bleed.expiry;
    }

    private void cleanupEffectsForEntity(UUID targetId) {
        // Czyść poison
        for (Map<UUID, PoisonData> playerPoisons : poisonedEntities.values()) {
            playerPoisons.remove(targetId);
        }

        // Czyść bleeding
        for (Map<UUID, BleedData> playerBleeds : bleedingEntities.values()) {
            playerBleeds.remove(targetId);
        }
    }

    protected boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    protected int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
    }

    private boolean rollChance(double chance, Player player, String mechanicName) {
        if (debuggingFlag == 1) {
            return DebugUtils.rollChanceWithDebug(player, mechanicName, chance);
        } else {
            return Math.random() * 100 < chance;
        }
    }
    
    private boolean rollChance(double chance) {
        return Math.random() * 100 < chance;
    }

    // === PERIODIC TASKS ===

    private void startPeriodicTasks() {
        // Task do aplikacji poison/bleed damage
        new BukkitRunnable() {
            @Override
            public void run() {
                applyPeriodicDamage();
                cleanupExpiredEffects();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Co sekundę

        // Task do śledzenia sneaking
        new BukkitRunnable() {
            @Override
            public void run() {
                trackSneaking();
            }
        }.runTaskTimer(plugin, 5L, 5L); // Co 0.25 sekundy
    }

    /**
     * Apply periodic effects - called by AscendancySkillEffectIntegrator
     */
    public void applyPeriodicEffects() {
        applyPeriodicDamage();
        cleanupExpiredEffects();
        trackSneaking();
    }

    private void applyPeriodicDamage() {
        long currentTime = System.currentTimeMillis();

        // Aplikuj poison damage
        for (Map.Entry<UUID, Map<UUID, PoisonData>> playerEntry : poisonedEntities.entrySet()) {
            Player player = plugin.getServer().getPlayer(playerEntry.getKey());
            if (player != null && player.isOnline()) {
                for (Map.Entry<UUID, PoisonData> poisonEntry : playerEntry.getValue().entrySet()) {
                    if (currentTime < poisonEntry.getValue().expiry) {
                        Entity entity = plugin.getServer().getEntity(poisonEntry.getKey());
                        if (entity instanceof LivingEntity && entity.isValid()) {
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(poisonEntry.getValue().damagePerSecond, player); // Apply full poison damage per second
                        }
                    }
                }
            }
        }

        // Aplikuj bleed damage
        for (Map.Entry<UUID, Map<UUID, BleedData>> playerEntry : bleedingEntities.entrySet()) {
            Player player = plugin.getServer().getPlayer(playerEntry.getKey());
            if (player != null && player.isOnline()) {
                for (Map.Entry<UUID, BleedData> bleedEntry : playerEntry.getValue().entrySet()) {
                    if (currentTime < bleedEntry.getValue().expiry) {
                        Entity entity = plugin.getServer().getEntity(bleedEntry.getKey());
                        if (entity instanceof LivingEntity && entity.isValid()) {
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(bleedEntry.getValue().damagePerSecond, player); // Apply full bleeding damage per second

                            // Efekt wizualny krwawienia
                            target.getWorld().playEffect(target.getLocation(),
                                    org.bukkit.Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                        }
                    }
                }
            }
        }
    }

    private void cleanupExpiredEffects() {
        long currentTime = System.currentTimeMillis();

        // Cleanup expired effects
        for (Map<UUID, PoisonData> playerPoisons : poisonedEntities.values()) {
            playerPoisons.entrySet().removeIf(entry -> currentTime >= entry.getValue().expiry);
        }

        for (Map<UUID, BleedData> playerBleeds : bleedingEntities.values()) {
            playerBleeds.entrySet().removeIf(entry -> currentTime >= entry.getValue().expiry);
        }

        assassinHasteExpiry.entrySet().removeIf(entry -> currentTime >= entry.getValue());
        ambushSpeedExpiry.entrySet().removeIf(entry -> currentTime >= entry.getValue());
        criticalDefenseExpiry.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }

    private void trackSneaking() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            String playerAscendancy = plugin.getClassManager().getPlayerAscendancy(playerId);
            
            // Only process for Shadowstalker players
            if (!"Shadowstalker".equals(playerAscendancy)) {
                continue;
            }

            if (player.isSneaking()) {
                // Jeśli dopiero zaczął sneaking
                if (!sneakingStartTime.containsKey(playerId)) {
                    sneakingStartTime.put(playerId, System.currentTimeMillis());
                    
                    // Apply movement speed effects when starting to sneak
                    SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    applyMovementSpeedEffects(player, stats);
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " started sneaking, applying movement speed effects");
                    }
                }
            } else {
                // If player was sneaking before but isn't now
                if (sneakingStartTime.containsKey(playerId)) {
                    // Przestał się skradać
                    sneakingStartTime.remove(playerId);
                    
                    // Reapply movement speed effects to remove sneaking bonus
                    SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);
                    applyMovementSpeedEffects(player, stats);
                    
                    if (debuggingFlag == 1) {
                        plugin.getLogger().info("SHADOWSTALKER: " + player.getName() + " stopped sneaking, updating movement speed");
                    }
                }
            }
        }
    }

    // Metoda do czyszczenia danych gracza przy wylogowaniu
    public void clearPlayerData(UUID playerId) {
        firstAttackTracker.remove(playerId);
        sneakingStartTime.remove(playerId);
        lastAttackTime.remove(playerId);
        attackCountOnTarget.remove(playerId);
        criticalDefenseStacks.remove(playerId);
        criticalDefenseExpiry.remove(playerId);
        poisonedEntities.remove(playerId);
        bleedingEntities.remove(playerId);
        assassinHasteExpiry.remove(playerId);
        ambushSpeedExpiry.remove(playerId);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SHADOWSTALKER: Cleared all data for player " + playerId);
        }
    }
}
