// ===== Party.java =====
package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private UUID leader;
    private final Set<UUID> members = new LinkedHashSet<>(); // LinkedHashSet maintains insertion order
    private final MyExperiencePlugin plugin;

    public Party(MyExperiencePlugin plugin, Player leader) {
        this.plugin = plugin;
        this.leader = leader.getUniqueId();
        members.add(leader.getUniqueId());
    }

    // Add a member to the party
    public boolean addMember(Player player) {
        if (members.size() >= 4) { // Changed from 3 to 4 for max party size
            return false; // Party is full
        }
        members.add(player.getUniqueId());
        return true;
    }

    // Remove a member from the party
    public void removeMember(Player player) {
        UUID playerId = player.getUniqueId();
        members.remove(playerId);

        // If the leader left, transfer leadership to the next member
        if (playerId.equals(leader) && !members.isEmpty()) {
            leader = members.iterator().next();
        }
    }

    // Get all members
    public Set<UUID> getMembers() {
        return new LinkedHashSet<>(members); // Return a copy to prevent external modification
    }

    // Get the party leader
    public UUID getLeader() {
        return leader;
    }

    // Set a new party leader
    public void setLeader(UUID newLeader) {
        if (members.contains(newLeader)) {
            this.leader = newLeader;
        }
    }

    // Check if a player is the party leader
    public boolean isLeader(UUID playerId) {
        return playerId.equals(leader);
    }

    // Check if the party is empty
    public boolean isEmpty() {
        return members.isEmpty();
    }
}