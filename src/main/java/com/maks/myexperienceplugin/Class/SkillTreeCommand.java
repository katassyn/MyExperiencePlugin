package com.maks.myexperienceplugin.Class;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillTreeCommand implements CommandExecutor {

    private final MyExperiencePlugin plugin;

    public SkillTreeCommand(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open the Skill Tree.");
            return true;
        }

        Player player = (Player) sender;
        int points = plugin.getClassManager().getPlayerSkillPoints(player.getUniqueId());
        player.sendMessage("§aYou have §e" + points + " §askill points to spend. (GUI coming soon!)");

        // Future expansion: open a skill tree GUI, etc.
        return true;
    }
}
