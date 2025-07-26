package com.maks.myexperienceplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class for debug messages
 */
public class DebugUtils {

    /**
     * Sends a debug message to the player's chat with the roll value and result
     * 
     * @param player The player to send the message to
     * @param mechanicName The name of the mechanic being rolled (e.g., "Critical Hit")
     * @param chance The chance of success (0-100)
     * @param roll The actual roll value (0.0-1.0)
     * @param success Whether the roll was successful
     */
    public static void sendRollDebugMessage(Player player, String mechanicName, double chance, double roll, boolean success) {
        String message = String.format(
            "%s[DEBUG] %s: %.1f%% chance | Roll: %.2f | %s",
            ChatColor.DARK_GRAY,
            mechanicName,
            chance,
            roll,
            success ? ChatColor.GREEN + "SUCCESS ✓" : ChatColor.RED + "FAILED ✗"
        );
        player.sendMessage(message);
    }

    /**
     * Sends a debug message to the player's chat
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void sendDebugMessage(Player player, String message) {
        player.sendMessage(ChatColor.DARK_GRAY + "[DEBUG] " + message);
    }

    /**
     * Rolls a chance and sends a debug message to the player's chat
     * 
     * @param player The player to send the message to
     * @param mechanicName The name of the mechanic being rolled (e.g., "Critical Hit")
     * @param chance The chance of success (0-100)
     * @return Whether the roll was successful
     */
    public static boolean rollChanceWithDebug(Player player, String mechanicName, double chance) {
        double roll = Math.random();
        boolean success = roll * 100 < chance;
        sendRollDebugMessage(player, mechanicName, chance, roll, success);
        return success;
    }
}
