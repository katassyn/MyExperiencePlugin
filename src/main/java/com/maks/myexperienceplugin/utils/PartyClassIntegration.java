package com.maks.myexperienceplugin.utils;

import com.maks.myexperienceplugin.MyExperiencePlugin;
import com.maks.myexperienceplugin.party.PartyAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for integrating Party system with Class system
 */
public class PartyClassIntegration {
    private final MyExperiencePlugin plugin;
    
    public PartyClassIntegration(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if two players are in the same party
     * @param player1 First player
     * @param player2 Second player
     * @return true if both players are in the same party
     */
    public static boolean areInSameParty(Player player1, Player player2) {
        if (player1 == null || player2 == null) {
            return false;
        }
        
        if (player1.equals(player2)) {
            return true; // Same player
        }
        
        // Check if both are in a party
        if (!PartyAPI.isInParty(player1) || !PartyAPI.isInParty(player2)) {
            return false;
        }
        
        // Get party members of player1
        List<Player> partyMembers = PartyAPI.getPartyMembers(player1);
        
        // Check if player2 is in the same party
        return partyMembers.contains(player2);
    }
    
    /**
     * Checks if an entity is a party member of the player
     * @param player The player
     * @param entity The entity to check
     * @return true if the entity is a player in the same party
     */
    public static boolean isPartyMember(Player player, Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        
        return areInSameParty(player, (Player) entity);
    }
    
    /**
     * Gets the number of party members within a certain distance
     * @param player The player to check around
     * @param distance The maximum distance
     * @param includePlayer Whether to include the player themselves in the count
     * @return Number of party members within distance
     */
    public static int getNearbyPartyMembers(Player player, double distance, boolean includePlayer) {
        if (!PartyAPI.isInParty(player)) {
            return includePlayer ? 1 : 0;
        }
        
        List<Player> partyMembers = PartyAPI.getPartyMembers(player);
        int count = 0;
        
        for (Player member : partyMembers) {
            if (!includePlayer && member.equals(player)) {
                continue;
            }
            
            if (member.getWorld().equals(player.getWorld()) && 
                member.getLocation().distance(player.getLocation()) <= distance) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Gets all party members within a certain distance
     * @param player The player to check around
     * @param distance The maximum distance
     * @param includePlayer Whether to include the player themselves
     * @return List of party members within distance
     */
    public static List<Player> getNearbyPartyMembersList(Player player, double distance, boolean includePlayer) {
        List<Player> nearbyMembers = new java.util.ArrayList<>();
        
        if (!PartyAPI.isInParty(player)) {
            if (includePlayer) {
                nearbyMembers.add(player);
            }
            return nearbyMembers;
        }
        
        List<Player> partyMembers = PartyAPI.getPartyMembers(player);
        
        for (Player member : partyMembers) {
            if (!includePlayer && member.equals(player)) {
                continue;
            }
            
            if (member.getWorld().equals(player.getWorld()) && 
                member.getLocation().distance(player.getLocation()) <= distance) {
                nearbyMembers.add(member);
            }
        }
        
        return nearbyMembers;
    }
    
    /**
     * Checks if a player should receive buffs from another player
     * (They are in the same party or are the same player)
     * @param buffProvider The player providing the buff
     * @param buffReceiver The player receiving the buff
     * @return true if the buff should be applied
     */
    public static boolean shouldReceiveBuff(Player buffProvider, Player buffReceiver) {
        if (buffProvider.equals(buffReceiver)) {
            return true; // Always buff yourself
        }
        
        return areInSameParty(buffProvider, buffReceiver);
    }
    
    /**
     * Checks if damage should be prevented between two players
     * (They are in the same party)
     * @param attacker The attacking player
     * @param victim The victim player
     * @return true if damage should be prevented
     */
    public static boolean shouldPreventDamage(Player attacker, Player victim) {
        return areInSameParty(attacker, victim);
    }
}
