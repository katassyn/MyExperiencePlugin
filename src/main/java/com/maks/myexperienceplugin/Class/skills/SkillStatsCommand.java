package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillStatsCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;
    private final SkillEffectsHandler skillEffectsHandler;

    public SkillStatsCommand(MyExperiencePlugin plugin, SkillEffectsHandler skillEffectsHandler) {
        this.plugin = plugin;
        this.skillEffectsHandler = skillEffectsHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can check skill stats.");
            return true;
        }

        Player player = (Player) sender;

        // Get player's skill stats
        SkillEffectsHandler.PlayerSkillStats stats = skillEffectsHandler.getPlayerStats(player);

        // Display the stats
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.GREEN + "Skill Stats" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "Class: " + ChatColor.WHITE + plugin.getClassManager().getPlayerClass(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Basic Points: " + ChatColor.WHITE + plugin.getSkillTreeManager().getBasicSkillPoints(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Unused Basic Points: " + ChatColor.WHITE + plugin.getSkillTreeManager().getUnusedBasicSkillPoints(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Ascendancy Points: " + ChatColor.WHITE + plugin.getSkillTreeManager().getAscendancySkillPoints(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Purchased Skills: " + ChatColor.WHITE + plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId()).size());
        player.sendMessage(ChatColor.GOLD + "--- " + ChatColor.GREEN + "Stat Bonuses" + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Bonus Damage: " + ChatColor.WHITE + stats.getBonusDamage());
        player.sendMessage(ChatColor.YELLOW + "Damage Multiplier: " + ChatColor.WHITE + (stats.getDamageMultiplier() * 100 - 100) + "%");
        player.sendMessage(ChatColor.YELLOW + "Evade Chance: " + ChatColor.WHITE + stats.getEvadeChance() + "%");
        player.sendMessage(ChatColor.YELLOW + "Shield Block Chance: " + ChatColor.WHITE + stats.getShieldBlockChance() + "%");
        player.sendMessage(ChatColor.YELLOW + "Defense Bonus: " + ChatColor.WHITE + stats.getDefenseBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Max Health Bonus: " + ChatColor.WHITE + stats.getMaxHealthBonus() + " HP");
        player.sendMessage(ChatColor.YELLOW + "Movement Speed Bonus: " + ChatColor.WHITE + stats.getMovementSpeedBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Luck Bonus: " + ChatColor.WHITE + stats.getLuckBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Gold per Kill: " + ChatColor.WHITE + stats.getGoldPerKill() + "$");

        // Force recalculate stats
        skillEffectsHandler.refreshPlayerStats(player);
        player.sendMessage(ChatColor.GREEN + "Stats have been recalculated!");

        return true;
    }
}
