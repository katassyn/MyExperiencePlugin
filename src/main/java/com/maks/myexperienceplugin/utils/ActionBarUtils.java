package com.maks.myexperienceplugin.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * Utility class for sending action bar messages to players
 */
public class ActionBarUtils {
    /**
     * Sends an action bar message to a player (displays above hotbar)
     * @param player The player to send the message to
     * @param message The message to display
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
