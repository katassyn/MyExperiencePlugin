package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForceUpdateSkillPointsCommand implements CommandExecutor, TabCompleter {

    private final MyExperiencePlugin plugin;
    private final SkillTreeManager skillTreeManager;
    private final SkillTreeGUI skillTreeGUI; // Store direct reference

    // Define the debugging flag directly in the class
    private static final int DEBUGGING_FLAG = 1;

    public ForceUpdateSkillPointsCommand(MyExperiencePlugin plugin, SkillTreeManager skillTreeManager, SkillTreeGUI skillTreeGUI) {
        this.plugin = plugin;
        this.skillTreeManager = skillTreeManager;
        this.skillTreeGUI = skillTreeGUI; // Store the reference
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            // Check permission for managing others
            if (sender instanceof Player && !sender.hasPermission("myplugin.skillpoints.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to update other players' skill points.");
                return true;
            }

            // Handle target player
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return true;
            }

            updatePlayerSkillPoints(sender, target);
            return true;
        } else if (sender instanceof Player) {
            // Self-update
            updatePlayerSkillPoints(sender, (Player) sender);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
            return true;
        }
    }

    private void updatePlayerSkillPoints(CommandSender sender, Player target) {
        int level = plugin.getPlayerLevel(target);

        // Log before state
        if (DEBUGGING_FLAG == 1) {
            int basic = skillTreeManager.getBasicSkillPoints(target.getUniqueId());
            int ascendancy = skillTreeManager.getAscendancySkillPoints(target.getUniqueId());

            plugin.getLogger().info("BEFORE update - Player " + target.getName() +
                    " (level " + level + "): basic=" + basic +
                    ", ascendancy=" + ascendancy);
        }

        // Force recalculation
        skillTreeManager.updateSkillPoints(target);

        // Delay to ensure database operation completes
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Log after state
            if (DEBUGGING_FLAG == 1) {
                int basic = skillTreeManager.getBasicSkillPoints(target.getUniqueId());
                int ascendancy = skillTreeManager.getAscendancySkillPoints(target.getUniqueId());

                plugin.getLogger().info("AFTER update - Player " + target.getName() +
                        " (level " + level + "): basic=" + basic +
                        ", ascendancy=" + ascendancy);
            }

            sender.sendMessage(ChatColor.GREEN + "Updated skill points for " + target.getName() +
                    " (level " + level + ")");

            if (sender != target) {
                target.sendMessage(ChatColor.GREEN + "Your skill points have been updated by an admin.");
            }

            // Refresh skill tree GUI if open
            if (target.getOpenInventory() != null &&
                    target.getOpenInventory().getTitle().contains("Skill Tree")) {
                skillTreeGUI.openSkillTreeGUI(target); // Use direct reference
            }
        }, 20L); // 1 second delay
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}