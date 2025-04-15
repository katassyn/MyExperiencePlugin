package com.maks.myexperienceplugin.Class.skills;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.gui.AscendancySkillTreeGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AscendancySkillTreeCommand implements CommandExecutor, TabCompleter {

    private final MyExperiencePlugin plugin;
    private final AscendancySkillTreeGUI ascendancySkillTreeGUI;

    // Debugging flag
    private final int debuggingFlag = 1;

    public AscendancySkillTreeCommand(MyExperiencePlugin plugin, AscendancySkillTreeGUI ascendancySkillTreeGUI) {
        this.plugin = plugin;
        this.ascendancySkillTreeGUI = ascendancySkillTreeGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the Ascendancy Skill Tree.");
            return true;
        }

        Player player = (Player) sender;
        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String playerAscendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            player.sendMessage(ChatColor.RED + "You need to choose a class first!");
            return true;
        }

        if (playerAscendancy == null || playerAscendancy.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You need to choose an ascendancy first!");
            return true;
        }

        // Check if player's level is at least 20
        int playerLevel = plugin.getPlayerLevel(player);
        if (playerLevel < 20) {
            player.sendMessage(ChatColor.RED + "You need to be at least level 20 to access the ascendancy skill tree!");
            return true;
        }

        // Determine which branch to open (1, 2, or 3)
        int branch = 1;
        if (args.length > 0) {
            try {
                branch = Integer.parseInt(args[0]);
                if (branch < 1 || branch > 3) {
                    branch = 1;
                }
            } catch (NumberFormatException e) {
                if (debuggingFlag == 1) {
                    plugin.getLogger().warning("Invalid branch number: " + args[0]);
                }
            }
        }

        // Open ascendancy skill tree GUI for the selected branch
        ascendancySkillTreeGUI.openAscendancySkillTreeGUI(player, branch);

        if (debuggingFlag == 1) {
            plugin.getLogger().info("Opened ascendancy skill tree GUI for " + player.getName() +
                    ", class: " + playerClass + ", ascendancy: " + playerAscendancy +
                    ", branch: " + branch);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> branchOptions = Arrays.asList("1", "2", "3");

            if (args[0].isEmpty()) {
                return branchOptions;
            }

            return branchOptions.stream()
                    .filter(option -> option.startsWith(args[0]))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}