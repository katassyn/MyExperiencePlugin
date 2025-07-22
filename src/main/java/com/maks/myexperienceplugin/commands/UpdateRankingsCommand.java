package com.maks.myexperienceplugin.commands;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateRankingsCommand implements CommandExecutor {
    
    private final MyExperiencePlugin plugin;
    
    public UpdateRankingsCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myplugin.updaterankings")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Updating player rankings...");
        
        // Run asynchronously to not block the main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            long startTime = System.currentTimeMillis();
            
            // Force update rankings
            plugin.getDatabaseManager().forceUpdatePlayerRankings();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Send completion message on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sender.sendMessage(ChatColor.GREEN + "Player rankings updated successfully!");
                sender.sendMessage(ChatColor.GRAY + "Update took " + duration + "ms");
            });
        });
        
        return true;
    }
}
