package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyCommand implements CommandExecutor, TabCompleter {
    private final MyExperiencePlugin plugin;
    private final PartyManager partyManager;

    public PartyCommand(MyExperiencePlugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use party commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /party <inv|accept|decline|leave|info>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "inv":
                if (args.length < 2) {
                    player.sendMessage("Usage: /party inv <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage("§cYou cannot invite yourself.");
                    return true;
                }
                partyManager.invitePlayer(player, target);
                break;

            case "accept":
                partyManager.acceptInvite(player);
                break;

            case "decline":
                partyManager.declineInvite(player);
                break;

            case "leave":
                partyManager.leaveParty(player);
                break;
            // Add this inside your switch (subCommand) { ... } block in onCommand method
            case "chat":
            case "c":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party chat <message>");
                    return true;
                }

                // Combine all arguments after "chat" into one message
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (i > 1) message.append(" ");
                    message.append(args[i]);
                }

                partyManager.sendPartyMessage(player, message.toString());
                return true;
            case "info":
                Party party = partyManager.getParty(player);
                if (party == null) {
                    player.sendMessage("§cYou are not in a party.");
                    return true;
                }
                player.sendMessage("§e===== §6[§aParty Info§6] §e=====");
                for (UUID memberId : party.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) {
                        int level = plugin.getPlayerLevel(member);
                        player.sendMessage("§6- §a" + member.getName() + " §7(Level " + level + ")");
                    }
                }
                player.sendMessage("§e==========================");
                break;

            default:
                player.sendMessage("§cUnknown subcommand.");
                break;
        }

        return true;
    }

    // Implement tab completion for commands
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
// Update your subCommands list in onTabComplete method
            List<String> subCommands = Arrays.asList("inv", "accept", "decline", "leave", "info", "chat", "c");
            List<String> completions = new ArrayList<>();
            for (String sc : subCommands) {
                if (sc.startsWith(args[0].toLowerCase())) {
                    completions.add(sc);
                }
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("inv")) {
            List<String> completions = new ArrayList<>();
            String prefix = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
