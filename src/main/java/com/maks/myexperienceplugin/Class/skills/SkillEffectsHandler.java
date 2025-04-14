package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.Class.skills.events.SkillPurchasedEvent;
import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class SkillEffectsHandler implements Listener {
    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final Random random = new Random();

    // Debugging flag - set to 0 after testing
    private final int debuggingFlag = 1;

    // Cached player stats
    private final Map<UUID, PlayerSkillStats> playerStatsCache = new HashMap<>();

    // Constants for attribute modifier names
    private static final String ATTR_MAX_HEALTH = "skill.maxhealth";
    private static final String ATTR_MOVEMENT_SPEED = "skill.movementspeed";
    private static final String ATTR_ATTACK_DAMAGE = "skill.attackdamage";
    private static final String ATTR_ARMOR = "skill.armor";
    private static final String ATTR_LUCK = "skill.luck";

    public SkillEffectsHandler(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // First, clear any previously applied attribute modifiers
        clearAllSkillModifiers(player);

        // Then calculate and apply fresh stats
        calculatePlayerStats(player);
        applyPlayerStats(player);

        if (debuggingFlag == 1) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Applied fresh stats for " + player.getName());
                PlayerSkillStats stats = playerStatsCache.get(player.getUniqueId());
                if (stats != null) {
                    plugin.getLogger().info("MaxHealthBonus: " + stats.getMaxHealthBonus());
                    plugin.getLogger().info("Current max health: " + player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                    // Debug all modifiers on max health
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers().forEach(mod ->
                            plugin.getLogger().info("Modifier: " + mod.getName() + " Amount: " + mod.getAmount())
                    );
                }
            }, 20L);
        }
    }

    /**
     * Listen for skill purchase events to update stats
     */
    @EventHandler
    public void onSkillPurchased(SkillPurchasedEvent event) {
        Player player = event.getPlayer();

        if (debuggingFlag == 1) {
            plugin.getLogger().info("SkillPurchasedEvent received for player " + player.getName() +
                    ", skillId: " + event.getSkillId());
        }

        // Refresh player stats when a skill is purchased
        refreshPlayerStats(player);
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
            attr.getModifiers().stream()
                    .filter(mod -> mod.getName().startsWith(namePrefix))
                    .forEach(mod -> attr.removeModifier(mod));
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
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply evade chance
        if (stats.getEvadeChance() > 0 && random.nextDouble() * 100 < stats.getEvadeChance()) {
            event.setCancelled(true);
            player.sendMessage("§a§oYou evaded the attack!");
            return;
        }

        // Apply shield block chance
        if (stats.getShieldBlockChance() > 0 && random.nextDouble() * 100 < stats.getShieldBlockChance()) {
            event.setDamage(event.getDamage() * 0.5); // 50% damage reduction
            player.sendMessage("§a§oYour shield blocked half the damage!");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        PlayerSkillStats stats = getPlayerStats(player);

        // Apply bonus damage
        if (stats.getBonusDamage() > 0) {
            event.setDamage(event.getDamage() + stats.getBonusDamage());
            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.DARK_GRAY + "Skill bonus damage: +" + stats.getBonusDamage());
            }
        }

        // Apply damage multiplier
        if (stats.getDamageMultiplier() != 1.0) {
            double newDamage = event.getDamage() * stats.getDamageMultiplier();
            event.setDamage(newDamage);
            if (debuggingFlag == 1) {
                player.sendMessage(ChatColor.DARK_GRAY + "Damage multiplier: x" + String.format("%.2f", stats.getDamageMultiplier()));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            PlayerSkillStats stats = getPlayerStats(player);

            // Apply gold per kill bonus
            if (stats.getGoldPerKill() > 0) {
                plugin.moneyRewardHandler.depositMoney(player, stats.getGoldPerKill());
                player.sendMessage("§6+" + stats.getGoldPerKill() + "$ from trophy hunter skill!");
            }
        }
    }

    public void calculatePlayerStats(Player player) {
        UUID uuid = player.getUniqueId();
        String playerClass = plugin.getClassManager().getPlayerClass(uuid);
        String ascendancy = plugin.getClassManager().getPlayerAscendancy(uuid);

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            playerStatsCache.put(uuid, new PlayerSkillStats());
            return;
        }

        PlayerSkillStats stats = new PlayerSkillStats();

        // Apply base class skills
        Set<Integer> purchasedSkills = skillTreeManager.getPurchasedSkills(uuid);

        for (int skillId : purchasedSkills) {
            // Check if this is a base class skill
            if (skillTreeManager.getSkillTree(playerClass, "basic") != null &&
                    skillTreeManager.getSkillTree(playerClass, "basic").getNode(skillId) != null) {

                int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);

                // Apply skill stats based on player's class
                if ("Ranger".equalsIgnoreCase(playerClass)) {
                    applyRangerSkillEffects(stats, skillId, purchaseCount);
                } else if ("DragonKnight".equalsIgnoreCase(playerClass)) {
                    applyDragonKnightSkillEffects(stats, skillId, purchaseCount);
                }
                // Add more class-specific implementations as needed
            }

            // Check if this is an ascendancy skill
            else if (!ascendancy.isEmpty() &&
                    skillTreeManager.getSkillTree(playerClass, "ascendancy", ascendancy) != null &&
                    skillTreeManager.getSkillTree(playerClass, "ascendancy", ascendancy).getNode(skillId) != null) {

                int purchaseCount = skillTreeManager.getSkillPurchaseCount(uuid, skillId);

                // Apply ascendancy skill stats
                if ("Beastmaster".equalsIgnoreCase(ascendancy)) {
                    applyBeastmasterSkillEffects(stats, skillId, purchaseCount);
                } else if ("Berserker".equalsIgnoreCase(ascendancy)) {
                    applyBerserkerSkillEffects(stats, skillId, purchaseCount);
                }
                // Add more ascendancy-specific implementations as needed
            }
        }

        playerStatsCache.put(uuid, stats);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Calculated stats for player " + player.getName() + ": " +
                    "HP+" + stats.getMaxHealthBonus() + ", " +
                    "DMG+" + stats.getBonusDamage() + ", " +
                    "MULT×" + stats.getDamageMultiplier());
        }
    }

    private void applyRangerSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +1% movement speed
                stats.addMovementSpeedBonus(1 * purchaseCount);
                break;
            case 3: // +5 damage
                stats.addBonusDamage(5 * purchaseCount);
                break;
            case 4: // +2% evade chance
                stats.addEvadeChance(2 * purchaseCount);
                break;
            case 5: // +1 HP
                stats.addMaxHealth(1 * purchaseCount);
                break;
            case 6: // +3$ per killed mob
                stats.addGoldPerKill(3 * purchaseCount);
                break;
            case 8: // +1% evade chance (1/2)
                stats.addEvadeChance(1 * purchaseCount);
                break;
            case 9: // +1% luck (1/2)
                stats.addLuckBonus(1 * purchaseCount);
                break;
            case 10: // each 3 hits deals +10 dmg - handled by a more complex system
                break;
            case 11: // +1% dmg (1/3)
                stats.addDamageMultiplier(0.01 * purchaseCount);
                break;
            case 13: // +1% def (1/2)
                stats.addDefenseBonus(1 * purchaseCount);
                break;
            case 14: // +4% evade chance, -2% dmg
                stats.addEvadeChance(4 * purchaseCount);
                stats.addDamageMultiplier(-0.02 * purchaseCount);
                break;
            // Add more Ranger skills as needed
        }
    }

    private void applyDragonKnightSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        switch (skillId) {
            case 1: // +3% def
                stats.addDefenseBonus(3 * purchaseCount);
                break;
            case 3: // +1% dmg
                stats.addDamageMultiplier(0.01 * purchaseCount);
                break;
            case 5: // +1% ms (1/2)
                stats.addMovementSpeedBonus(1 * purchaseCount);
                break;
            case 8: // +2hp (1/2)
                stats.addMaxHealth(2 * purchaseCount);
                break;
            case 9: // +1% luck (1/2)
                stats.addLuckBonus(1 * purchaseCount);
                break;
            case 10: // +7 dmg (1/2)
                stats.addBonusDamage(7 * purchaseCount);
                break;
            case 11: // +5% dmg, -2% ms
                stats.addDamageMultiplier(0.05 * purchaseCount);
                stats.addMovementSpeedBonus(-2 * purchaseCount);
                break;
            case 13: // +10 dmg
                stats.addBonusDamage(10 * purchaseCount);
                break;
            case 14: // +5% shield block chance
                stats.addShieldBlockChance(5 * purchaseCount);
                break;
            // Add more DragonKnight skills as needed
        }
    }

    private void applyBeastmasterSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        // Example implementation - would be expanded for all skills
        switch (skillId) {
            case 9: // Pack Damage: All summons gain +5% damage
                // This would affect companion damage, not player damage
                break;
            case 14: // Pack Damage Plus: All summons gain +10% damage
                // This would affect companion damage, not player damage
                break;
            case 15: // Bear Guardian: When Bears hp<50% you and all summons gain +10% def
                stats.addDefenseBonus(10 * purchaseCount);
                break;
            // ... other Beastmaster skills
        }
    }

    private void applyBerserkerSkillEffects(PlayerSkillStats stats, int skillId, int purchaseCount) {
        // Example implementation - would be expanded for all skills
        switch (skillId) {
            case 1: // Cannot wear chestplate but gain +200% dmg
                stats.addDamageMultiplier(2.0 * purchaseCount);
                break;
            case 2: // Each 10% hp lost gives +10% dmg
                // This is a dynamic effect and would be handled in combat
                break;
            // ... other Berserker skills
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
        clearAllSkillModifiers(player);
        calculatePlayerStats(player);
        applyPlayerStats(player);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Refreshed player stats for " + player.getName());
        }
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

        // Adders
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
    }
}