package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillTree2Command implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public SkillTree2Command(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the Ascendancy Skill Tree.");
            return true;
        }

        Player player = (Player) sender;
        String playerClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        String playerAscendancy = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());

        if ("NoClass".equalsIgnoreCase(playerClass)) {
            player.sendMessage(ChatColor.RED + "You need to choose a class first!");
            return true;
        }

        if (playerAscendancy == null || playerAscendancy.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You need to choose an ascendancy first!");
            return true;
        }

        // Check if player's level is at least 20
        int playerLevel = plugin.getPlayerLevel(player);
        if (playerLevel < 20) {
            player.sendMessage(ChatColor.RED + "You need to be at least level 20 to access the ascendancy skill tree!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Ascendancy skill trees are coming soon!");
        // TODO: When ascendancy skill trees are implemented
        // Uncomment this line: skillTreeGUI.openAscendancySkillTreeGUI(player, playerAscendancy);

        return true;
    }
}
