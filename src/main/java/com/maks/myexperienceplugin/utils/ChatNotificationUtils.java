package com.maks.myexperienceplugin.utils;

import org.bukkit.entity.Player;

/** Utility for skill-related chat messages. */
public class ChatNotificationUtils {
    /** Toggle for chat notifications. Disabled by default to prevent spam. */
    public static boolean ENABLED = false;

    /**
     * Sends a chat message if notifications are enabled.
     * @param player target player
     * @param message message text
     */
    public static void send(Player player, String message) {
        if (ENABLED) {
            player.sendMessage(message);
        }
    }
}
