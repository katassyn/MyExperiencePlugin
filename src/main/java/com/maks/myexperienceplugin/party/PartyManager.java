// ===== PartyManager.java =====
package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.*;

public class PartyManager {
    private final MyExperiencePlugin plugin;

    // Maps to keep track of parties and invites
    private final Map<UUID, Party> playerPartyMap = new HashMap<>();
    private final Map<UUID, PartyInvite> pendingInvites = new HashMap<>();
    private final Set<Party> allParties = new HashSet<>(); // Track all parties for cleanup

    // Cooldown for sending invites (in milliseconds)
    private final long inviteCooldown = 60 * 1000; // 60 seconds
    // Duration for which an invite is valid (in milliseconds)
    private final long inviteDuration = 10 * 1000; // 10 seconds

    // Cooldowns for each player
    private final Map<UUID, Long> inviteCooldowns = new HashMap<>();

    public PartyManager(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    // Check if player is in a party
    public boolean isInParty(Player player) {
        return playerPartyMap.containsKey(player.getUniqueId());
    }

    // Create or get a party for a player (allows single-player parties)
    public Party getOrCreateParty(Player player) {
        Party party = getParty(player);
        if (party == null) {
            party = new Party(plugin, player);
            playerPartyMap.put(player.getUniqueId(), party);
            allParties.add(party);
        }
        return party;
    }

    // Get the party of a player
    public Party getParty(Player player) {
        return playerPartyMap.get(player.getUniqueId());
    }

    // Check if a player is the party leader
    public boolean isPartyLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.isLeader(player.getUniqueId());
    }

    // Invite a player to a party
    public void invitePlayer(Player inviter, Player invitee) {
        UUID inviterId = inviter.getUniqueId();
        UUID inviteeId = invitee.getUniqueId();

        // Get or create party for inviter
        Party party = getOrCreateParty(inviter);

        // Only the party leader can invite
        if (!party.isLeader(inviterId)) {
            inviter.sendMessage("§cOnly the party leader can invite players.");
            return;
        }

        // Check if invitee is already in a party
        if (isInParty(invitee)) {
            inviter.sendMessage("§c" + invitee.getName() + " is already in a party.");
            return;
        }

        // Check if party is full
        if (party.getMembers().size() >= 4) {
            inviter.sendMessage("§cYour party is full.");
            return;
        }

        // Check cooldown
        long lastInviteTime = inviteCooldowns.getOrDefault(inviteeId, 0L);
        if (System.currentTimeMillis() - lastInviteTime < inviteCooldown) {
            inviter.sendMessage("§cYou must wait before inviting this player again.");
            return;
        }

        // Send invite
        PartyInvite invite = new PartyInvite(inviter, inviteDuration);
        pendingInvites.put(inviteeId, invite);
        inviteCooldowns.put(inviteeId, System.currentTimeMillis());

        // Create clickable accept message
        TextComponent message = new TextComponent("§a" + inviter.getName() + " has invited you to join their party. ");
        TextComponent accept = new TextComponent("§b[ACCEPT]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept the party invite")));

        message.addExtra(accept);

        invitee.spigot().sendMessage(message);

        inviter.sendMessage("§aYou have invited §e" + invitee.getName() + "§a to your party.");

        // Schedule invite expiration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PartyInvite pendingInvite = pendingInvites.get(inviteeId);
            if (pendingInvite != null && pendingInvite == invite) {
                pendingInvites.remove(inviteeId);
                inviter.sendMessage("§e" + invitee.getName() + " did not accept your party invite in time.");
            }
        }, inviteDuration / 50); // Convert milliseconds to ticks
    }

    // Accept an invite
    public void acceptInvite(Player player) {
        UUID playerId = player.getUniqueId();
        PartyInvite invite = pendingInvites.remove(playerId);

        if (invite == null || invite.isExpired()) {
            player.sendMessage("§cYou have no pending party invites.");
            return;
        }

        Player inviter = invite.getInviter();
        Party party = getParty(inviter);
        if (party == null) {
            player.sendMessage("§cThe party you were invited to no longer exists.");
            return;
        }

        if (party.getMembers().size() >= 4) {
            player.sendMessage("§cThe party is full.");
            return;
        }

        party.addMember(player);
        playerPartyMap.put(playerId, party);

        // Notify party members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                if (!memberId.equals(playerId)) {
                    member.sendMessage("§a" + player.getName() + " has joined the party.");
                } else {
                    member.sendMessage("§aYou have joined the party.");
                }
            }
        }
    }

    // Decline an invite
    public void declineInvite(Player player) {
        UUID playerId = player.getUniqueId();
        PartyInvite invite = pendingInvites.remove(playerId);

        if (invite == null || invite.isExpired()) {
            player.sendMessage("§cYou have no pending party invites.");
            return;
        }

        Player inviter = invite.getInviter();
        inviter.sendMessage("§e" + player.getName() + " has declined your party invite.");
        player.sendMessage("§eYou have declined the party invite from " + inviter.getName() + ".");
    }

    // Remove a player from their party
    public void removePlayerFromParty(Player player) {
        UUID playerId = player.getUniqueId();
        Party party = getParty(player);

        if (party != null) {
            boolean wasLeader = party.isLeader(playerId);
            party.removeMember(player);
            playerPartyMap.remove(playerId);

            // Check if party is empty and remove it
            if (party.isEmpty()) {
                allParties.remove(party);
            } else {
                // Notify remaining members
                for (UUID memberId : party.getMembers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) {
                        member.sendMessage("§e" + player.getName() + " has left the party.");

                        // Notify about new leader if leadership changed
                        if (wasLeader && party.isLeader(memberId)) {
                            member.sendMessage("§6You are now the party leader!");
                        } else if (wasLeader) {
                            Player newLeader = Bukkit.getPlayer(party.getLeader());
                            if (newLeader != null) {
                                member.sendMessage("§6" + newLeader.getName() + " is now the party leader.");
                            }
                        }
                    }
                }
            }
        }
    }

    // Handle /party leave command
    public void leaveParty(Player player) {
        if (!isInParty(player)) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }
        removePlayerFromParty(player);
        player.sendMessage("§aYou have left the party.");
    }

    /**
     * Sends a message to all members of a player's party
     */
    public void sendPartyMessage(Player sender, String message) {
        Party party = getParty(sender);
        if (party == null) {
            sender.sendMessage("§cYou are not in a party.");
            return;
        }

        String formattedMessage = "§9[Party] §7" + sender.getName() + ": §f" + message;
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    // Kick a player from party (only for party leader)
    public boolean kickPlayerFromParty(Player kicker, Player target) {
        Party kickerParty = getParty(kicker);
        Party targetParty = getParty(target);

        if (kickerParty == null || targetParty == null || !kickerParty.equals(targetParty)) {
            kicker.sendMessage("§cYou cannot kick this player.");
            return false;
        }

        // Only the party leader can kick
        if (!kickerParty.isLeader(kicker.getUniqueId())) {
            kicker.sendMessage("§cOnly the party leader can kick players.");
            return false;
        }

        if (kicker.equals(target)) {
            kicker.sendMessage("§cYou cannot kick yourself. Use /party leave instead.");
            return false;
        }

        removePlayerFromParty(target);
        kicker.sendMessage("§aYou have kicked " + target.getName() + " from the party.");
        target.sendMessage("§cYou have been kicked from the party by " + kicker.getName() + ".");

        // Notify other party members
        for (UUID memberId : kickerParty.getMembers()) {
            if (!memberId.equals(kicker.getUniqueId())) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§e" + target.getName() + " has been kicked from the party.");
                }
            }
        }

        return true;
    }

    // Transfer party leadership
    public boolean transferLeadership(Player currentLeader, Player newLeader) {
        Party party = getParty(currentLeader);

        if (party == null || !party.isLeader(currentLeader.getUniqueId())) {
            currentLeader.sendMessage("§cYou are not the party leader.");
            return false;
        }

        if (!party.getMembers().contains(newLeader.getUniqueId())) {
            currentLeader.sendMessage("§c" + newLeader.getName() + " is not in your party.");
            return false;
        }

        party.setLeader(newLeader.getUniqueId());

        // Notify all party members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                if (member.equals(newLeader)) {
                    member.sendMessage("§6You are now the party leader!");
                } else {
                    member.sendMessage("§6" + newLeader.getName() + " is now the party leader.");
                }
            }
        }

        return true;
    }

    // Get all online players not in any party (for invitation GUI)
    public List<Player> getPlayersNotInParty() {
        List<Player> availablePlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isInParty(player)) {
                availablePlayers.add(player);
            }
        }
        return availablePlayers;
    }

    // Disband a party (leader only)
    public boolean disbandParty(Player leader) {
        Party party = getParty(leader);

        if (party == null || !party.isLeader(leader.getUniqueId())) {
            leader.sendMessage("§cYou are not the party leader.");
            return false;
        }

        // Notify all members and remove them
        Set<UUID> members = new HashSet<>(party.getMembers());
        for (UUID memberId : members) {
            playerPartyMap.remove(memberId);
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage("§cThe party has been disbanded by the leader.");
            }
        }

        // Remove the party from tracking
        allParties.remove(party);

        return true;
    }
}