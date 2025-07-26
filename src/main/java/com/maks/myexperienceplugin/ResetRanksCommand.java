package com.maks.myexperienceplugin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResetRanksCommand implements CommandExecutor, TabCompleter {
    private final MyExperiencePlugin plugin;
    private final LuckPerms luckPerms;
    private final int debuggingFlag = 0; // Set to 1 for debugging, 0 for production

    public ResetRanksCommand(MyExperiencePlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /resetranks <all|premium|deluxe> [player]");
            return true;
        }

        String rankType = args[0].toLowerCase();
        if (!rankType.equals("all") && !rankType.equals("premium") && !rankType.equals("deluxe")) {
            sender.sendMessage("§cInvalid rank type. Use 'all', 'premium', or 'deluxe'.");
            return true;
        }

        Player targetPlayer;
        if (args.length > 1) {
            // Check if sender has permission to reset ranks for other players
            if (!sender.hasPermission("myplugin.resetranks.others")) {
                sender.sendMessage("§cYou don't have permission to reset ranks for other players.");
                return true;
            }
            
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer not found: " + args[1]);
                return true;
            }
        } else {
            // If no player is specified, the sender must be a player
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player when running this command from console.");
                return true;
            }
            targetPlayer = (Player) sender;
        }

        // Reset the specified ranks
        resetRanks(targetPlayer, rankType, sender);
        return true;
    }

    private void resetRanks(Player player, String rankType, CommandSender sender) {
        try {
            // Get user from LuckPerms
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                sender.sendMessage("§cCould not find LuckPerms user for: " + player.getName());
                return;
            }

            boolean changes = false;

            // Remove specified ranks
            if (rankType.equals("all") || rankType.equals("premium")) {
                changes |= removeRank(user, "premium");
            }
            
            if (rankType.equals("all") || rankType.equals("deluxe")) {
                changes |= removeRank(user, "deluxe");
            }

            // Save changes if any were made
            if (changes) {
                luckPerms.getUserManager().saveUser(user);
                
                if (sender.equals(player)) {
                    sender.sendMessage("§aYour " + formatRankType(rankType) + " rank(s) have been reset.");
                } else {
                    sender.sendMessage("§a" + player.getName() + "'s " + formatRankType(rankType) + " rank(s) have been reset.");
                    player.sendMessage("§aYour " + formatRankType(rankType) + " rank(s) have been reset by an administrator.");
                }
                
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[ResetRanks] " + player.getName() + "'s " + rankType + " rank(s) were reset by " + sender.getName());
                }
            } else {
                if (sender.equals(player)) {
                    sender.sendMessage("§cYou don't have the specified rank(s) to reset.");
                } else {
                    sender.sendMessage("§c" + player.getName() + " doesn't have the specified rank(s) to reset.");
                }
            }
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred while resetting ranks: " + e.getMessage());
            if (debuggingFlag == 1) {
                Bukkit.getLogger().warning("[ResetRanks] Failed to reset ranks for " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean removeRank(User user, String group) {
        boolean hadRank = false;
        
        // Check if user has the rank before attempting to remove it
        if (user.getNodes(NodeType.INHERITANCE).stream()
                .anyMatch(node -> node.getGroupName().equalsIgnoreCase(group))) {
            
            // Remove all inheritance nodes for this group
            user.data().clear(NodeType.INHERITANCE.predicate(node -> 
                    node.getGroupName().equalsIgnoreCase(group)));
            
            hadRank = true;
        }
        
        return hadRank;
    }

    private String formatRankType(String rankType) {
        switch (rankType) {
            case "premium":
                return "§6Premium§a";
            case "deluxe":
                return "§4Deluxe§a";
            case "all":
                return "§6Premium§a and §4Deluxe§a";
            default:
                return rankType;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> rankTypes = Arrays.asList("all", "premium", "deluxe");
            return rankTypes.stream()
                    .filter(type -> type.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && sender.hasPermission("myplugin.resetranks.others")) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
