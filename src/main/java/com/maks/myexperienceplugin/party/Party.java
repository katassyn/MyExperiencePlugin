package com.maks.myexperienceplugin.party;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final Set<UUID> members = new HashSet<>();
    private final MyExperiencePlugin plugin;

    public Party(MyExperiencePlugin plugin, Player leader) {
        this.plugin = plugin;
        members.add(leader.getUniqueId());
    }

    // Add a member to the party
    public boolean addMember(Player player) {
        if (members.size() >= 3) {
            return false; // Party is full
        }
        members.add(player.getUniqueId());
        return true;
    }

    // Remove a member from the party
    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    // Get all members
    public Set<UUID> getMembers() {
        return members;
    }
}
