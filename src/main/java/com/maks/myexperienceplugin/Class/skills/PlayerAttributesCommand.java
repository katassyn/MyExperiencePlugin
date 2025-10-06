package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.protection.ProtectionScalingService;
import com.maks.myexperienceplugin.protection.ProtectionScalingService.ProtectionScalingResult;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerAttributesCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;
    private final SkillEffectsHandler skillEffectsHandler;
    private final ProtectionScalingService protectionScalingService;

    public PlayerAttributesCommand(MyExperiencePlugin plugin,
                                  SkillEffectsHandler skillEffectsHandler,
                                  ProtectionScalingService protectionScalingService) {
        this.plugin = plugin;
        this.skillEffectsHandler = skillEffectsHandler;
        this.protectionScalingService = protectionScalingService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can check attributes.");
            return true;
        }

        Player player = (Player) sender;

        // Display vanilla attributes
        player.sendMessage(ChatColor.GOLD + "=== " + ChatColor.GREEN + "Player Attributes" + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + plugin.getPlayerLevel(player));

        // Display all attributes
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance attrib = player.getAttribute(attribute);
            if (attrib != null) {
                String formattedValue = String.format("%.2f", attrib.getValue());
                String formattedBase = String.format("%.2f", attrib.getBaseValue());

                if (attrib.getValue() != attrib.getBaseValue()) {
                    player.sendMessage(ChatColor.YELLOW + formatAttributeName(attribute) + ": " +
                            ChatColor.WHITE + formattedValue + ChatColor.GRAY + " (base: " + formattedBase + ")");
                } else {
                    player.sendMessage(ChatColor.YELLOW + formatAttributeName(attribute) + ": " +
                            ChatColor.WHITE + formattedValue);
                }
            }
        }

        // Display skill bonuses
        SkillEffectsHandler.PlayerSkillStats stats = skillEffectsHandler.getPlayerStats(player);
        player.sendMessage(ChatColor.GOLD + "--- " + ChatColor.GREEN + "Skill Bonuses" + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Class: " + ChatColor.WHITE + plugin.getClassManager().getPlayerClass(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Ascendancy: " + ChatColor.WHITE +
                (plugin.getClassManager().getPlayerAscendancy(player.getUniqueId()).isEmpty() ?
                        "None" : plugin.getClassManager().getPlayerAscendancy(player.getUniqueId())));
        player.sendMessage(ChatColor.YELLOW + "Basic Points: " + ChatColor.WHITE + plugin.getSkillTreeManager().getBasicSkillPoints(player.getUniqueId()) +
                ChatColor.GRAY + " (unused: " + plugin.getSkillTreeManager().getUnusedBasicSkillPoints(player.getUniqueId()) + ")");
        player.sendMessage(ChatColor.YELLOW + "Ascendancy Points: " + ChatColor.WHITE + plugin.getSkillTreeManager().getAscendancySkillPoints(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Purchased Skills: " + ChatColor.WHITE + plugin.getSkillTreeManager().getPurchasedSkills(player.getUniqueId()).size());

        player.sendMessage(ChatColor.GOLD + "--- " + ChatColor.GREEN + "Skill Stats" + ChatColor.GOLD + " ---");
        player.sendMessage(ChatColor.YELLOW + "Bonus Damage: " + ChatColor.WHITE + stats.getBonusDamage());
        player.sendMessage(ChatColor.YELLOW + "Damage Multiplier: " + ChatColor.WHITE + String.format("%.2f", stats.getDamageMultiplier()) + "x");
        player.sendMessage(ChatColor.YELLOW + "Evade Chance: " + ChatColor.WHITE + stats.getEvadeChance() + "%");
        player.sendMessage(ChatColor.YELLOW + "Shield Block Chance: " + ChatColor.WHITE + stats.getShieldBlockChance() + "%");
        player.sendMessage(ChatColor.YELLOW + "Defense Bonus: " + ChatColor.WHITE + stats.getDefenseBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Max Health Bonus: " + ChatColor.WHITE + stats.getMaxHealthBonus() + " HP");
        player.sendMessage(ChatColor.YELLOW + "Movement Speed Bonus: " + ChatColor.WHITE + stats.getMovementSpeedBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Luck Bonus: " + ChatColor.WHITE + stats.getLuckBonus() + "%");
        player.sendMessage(ChatColor.YELLOW + "Gold per Kill: " + ChatColor.WHITE + stats.getGoldPerKill() + "$");

        ProtectionScalingResult protectionStats = protectionScalingService.calculateFor(player);
        double displayReduction = protectionScalingService.isEnabled()
                ? protectionStats.finalReduction()
                : protectionStats.vanillaReduction();
        player.sendMessage(ChatColor.YELLOW + "Protection Reduction: " + ChatColor.WHITE +
                formatPercent(displayReduction * 100.0) +
                ChatColor.GRAY + " (vanilla: " + formatPercent(protectionStats.vanillaReduction() * 100.0) + ")");

        if (protectionStats.hasProtection()) {
            player.sendMessage(ChatColor.YELLOW + "Total Protection Levels: " + ChatColor.WHITE +
                    protectionStats.totalProtectionLevels() +
                    ChatColor.GRAY + " (effective: " + protectionStats.clampedProtectionLevels() + ")");
        }

        return true;
    }

    private String formatAttributeName(Attribute attribute) {
        String name = attribute.name().replace("GENERIC_", "");
        String[] parts = name.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            formatted.append(part.charAt(0)).append(part.substring(1).toLowerCase()).append(" ");
        }

        return formatted.toString().trim();
    }

    private String formatPercent(double value) {
        return String.format("%.2f%%", value);
    }
}
