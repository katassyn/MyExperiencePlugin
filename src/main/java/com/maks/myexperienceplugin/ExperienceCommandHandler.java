package com.maks.myexperienceplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExperienceCommandHandler implements CommandExecutor {
    private final MyExperiencePlugin plugin;

    public ExperienceCommandHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("get_lvl")) {
            return handleGetLevelCommand(sender, args);
        } else if (label.equalsIgnoreCase("exp_give")) {
            return handleExpGiveCommand(sender, args);
        } else if (label.equalsIgnoreCase("exp_give_p")) {
            return handleExpGivePCommand(sender, args);
        }
        return false;
    }

    private boolean handleGetLevelCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /get_lvl <level>");
            return true;
        }

        try {
            int level = Integer.parseInt(args[0]);
            Player player = (Player) sender;

            int maxLevel = plugin.getMaxLevel();
            if (level < 1 || level > maxLevel) {
                player.sendMessage("§cLevel must be between 1 and " + maxLevel + ".");
                return true;
            }

            UUID playerId = player.getUniqueId();
            plugin.getPlayerLevels().put(playerId, level);
            plugin.getPlayerCurrentXP().put(playerId, 0.0);
            plugin.updatePlayerXPBar(player);
            plugin.getDatabaseManager().savePlayerData(player, level, 0.0);

            player.sendMessage("§aYour level has been set to §6" + level + "§a!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid level. Please enter a valid number.");
        }
        return true;
    }

    private boolean handleExpGiveCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /exp_give <amount> <player>");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[0]);
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            plugin.addXP(target, amount);
            sender.sendMessage("§aYou gave §6" + MyExperiencePlugin.formatNumber(amount) + " XP §ato §6" + target.getName() + "§a!");
            target.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(amount) + " XP§a!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount. Please enter a valid number.");
        }
        return true;
    }

    private boolean handleExpGivePCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /exp_give_p <percentage> <player>");
            return true;
        }

        try {
            double percentage = Double.parseDouble(args[0]);
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            if (percentage < 0 || percentage > 100) {
                sender.sendMessage("§cPercentage must be between 0 and 100.");
                return true;
            }

            UUID targetId = target.getUniqueId();
            int currentLevel = plugin.getPlayerLevels().getOrDefault(targetId, 1);
            double requiredXP = plugin.getXpPerLevel().getOrDefault(currentLevel, Math.pow(currentLevel * 100 + 100, 1.013));
            double xpToGive = (requiredXP * percentage) / 100.0;

            plugin.addXP(target, xpToGive);
            sender.sendMessage("§aYou gave §6" + MyExperiencePlugin.formatNumber(xpToGive) + " XP §a(" + percentage + "%) to §6" + target.getName() + "§a!");
            target.sendMessage("§aYou received §6" + MyExperiencePlugin.formatNumber(xpToGive) + " XP §a(" + percentage + "%)!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid percentage. Please enter a valid number.");
        }
        return true;
    }
}
