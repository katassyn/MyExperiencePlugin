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

    // Get the party of a player
    public Party getParty(Player player) {
        return playerPartyMap.get(player.getUniqueId());
    }

    // Invite a player to a party
    public void invitePlayer(Player inviter, Player invitee) {
        UUID inviterId = inviter.getUniqueId();
        UUID inviteeId = invitee.getUniqueId();

        // Check if inviter is in a party, if not create one
        Party party = getParty(inviter);
        if (party == null) {
            party = new Party(plugin, inviter);
            playerPartyMap.put(inviterId, party);
        }

        // Check if party is full
        if (party.getMembers().size() >= 3) {
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

        if (party.getMembers().size() >= 3) {
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

        // Notify inviter
        if (!inviter.getUniqueId().equals(player.getUniqueId())) {
            inviter.sendMessage("§a" + player.getName() + " has accepted your party invite.");
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
            party.removeMember(player);
            playerPartyMap.remove(playerId);

            // Notify remaining members
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage("§e" + player.getName() + " has left the party.");
                }
            }

            // If party is empty, you can add logic to remove it if needed
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
}
