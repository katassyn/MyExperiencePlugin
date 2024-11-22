package com.maks.myexperienceplugin;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BonusExpCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public BonusExpCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myplugin.bonusexp")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /bonus_exp <enable|disable|set> [value]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "enable":
                plugin.getConfig().set("Bonus_exp.Enabled", true);
                plugin.saveConfig();
                sender.sendMessage("§aBonus XP event has been enabled!");
                break;

            case "disable":
                plugin.getConfig().set("Bonus_exp.Enabled", false);
                plugin.saveConfig();
                sender.sendMessage("§cBonus XP event has been disabled!");
                break;

            case "set":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /bonus_exp set <value>");
                    return true;
                }
                try {
                    double value = Double.parseDouble(args[1]);
                    plugin.getConfig().set("Bonus_exp.Value", value);
                    plugin.saveConfig();
                    sender.sendMessage("§aBonus XP multiplier has been set to " + value + "%!");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid value! Please enter a number.");
                }
                break;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /bonus_exp <enable|disable|set> [value]");
                break;
        }

        return true;
    }
}
