package com.maks.myexperienceplugin.alchemy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Map;

/**
 * Command to display current alchemy cooldowns for a player
 */
public class AlchemyCooldownCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Map<AlchemyManager.AlchemyCategory, Long> cooldowns = AlchemyManager.getInstance().getPlayerCooldowns(player);
        
        if (cooldowns == null || cooldowns.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "You have no active alchemy cooldowns.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Your Alchemy Cooldowns ===");
        long currentTime = System.currentTimeMillis();
        
        for (AlchemyManager.AlchemyCategory category : AlchemyManager.AlchemyCategory.values()) {
            Long cooldownEndTime = cooldowns.get(category);
            
            if (cooldownEndTime != null) {
                long remainingSeconds = Math.max(0, (cooldownEndTime - currentTime) / 1000);
                
                if (remainingSeconds > 0) {
                    String categoryName = formatCategoryName(category);
                    player.sendMessage(ChatColor.YELLOW + categoryName + ": " + 
                                      ChatColor.WHITE + formatTime(remainingSeconds));
                }
            }
        }
        
        return true;
    }
    
    /**
     * Format the category name for display
     */
    private String formatCategoryName(AlchemyManager.AlchemyCategory category) {
        switch (category) {
            case ELIXIR:
                return "Potions";
            case TONIC:
                return "Tonics";
            case PHYCIS:
                return "Phycis";
            case TOTEM:
                return "Totems";
            default:
                return category.name();
        }
    }
    
    /**
     * Format time in seconds to a readable format
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        
        if (minutes < 60) {
            return minutes + " minutes, " + remainingSeconds + " seconds";
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        return hours + " hours, " + remainingMinutes + " minutes, " + remainingSeconds + " seconds";
    }
}