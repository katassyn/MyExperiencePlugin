package com.maks.myexperienceplugin.Class.skills.effects.ascendancy;

import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.SkillTreeManager;
import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.utils.ActionBarUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    public void applySkillEffects(SkillEffectsHandler.PlayerSkillStats stats, int skillId, int purchaseCount) {
        // Większość efektów Shadowstalkera jest dynamiczna
        // Statyczne efekty są już obsługiwane w ShadowstalkerSkillManager.applySkillStats()

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SHADOWSTALKER: Applying skill effects for skill " + 
                    (skillId - ID_OFFSET) + " with purchase count " + purchaseCount);
        }
    }

    @Override
    public void handleEntityDamage(EntityDamageEvent event, Player player, SkillEffectsHandler.PlayerSkillStats stats) {
        UUID playerId = player.getUniqueId();
        double damage = event.getDamage();

        // Skill 4: +4% evade chance while sneaking
        if (isPurchased(playerId, ID_OFFSET + 4) && player.isSneaking()) {
            double evadeChance = stats.getEvadeChance() + 4;
            if (rollChance(evadeChance)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.GRAY + "Evaded!");
                return;
            }
        }

        // Skill 6: +3% evade chance in darkness (1/2)
        if (isPurchased(playerId, ID_OFFSET + 6) && isInDarkness(player)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 6);
            double evadeChance = stats.getEvadeChance() + (3 * purchaseCount);
            if (rollChance(evadeChance)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.DARK_GRAY + "Shadow evade!");
                return;
            }
        }

        // Skill 12: When below 30% health, gain +15% evade chance
        if (isPurchased(playerId, ID_OFFSET + 12) && player.getHealth() < (player.getMaxHealth() * 0.3)) {
            double evadeChance = stats.getEvadeChance() + 15;
            if (rollChance(evadeChance)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Desperate evade!");
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

        // Śledź czas ataku
        lastAttackTime.put(playerId, System.currentTimeMillis());

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
            player.sendMessage(ChatColor.GOLD + "Precision Strike!");
        } else if (rollChance(critChance)) {
            isCritical = true;
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
            Long lastAttack = lastAttackTime.get(playerId);
            if (lastAttack == null || (System.currentTimeMillis() - lastAttack) > 3000) {
                finalDamage *= 1.35;
                player.sendMessage(ChatColor.DARK_GREEN + "Ambush!");
            }
        }

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
            }

            // Skill 17: Critical hits have 10% chance to deal +50% additional damage (1/2)
            if (isPurchased(playerId, ID_OFFSET + 17)) {
                int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 17);
                if (rollChance(10 * purchaseCount)) {
                    finalDamage *= 1.5;
                    player.sendMessage(ChatColor.DARK_RED + "DEVASTATING CRITICAL!");
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
                if (rollChance(25 * purchaseCount)) {
                    amplifyPoison(targetId, playerId);
                    player.sendMessage(ChatColor.DARK_GREEN + "Poison Amplified!");
                }
            }
        }

        // === POISON APPLICATION ===
        // Skill 9: +15% chance for attacks to apply poison (1/2)
        if (isPurchased(playerId, ID_OFFSET + 9)) {
            int purchaseCount = getSkillPurchaseCount(playerId, ID_OFFSET + 9);
            if (rollChance(15 * purchaseCount)) {
                double poisonDamage = 10; // Bazowe 10 dmg/s

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
            }
        }

        // === SPECIAL EFFECTS ===
        // Skill 25: After attacking from sneaking, gain +40% attack speed for 3 seconds
        if (isPurchased(playerId, ID_OFFSET + 25) && player.isSneaking()) {
            ambushSpeedExpiry.put(playerId, System.currentTimeMillis() + 3000);
            player.sendMessage(ChatColor.YELLOW + "Ambush Speed activated!");
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
            player.sendMessage(ChatColor.GREEN + "Assassin's Haste activated!");

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
        return angle < Math.PI / 3; // 60 stopni
    }

    private void applyPoison(UUID targetId, UUID playerId, double damagePerSecond, long duration) {
        Map<UUID, PoisonData> playerPoisons = poisonedEntities.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        playerPoisons.put(targetId, new PoisonData(System.currentTimeMillis() + duration, damagePerSecond, playerId));
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
                poison.amplified = true;
                // Podwój obrażenia
                PoisonData amplified = new PoisonData(poison.expiry, poison.damagePerSecond * 2, playerId);
                amplified.amplified = true;
                playerPoisons.put(targetId, amplified);
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

    private boolean isPurchased(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getPurchasedSkills(playerId).contains(skillId);
    }

    private int getSkillPurchaseCount(UUID playerId, int skillId) {
        return plugin.getSkillTreeManager().getSkillPurchaseCount(playerId, skillId);
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
            for (Map.Entry<UUID, PoisonData> poisonEntry : playerEntry.getValue().entrySet()) {
                if (currentTime < poisonEntry.getValue().expiry) {
                    Entity entity = plugin.getServer().getEntity(poisonEntry.getKey());
                    if (entity instanceof LivingEntity && entity.isValid()) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(poisonEntry.getValue().damagePerSecond / 20); // Dziel przez 20 dla tickrate
                    }
                }
            }
        }

        // Aplikuj bleed damage
        for (Map.Entry<UUID, Map<UUID, BleedData>> playerEntry : bleedingEntities.entrySet()) {
            for (Map.Entry<UUID, BleedData> bleedEntry : playerEntry.getValue().entrySet()) {
                if (currentTime < bleedEntry.getValue().expiry) {
                    Entity entity = plugin.getServer().getEntity(bleedEntry.getKey());
                    if (entity instanceof LivingEntity && entity.isValid()) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(bleedEntry.getValue().damagePerSecond / 20);

                        // Efekt wizualny krwawienia
                        target.getWorld().playEffect(target.getLocation(), 
                                org.bukkit.Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                    }
                }
            }
        }
    }

    private void cleanupExpiredEffects() {
        long currentTime = System.currentTimeMillis();

        // Czyść wygasłe efekty
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

            if (player.isSneaking()) {
                // Jeśli dopiero zaczął sneaking
                if (!sneakingStartTime.containsKey(playerId)) {
                    sneakingStartTime.put(playerId, System.currentTimeMillis());
                }
            } else {
                // Przestał się skradać
                sneakingStartTime.remove(playerId);
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
