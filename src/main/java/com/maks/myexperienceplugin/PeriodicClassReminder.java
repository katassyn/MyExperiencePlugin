package com.maks.myexperienceplugin;

import net.md_5.bungee.api.ChatColor;              // Bungee ChatColor
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PeriodicClassReminder extends BukkitRunnable {

    private final MyExperiencePlugin plugin;

    public PeriodicClassReminder(MyExperiencePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String baseClass = plugin.getClassManager().getPlayerClass(player.getUniqueId());
            String ascend = plugin.getClassManager().getPlayerAscendancy(player.getUniqueId());
            int level = plugin.getPlayerLevel(player);

            // If no base class => clickable message
            if ("NoClass".equalsIgnoreCase(baseClass)) {
                sendNoClassMessage(player);
            }
            // If they do have base class, but no ascendancy, and level >= 20 => clickable message
            else if (ascend.isEmpty() && level >= 20) {
                sendNoAscendancyMessage(player, baseClass);
            }
        }
    }

    private void sendNoClassMessage(Player player) {
        player.sendMessage("§e=============== §6[§aCLASS§6] §e===============");
        player.sendMessage("§cYou haven't selected your base class yet!");

        TextComponent clickHere = new TextComponent("[CLICK HERE]");
        clickHere.setColor(ChatColor.YELLOW);
        clickHere.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder("Click to open the class selection GUI").create()
        ));
        clickHere.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/chose_class"
        ));

        TextComponent line = new TextComponent("§aYou can select your class now ");
        line.addExtra(clickHere);
        line.addExtra("§a!");
        player.spigot().sendMessage(line);
        player.sendMessage("§e================================");
    }

    private void sendNoAscendancyMessage(Player player, String baseClass) {
        player.sendMessage("§e=============== §6[§aASCENDANCY§6] §e===============");
        player.sendMessage("§cYou haven't chosen an ascendancy yet for: §e" + baseClass);

        TextComponent clickHere = new TextComponent("[CLICK HERE]");
        clickHere.setColor(ChatColor.AQUA);
        clickHere.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder("Click to open the ascendancy GUI").create()
        ));
        clickHere.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/chose_ascendancy"
        ));

        TextComponent line = new TextComponent("§aYou can select your ascendancy now ");
        line.addExtra(clickHere);
        line.addExtra("§a!");
        player.spigot().sendMessage(line);
        player.sendMessage("§e================================");
    }
}
