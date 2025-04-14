package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.Class.skills.SkillTreeGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillTreeCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;
    private final SkillTreeGUI skillTreeGUI;

    public SkillTreeCommand(MyExperiencePlugin plugin, SkillTreeGUI skillTreeGUI) {
        this.plugin = plugin;
        this.skillTreeGUI = skillTreeGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the Skill Tree.");
            return true;
        }

        Player player = (Player) sender;
        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            player.sendMessage(ChatColor.RED + "You need to choose a class first!");
            return true;
        }

        // Open the skill tree GUI
        skillTreeGUI.openSkillTreeGUI(player);

        return true;
    }
}