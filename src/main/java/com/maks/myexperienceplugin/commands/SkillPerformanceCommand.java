package com.maks.myexperienceplugin.commands;

import com.maks.myexperienceplugin.Class.skills.effects.BaseSkillEffectsHandler;
import com.maks.myexperienceplugin.Class.skills.effects.PerformanceMonitor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

/**
 * Command to display skill system performance statistics
 */
public class SkillPerformanceCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myexperienceplugin.performance")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            displayPerformanceReport(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "report":
            case "stats":
                displayPerformanceReport(sender);
                break;
                
            case "reset":
                resetPerformanceStats(sender);
                break;
                
            case "help":
            default:
                displayHelp(sender);
                break;
        }
        
        return true;
    }

    private void displayPerformanceReport(CommandSender sender) {
        PerformanceMonitor monitor = BaseSkillEffectsHandler.getPerformanceMonitor();
        if (monitor == null) {
            sender.sendMessage(ChatColor.RED + "Performance monitor not initialized.");
            return;
        }
        
        monitor.displayReport(sender);
    }

    private void resetPerformanceStats(CommandSender sender) {
        PerformanceMonitor monitor = BaseSkillEffectsHandler.getPerformanceMonitor();
        if (monitor == null) {
            sender.sendMessage(ChatColor.RED + "Performance monitor not initialized.");
            return;
        }
        
        monitor.reset();
        sender.sendMessage(ChatColor.GREEN + "Performance statistics reset.");
    }

    private void displayHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Skill Performance Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/skillperf report" + ChatColor.WHITE + " - Display performance statistics");
        sender.sendMessage(ChatColor.YELLOW + "/skillperf stats" + ChatColor.WHITE + " - Same as report");
        sender.sendMessage(ChatColor.YELLOW + "/skillperf reset" + ChatColor.WHITE + " - Reset all performance statistics");
        sender.sendMessage(ChatColor.YELLOW + "/skillperf help" + ChatColor.WHITE + " - Show this help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("report", "stats", "reset", "help");
        }
        return null;
    }
}