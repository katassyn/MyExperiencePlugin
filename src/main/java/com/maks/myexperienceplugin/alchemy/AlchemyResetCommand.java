package com.maks.myexperienceplugin.alchemy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlchemyResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("myplugin.alchemy.reset")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        // Handle different command cases
        if (args.length == 0) {
            // If no arguments, reset for the sender if it's a player
            if (sender instanceof Player) {
                resetForPlayer((Player) sender);
                sender.sendMessage("§aYour alchemy effects and cooldowns have been reset!");
            } else {
                sender.sendMessage("§cUsage: /alchemy_reset [player]");
            }
            return true;
        } else {
            // Reset for the specified player
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + targetName);
                return true;
            }

            resetForPlayer(target);
            sender.sendMessage("§aAlchemy effects and cooldowns have been reset for " + target.getName() + "!");
            target.sendMessage("§aYour alchemy effects and cooldowns have been reset by " + sender.getName() + "!");
            return true;
        }
    }

    private void resetForPlayer(Player player) {
        AlchemyManager manager = AlchemyManager.getInstance();

        // Remove all active effects
        for (AlchemyManager.AlchemyCategory category : AlchemyManager.AlchemyCategory.values()) {
            manager.removeEffect(player, category);
        }

        // Clear cooldowns for the player
        manager.clearCooldowns(player);
    }
}