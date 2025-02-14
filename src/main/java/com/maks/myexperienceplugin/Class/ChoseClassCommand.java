package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ChoseClassCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public ChoseClassCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // If run by CONSOLE
        if (sender instanceof ConsoleCommandSender) {
            // We expect 1 argument: playerName
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /chose_class <playerName> (from console)");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Could not find an online player named: " + args[0]);
                return true;
            }

            // Now open GUI for that target
            sender.sendMessage(ChatColor.GREEN + "Attempting to open the base class GUI for " + target.getName());
            plugin.getClassGUI().openBaseClassGUI(target);
            return true;
        }

        // If run by a PLAYER
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // If the player typed "/chose_class <someone>", we try to open GUI for that <someone>
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Could not find an online player named: " + args[0]);
                    return true;
                }
                if (!player.isOp() && !player.hasPermission("myplugin.choseclass.others")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to open the GUI for other players!");
                    return true;
                }
                player.sendMessage(ChatColor.GREEN + "Attempting to open the base class GUI for " + target.getName());
                plugin.getClassGUI().openBaseClassGUI(target);
                return true;
            }

            // Otherwise, no arguments => open GUI for the sender
            player.sendMessage(ChatColor.GOLD + "Attempting to open the base class selection GUI for you...");
            plugin.getClassGUI().openBaseClassGUI(player);
            return true;
        }

        // If it's neither console nor a player (e.g., Command Block?), handle that if you wish
        sender.sendMessage(ChatColor.RED + "This command can only be used by console or players!");
        return true;
    }
}
