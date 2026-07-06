package com.maks.myexperienceplugin.Class.skills.classes.ranger.ascendancy;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.EarthwardenSkillEffectsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Set;

public class EarthwardenTestCommand implements CommandExecutor {
    private final MyExperiencePlugin plugin;
    private static final int ID_OFFSET = 600000;

    public EarthwardenTestCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setclass":
                // Set class to Ranger and ascendancy to Earthwarden
                plugin.getClassManager().setPlayerClass(player, "Ranger");
                plugin.getClassManager().setPlayerAscendancy(player, "Earthwarden");
                player.sendMessage(ChatColor.GREEN + "Set your class to Ranger/Earthwarden!");
                break;

            case "giveskill":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /ewtest giveskill <skill_number>");
                    return true;
                }

                try {
                    int skillNum = Integer.parseInt(args[1]);
                    if (skillNum < 1 || skillNum > 27) {
                        player.sendMessage(ChatColor.RED + "Skill number must be between 1 and 27!");
                        return true;
                    }

                    int skillId = ID_OFFSET + skillNum;
                    plugin.getSkillTreeManager().purchaseSkill(player, skillId);
                    player.sendMessage(ChatColor.GREEN + "Granted Earthwarden skill #" + skillNum);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid skill number!");
                }
                break;

            case "giveall":
                // Grant all Earthwarden skills
                for (int i = 1; i <= 27; i++) {
                    plugin.getSkillTreeManager().purchaseSkill(player, ID_OFFSET + i);
                }
                player.sendMessage(ChatColor.GREEN + "Granted all 27 Earthwarden skills!");
                break;

            case "stats":
                // Show player stats
                showPlayerStats(player);
                break;

            case "debug":
                // Show debug info
                EarthwardenSkillEffectsHandler handler = getHandler();
                if (handler != null) {
                    player.sendMessage(ChatColor.GOLD + "Debug info sent to console");
                    plugin.getLogger().info("[EARTHWARDEN DEBUG] Debug info for " + player.getName());
                    
                    // Call debug method if it exists
                    try {
                        handler.getClass().getMethod("debugPlayerStats", Player.class).invoke(handler, player);
                    } catch (Exception e) {
                        // If debugPlayerStats doesn't exist, show basic info
                        player.sendMessage(ChatColor.YELLOW + "Basic debug info:");
                        showPlayerStats(player);
                        showEnvironmentInfo(player);
                        plugin.getLogger().info("[EARTHWARDEN] Debug info for " + player.getName() + " - basic stats shown");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find EarthwardenSkillEffectsHandler");
                }
                break;

            case "simulate":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /ewtest simulate <scenario>");
                    showSimulateHelp(player);
                    return true;
                }
                simulateScenario(player, args[1]);
                break;

            case "environment":
                // Show environment info
                showEnvironmentInfo(player);
                break;

            case "gear":
                // Give testing gear
                giveTestingGear(player);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Earthwarden Test Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/ewtest setclass - Set your class to Earthwarden");
        player.sendMessage(ChatColor.YELLOW + "/ewtest giveskill <1-27> - Grant specific skill");
        player.sendMessage(ChatColor.YELLOW + "/ewtest giveall - Grant all skills");
        player.sendMessage(ChatColor.YELLOW + "/ewtest stats - Show your current stats");
        player.sendMessage(ChatColor.YELLOW + "/ewtest debug - Show debug information");
        player.sendMessage(ChatColor.YELLOW + "/ewtest simulate <scenario> - Simulate scenarios");
        player.sendMessage(ChatColor.YELLOW + "/ewtest environment - Check environment bonuses");
        player.sendMessage(ChatColor.YELLOW + "/ewtest gear - Get testing gear");
    }

    private void showSimulateHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Simulation Scenarios ===");
        player.sendMessage(ChatColor.YELLOW + "lowhealth - Set HP to 30%");
        player.sendMessage(ChatColor.YELLOW + "criticalhealth - Set HP to 20%");
        player.sendMessage(ChatColor.YELLOW + "verylowhealth - Set HP to 10%");
        player.sendMessage(ChatColor.YELLOW + "grass - Create grass platform");
        player.sendMessage(ChatColor.YELLOW + "falldamage - Simulate fall damage");
        player.sendMessage(ChatColor.YELLOW + "firedamage - Simulate fire damage");
        player.sendMessage(ChatColor.YELLOW + "enemies - Spawn multiple enemies");
        player.sendMessage(ChatColor.YELLOW + "allies - Spawn test allies (armor stands)");
        player.sendMessage(ChatColor.YELLOW + "fullhealth - Restore to full health");
        player.sendMessage(ChatColor.YELLOW + "stilltest - Test standing still bonuses");
    }

    private void showPlayerStats(Player player) {
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        player.sendMessage(ChatColor.GOLD + "=== Your Earthwarden Stats ===");
        player.sendMessage(ChatColor.AQUA + "Defense Bonus: " + stats.getDefenseBonus() + "%");
        player.sendMessage(ChatColor.AQUA + "Max Health Bonus: " + stats.getMaxHealthBonus());
        player.sendMessage(ChatColor.AQUA + "Evade Chance: " + stats.getEvadeChance() + "%");
        player.sendMessage(ChatColor.AQUA + "Damage Multiplier: " + String.format("%.2f", stats.getDamageMultiplier()) + "x");
        player.sendMessage(ChatColor.AQUA + "Movement Speed Bonus: " + stats.getMovementSpeedBonus() + "%");
        player.sendMessage(ChatColor.AQUA + "Luck Bonus: " + stats.getLuckBonus() + "%");

        // Show active skills
        player.sendMessage(ChatColor.GOLD + "=== Active Skills ===");
        int activeSkills = 0;
        Set<Integer> purchasedSkills = plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId());
        for (int i = 1; i <= 27; i++) {
            if (purchasedSkills.contains(Integer.valueOf(ID_OFFSET + i))) {
                activeSkills++;
            }
        }
        player.sendMessage(ChatColor.GREEN + "Active Skills: " + activeSkills + "/27");

        // Show some key skills status
        showKeySkillsStatus(player, purchasedSkills);
    }

    private void showKeySkillsStatus(Player player, Set<Integer> skills) {
        player.sendMessage(ChatColor.GOLD + "=== Key Skills Status ===");
        
        // Check important skills
        if (skills.contains(ID_OFFSET + 1)) {
            player.sendMessage(ChatColor.GREEN + "✓ Grassy Defense Bonus");
        }
        if (skills.contains(ID_OFFSET + 2)) {
            player.sendMessage(ChatColor.GREEN + "✓ Post-Kill Healing");
        }
        if (skills.contains(ID_OFFSET + 3)) {
            player.sendMessage(ChatColor.GREEN + "✓ Environmental Resistance");
        }
        if (skills.contains(ID_OFFSET + 5)) {
            player.sendMessage(ChatColor.GREEN + "✓ Standing Still Defense");
        }
        if (skills.contains(ID_OFFSET + 8)) {
            player.sendMessage(ChatColor.GREEN + "✓ Root Attack");
        }
        if (skills.contains(ID_OFFSET + 15)) {
            player.sendMessage(ChatColor.GREEN + "✓ Low HP Defense & Damage");
        }
        if (skills.contains(ID_OFFSET + 25)) {
            player.sendMessage(ChatColor.GREEN + "✓ Critical HP Evasion");
        }
        if (skills.contains(ID_OFFSET + 27)) {
            player.sendMessage(ChatColor.GREEN + "✓ Second Chance (death immunity)");
        }
    }

    private void showEnvironmentInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Environment Info ===");
        
        Location loc = player.getLocation();
        Block blockBelow = loc.getBlock().getRelative(BlockFace.DOWN);
        Material belowMaterial = blockBelow.getType();
        
        // Check for grassy areas
        boolean isGrassy = isGrassyArea(blockBelow);
        
        player.sendMessage(ChatColor.AQUA + "Block below: " + belowMaterial.toString());
        player.sendMessage(ChatColor.AQUA + "Is grassy area: " + (isGrassy ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));
        
        // Check nearby allies (other players)
        long nearbyAllies = player.getWorld().getNearbyEntities(loc, 15, 15, 15).stream()
            .filter(entity -> entity instanceof Player && entity != player)
            .count();
        player.sendMessage(ChatColor.AQUA + "Nearby allies (15 blocks): " + nearbyAllies);
        
        // Check surrounding enemies
        long nearbyEnemies = player.getWorld().getNearbyEntities(loc, 8, 8, 8).stream()
            .filter(entity -> entity instanceof org.bukkit.entity.Monster)
            .count();
        player.sendMessage(ChatColor.AQUA + "Nearby enemies (8 blocks): " + nearbyEnemies);
        
        // Current health percentage
        double currentHP = player.getHealth();
        double maxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double hpPercent = (currentHP / maxHP) * 100;
        player.sendMessage(ChatColor.AQUA + "Health: " + String.format("%.1f", hpPercent) + "%");
    }
    
    private boolean isGrassyArea(Block block) {
        Material material = block.getType();
        return material == Material.GRASS_BLOCK || 
               material == Material.GRASS || 
               material == Material.TALL_GRASS || 
               material == Material.FERN ||
               material == Material.LARGE_FERN;
    }

    private void simulateScenario(Player player, String scenario) {
        switch (scenario.toLowerCase()) {
            case "lowhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.3);
                player.sendMessage(ChatColor.YELLOW + "Set health to 30% - low HP bonuses may activate!");
                break;

            case "criticalhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.2);
                player.sendMessage(ChatColor.GOLD + "Set health to 20% - critical HP bonuses should activate!");
                break;

            case "verylowhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.1);
                player.sendMessage(ChatColor.RED + "Set health to 10% - maximum defensive bonuses!");
                break;

            case "grass":
                // Create grass platform around player
                Location loc = player.getLocation();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        Block block = loc.add(x, -1, z).getBlock();
                        block.setType(Material.GRASS_BLOCK);
                        loc.subtract(x, -1, z);
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Created grass platform - grass bonuses should activate!");
                break;

            case "falldamage":
                // Use damage() method without cause parameter
                player.damage(5.0);
                player.sendMessage(ChatColor.YELLOW + "Simulated fall damage - resistance should reduce it!");
                break;

            case "firedamage":
                // Use damage() method without cause parameter  
                player.damage(3.0);
                player.sendMessage(ChatColor.YELLOW + "Simulated fire damage - resistance should reduce it!");
                break;

            case "enemies":
                // Spawn 3-5 enemies around player
                Location loc2 = player.getLocation();
                for (int i = 0; i < 4; i++) {
                    double angle = (i * 90) * Math.PI / 180;
                    double x = loc2.getX() + 3 * Math.cos(angle);
                    double z = loc2.getZ() + 3 * Math.sin(angle);
                    Location spawnLoc = new Location(loc2.getWorld(), x, loc2.getY(), z);
                    loc2.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
                }
                player.sendMessage(ChatColor.YELLOW + "Spawned enemies - surrounded bonuses should activate!");
                break;

            case "allies":
                // Spawn armor stands as fake allies
                Location loc3 = player.getLocation();
                for (int i = 0; i < 3; i++) {
                    double angle = (i * 120) * Math.PI / 180;
                    double x = loc3.getX() + 2 * Math.cos(angle);
                    double z = loc3.getZ() + 2 * Math.sin(angle);
                    Location spawnLoc = new Location(loc3.getWorld(), x, loc3.getY(), z);
                    org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) 
                        loc3.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
                    stand.setCustomName("Test Ally");
                    stand.setCustomNameVisible(true);
                }
                player.sendMessage(ChatColor.GREEN + "Spawned test allies - ally bonuses should activate!");
                break;

            case "fullhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.sendMessage(ChatColor.GREEN + "Restored to full health!");
                break;

            case "stilltest":
                player.sendMessage(ChatColor.YELLOW + "Stand still for 3+ seconds to test standing still bonus!");
                player.sendMessage(ChatColor.GRAY + "Defense should increase when not moving...");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown scenario: " + scenario);
                showSimulateHelp(player);
                break;
        }
    }

    private void giveTestingGear(Player player) {
        // Give appropriate gear for testing
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        
        // Add some enchants for testing
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 2);
        helmet.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        chestplate.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        
        player.getInventory().addItem(sword, shield, helmet, chestplate, leggings, boots);
        player.sendMessage(ChatColor.GREEN + "Received testing gear (heavy armor for defense testing)!");
    }

    private EarthwardenSkillEffectsHandler getHandler() {
        try {
            return (EarthwardenSkillEffectsHandler) plugin.getAscendancySkillEffectIntegrator()
                .getHandler("Earthwarden");
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting EarthwardenSkillEffectsHandler: " + e.getMessage());
            return null;
        }
    }
}
