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
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                showHelp(player);
                break;

            case "create":
                createParty(player);
                break;

            case "inv":
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                invitePlayer(player, args[1]);
                break;

            case "accept":
                partyManager.acceptInvite(player);
                break;

            case "decline":
            case "deny":
                partyManager.declineInvite(player);
                break;

            case "leave":
                partyManager.leaveParty(player);
                break;

            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party kick <player>");
                    return true;
                }
                kickPlayer(player, args[1]);
                break;

            case "leader":
            case "transfer":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party leader <player>");
                    return true;
                }
                transferLeader(player, args[1]);
                break;

            case "disband":
                disbandParty(player);
                break;

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
                break;

            case "info":
            case "list":
                showPartyInfo(player);
                break;

            default:
                player.sendMessage("§cUnknown subcommand. Use /party help for help.");
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§e===== §6[§aParty Commands§6] §e=====");
        player.sendMessage("§6/party create §7- Create a new party");
        player.sendMessage("§6/party invite <player> §7- Invite a player");
        player.sendMessage("§6/party accept §7- Accept party invite");
        player.sendMessage("§6/party decline §7- Decline party invite");
        player.sendMessage("§6/party leave §7- Leave your party");
        player.sendMessage("§6/party kick <player> §7- Kick a player (leader only)");
        player.sendMessage("§6/party leader <player> §7- Transfer leadership");
        player.sendMessage("§6/party disband §7- Disband party (leader only)");
        player.sendMessage("§6/party chat <msg> §7- Send party message");
        player.sendMessage("§6/party info §7- Show party information");
        player.sendMessage("§e==========================");
    }

    private void createParty(Player player) {
        if (partyManager.isInParty(player)) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }

        partyManager.getOrCreateParty(player);
        player.sendMessage("§aParty created! You are now the party leader.");
        player.sendMessage("§7Use §e/party invite <player>§7 to invite others.");
    }

    private void invitePlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot invite yourself.");
            return;
        }

        // Create party if player doesn't have one
        if (!partyManager.isInParty(player)) {
            partyManager.getOrCreateParty(player);
            player.sendMessage("§aParty created!");
        }

        partyManager.invitePlayer(player, target);
    }

    private void kickPlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        partyManager.kickPlayerFromParty(player, target);
    }

    private void transferLeader(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        if (partyManager.transferLeadership(player, target)) {
            player.sendMessage("§aLeadership transferred to " + target.getName() + "!");
        }
    }

    private void disbandParty(Player player) {
        if (partyManager.disbandParty(player)) {
            player.sendMessage("§aParty has been disbanded.");
        }
    }

    private void showPartyInfo(Player player) {
        Party party = partyManager.getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        Player leader = Bukkit.getPlayer(party.getLeader());
        String leaderName = (leader != null) ? leader.getName() : "Unknown";

        player.sendMessage("§e===== §6[§aParty Info§6] §e=====");
        player.sendMessage("§6Leader: §a" + leaderName + (party.isLeader(player.getUniqueId()) ? " §7(You)" : ""));
        player.sendMessage("§6Members: §a" + party.getMembers().size() + "/4");
        player.sendMessage("§6Member List:");

        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                int level = plugin.getPlayerLevel(member);
                String memberInfo = "§6- §a" + member.getName() + " §7(Level " + level + ")";

                if (party.isLeader(memberId)) {
                    memberInfo += " §6[Leader]";
                }
                if (memberId.equals(player.getUniqueId())) {
                    memberInfo += " §7(You)";
                }

                player.sendMessage(memberInfo);
            }
        }
        player.sendMessage("§e==========================");
    }

    // Implement tab completion for commands
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                    "help", "create", "invite", "accept", "decline",
                    "leave", "kick", "leader", "disband", "info", "chat", "c"
            );
            List<String> completions = new ArrayList<>();
            for (String sc : subCommands) {
                if (sc.startsWith(args[0].toLowerCase())) {
                    completions.add(sc);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("invite") || subCommand.equals("inv") ||
                    subCommand.equals("kick") || subCommand.equals("leader") ||
                    subCommand.equals("transfer")) {

                List<String> completions = new ArrayList<>();
                String prefix = args[1].toLowerCase();

                // For kick and leader commands, only show party members
                if (subCommand.equals("kick") || subCommand.equals("leader") || subCommand.equals("transfer")) {
                    Player player = (Player) sender;
                    Party party = partyManager.getParty(player);
                    if (party != null) {
                        for (UUID memberId : party.getMembers()) {
                            if (!memberId.equals(player.getUniqueId())) {
                                Player member = Bukkit.getPlayer(memberId);
                                if (member != null && member.getName().toLowerCase().startsWith(prefix)) {
                                    completions.add(member.getName());
                                }
                            }
                        }
                    }
                } else {
                    // For invite, show all online players
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(prefix)) {
                            completions.add(player.getName());
                        }
                    }
                }
                return completions;
            }
        }

        return Collections.emptyList();
    }
}