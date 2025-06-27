package com.maks.myexperienceplugin.Class.skills.classes.dragonknight.ascendancy;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.ascendancy.ScaleGuardianSkillEffectsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class ScaleGuardianTestCommand implements CommandExecutor {
    private final MyExperiencePlugin plugin;
    private static final int ID_OFFSET = 400000;

    public ScaleGuardianTestCommand(MyExperiencePlugin plugin) {
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
                // Set class to DragonKnight and ascendancy to ScaleGuardian
                plugin.getClassManager().setPlayerClass(player, "DragonKnight");
                plugin.getClassManager().setPlayerAscendancy(player, "ScaleGuardian");
                player.sendMessage(ChatColor.GREEN + "Set your class to DragonKnight/ScaleGuardian!");
                break;

            case "giveskill":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sgtest giveskill <skill_number>");
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
                    player.sendMessage(ChatColor.GREEN + "Granted Scale Guardian skill #" + skillNum);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid skill number!");
                }
                break;

            case "giveall":
                // Grant all Scale Guardian skills
                for (int i = 1; i <= 27; i++) {
                    plugin.getSkillTreeManager().purchaseSkill(player, ID_OFFSET + i);
                }
                player.sendMessage(ChatColor.GREEN + "Granted all 27 Scale Guardian skills!");
                break;

            case "stats":
                // Show player stats
                showPlayerStats(player);
                break;

            case "debug":
                // Show debug info
                ScaleGuardianSkillEffectsHandler handler = getHandler();
                if (handler != null) {
                    player.sendMessage(ChatColor.GOLD + "Debug info sent to console");
                    plugin.getLogger().info("[SCALE GUARDIAN DEBUG] Debug info for " + player.getName());
                    // Call debug method if it exists
                    try {
                        handler.getClass().getMethod("debugPlayerStats", Player.class).invoke(handler, player);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Debug method not found or error occurred");
                        plugin.getLogger().warning("Error calling debugPlayerStats: " + e.getMessage());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find ScaleGuardianSkillEffectsHandler");
                }
                break;

            case "simulate":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /sgtest simulate <scenario>");
                    showSimulateHelp(player);
                    return true;
                }
                simulateScenario(player, args[1]);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Scale Guardian Test Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/sgtest setclass - Set your class to ScaleGuardian");
        player.sendMessage(ChatColor.YELLOW + "/sgtest giveskill <1-27> - Grant specific skill");
        player.sendMessage(ChatColor.YELLOW + "/sgtest giveall - Grant all skills");
        player.sendMessage(ChatColor.YELLOW + "/sgtest stats - Show your current stats");
        player.sendMessage(ChatColor.YELLOW + "/sgtest debug - Show debug information");
        player.sendMessage(ChatColor.YELLOW + "/sgtest simulate <scenario> - Simulate combat scenarios");
    }

    private void showSimulateHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Simulation Scenarios ===");
        player.sendMessage(ChatColor.YELLOW + "lowhealth - Set HP to 20%");
        player.sendMessage(ChatColor.YELLOW + "criticalhealth - Set HP to 5%");
        player.sendMessage(ChatColor.YELLOW + "heavyhit - Simulate taking 50% max HP damage");
        player.sendMessage(ChatColor.YELLOW + "surrounded - Spawn 5 zombies around you");
        player.sendMessage(ChatColor.YELLOW + "allylow - Spawn ally with 20% HP nearby");
    }

    private void showPlayerStats(Player player) {
        SkillEffectsHandler.PlayerSkillStats stats = plugin.getSkillEffectsHandler().getPlayerStats(player);

        player.sendMessage(ChatColor.GOLD + "=== Your Scale Guardian Stats ===");
        player.sendMessage(ChatColor.AQUA + "Shield Block Chance: " + stats.getShieldBlockChance() + "%");
        player.sendMessage(ChatColor.AQUA + "Defense Bonus: " + stats.getDefenseBonus() + "%");
        player.sendMessage(ChatColor.AQUA + "Damage Multiplier: " + stats.getDamageMultiplier() + "x");
        player.sendMessage(ChatColor.AQUA + "Max Health Bonus: " + stats.getMaxHealthBonus());
        player.sendMessage(ChatColor.AQUA + "Movement Speed Bonus: " + stats.getMovementSpeedBonus() + "%");

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
    }

    private void simulateScenario(Player player, String scenario) {
        switch (scenario.toLowerCase()) {
            case "lowhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.2);
                player.sendMessage(ChatColor.YELLOW + "Set health to 20%");
                break;

            case "criticalhealth":
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.05);
                player.sendMessage(ChatColor.RED + "Set health to 5% - Last Resort should activate!");
                break;

            case "heavyhit":
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                player.damage(maxHealth * 0.5);
                player.sendMessage(ChatColor.YELLOW + "Simulated heavy hit (50% max HP) - check for Reactive Defense!");
                break;

            case "surrounded":
                for (int i = 0; i < 5; i++) {
                    player.getWorld().spawnEntity(
                        player.getLocation().add(Math.random() * 4 - 2, 0, Math.random() * 4 - 2),
                        EntityType.ZOMBIE
                    );
                }
                player.sendMessage(ChatColor.YELLOW + "Spawned 5 zombies - check Proximity Defense!");
                break;

            case "allylow":
                // This would need actual implementation of spawning a test NPC
                player.sendMessage(ChatColor.YELLOW + "This feature requires NPC/fake player implementation");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown scenario: " + scenario);
                showSimulateHelp(player);
                break;
        }
    }

    private ScaleGuardianSkillEffectsHandler getHandler() {
        try {
            return (ScaleGuardianSkillEffectsHandler) plugin.getAscendancySkillEffectIntegrator()
                .getHandler("ScaleGuardian");
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting ScaleGuardianSkillEffectsHandler: " + e.getMessage());
            return null;
        }
    }
}
