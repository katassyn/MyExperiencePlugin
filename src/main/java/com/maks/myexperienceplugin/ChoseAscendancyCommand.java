package com.maks.myexperienceplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChoseAscendancyCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public ChoseAscendancyCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Must be a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game!");
            return true;
        }
        Player player = (Player) sender;

        // Grab player's base class + ascendancy
        String baseClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String ascend = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        if ("NoClass".equalsIgnoreCase(baseClass)) {
            player.sendMessage(ChatColor.RED + "You must choose a base class first!");
            return true;
        }
        if (!ascend.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You already have an ascendancy: " + ascend);
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Opening Ascendancy Selection GUI for your base class: " + baseClass);
        plugin.getClassGUI().openAscendancyGUI(player, baseClass);
        return true;
    }
}
