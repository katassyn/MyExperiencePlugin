package com.maks.myexperienceplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatLevelHandler implements Listener {

    private final MyExperiencePlugin plugin;
    private final int maxLevel = 100;  // Maksymalny poziom

    public ChatLevelHandler(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        int level = plugin.getPlayerLevel(player);

        String formattedMessage;

        // Sprawdzamy, czy gracz osiągnął maksymalny poziom
        if (level >= maxLevel) {
            // Jeśli poziom gracza to maksymalny poziom, wyświetlamy [MAX LEVEL] na czerwono i pogrubione
            formattedMessage = String.format("§4§l[MAX LEVEL]§r %s: %s", player.getName(), event.getMessage());
        } else {
            // Standardowe formatowanie [ X ]
            formattedMessage = String.format("§b[ %d§r§b ]§r %s: %s", level, player.getName(), event.getMessage());
        }

        // Ustawiamy sformatowaną wiadomość
        event.setFormat(formattedMessage);
    }
}
