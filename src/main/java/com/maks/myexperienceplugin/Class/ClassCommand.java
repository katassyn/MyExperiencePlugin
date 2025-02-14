package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ClassCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public ClassCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Must be player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game!");
            return true;
        }
        Player player = (Player) sender;

        // Check OP or permission
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Nie masz uprawnień do tej komendy!");
            return true;
        }

        // /class set <className>
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Użycie: /class set <className>");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            String newClass = args[1];

            // Enforce valid base classes only
            List<String> validClasses = Arrays.asList("ranger", "dragonknight", "spellweaver");
            if (!validClasses.contains(newClass.toLowerCase())) {
                player.sendMessage(ChatColor.RED + "Niepoprawna klasa! Możesz wybrać: Ranger, Dragonknight, Spellweaver.");
                return true;
            }

            plugin.getClassManager().setPlayerClass(player, newClass);
            player.sendMessage(ChatColor.GREEN + "Ustawiono klasę na: " + ChatColor.GOLD + newClass);
        } else {
            player.sendMessage(ChatColor.RED + "Nieznana sub-komenda. Użyj: /class set <className>");
        }

        return true;
    }
}
