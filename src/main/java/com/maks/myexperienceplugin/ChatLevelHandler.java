package com.maks.myexperienceplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatLevelHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private final int maxLevel = 100; // Maximum level

    public ChatLevelHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        int level = plugin.getPlayerLevel(player);

        String levelTag;

        // Determine the level tag to prepend
        if (level >= maxLevel) {
            levelTag = "§4§l[MAX LEVEL]§r";
        } else {
            levelTag = String.format("§b[ %d ]§r", level);
        }

        // Get the player's display name (preserves prefixes/suffixes from other plugins)
        String displayName = player.getDisplayName();

        // Combine the level tag, display name, and message
        String formattedMessage = String.format("%s %s: %s", levelTag, displayName, event.getMessage());

        // Set the formatted message as the chat output
        event.setFormat(formattedMessage);
    }
}
