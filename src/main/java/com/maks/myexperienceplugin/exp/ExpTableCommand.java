package com.maks.myexperienceplugin.exp;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpTableCommand implements CommandExecutor, TabCompleter {

    private final MyExperiencePlugin plugin;

    public ExpTableCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.loadExpTable();
            sender.sendMessage("Experience table reloaded!");
            plugin.getLogger().info("Experience table reloaded by " + sender.getName());  // Logowanie informacji o przeładowaniu
            return true;
        }

        sender.sendMessage("Usage: /exp_table reload");
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            return completions;
        }
        return Collections.emptyList();
    }
}
