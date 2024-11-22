package com.maks.myexperienceplugin;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadBonusCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public ReloadBonusCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("myplugin.reloadbonus")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        plugin.reloadConfig(); // Przeładuj plik config.yml
        sender.sendMessage("§aBonus XP configuration reloaded!");
        return true;
    }
}
